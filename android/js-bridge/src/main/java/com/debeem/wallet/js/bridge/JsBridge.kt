package com.debeem.wallet.js.bridge

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.VisibleForTesting
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * JsBridge: A class that provides a bridge between Android and JavaScript using WebView.
 * It allows calling JavaScript functions from Android and handling the results.
 *
 * @param context The Android Context, used for creating the WebView.
 * @param onInitialized A function to be called when the SDK is initialized, passing a Boolean indicating success.
 * @throws IllegalArgumentException if the context is null.
 */
class JsBridge(
    private val context: Context,
    private val onInitialized: (Boolean) -> Unit,
) {
    // WebView instance used to load and execute JavaScript
    private var webView: WebView? = null

    // Flag to indicate if the SDK has been initialized
    private var inited = AtomicBoolean(false)

    // Map to store callbacks for JavaScript function calls
    private val callbackMap = mutableMapOf<String, ((String) -> Unit)?>()

    companion object {
        const val TAG = "JSBridgeSDK"
        private const val MAX_SCRIPT_LENGTH = 10000
    }

    init {
        setupWebView()
    }

    /**
     * Sets up the WebView for JavaScript interaction and HTML content loading.
     *
     * This method performs the following tasks:
     * 1. Creates and configures a WebView instance with JavaScript and DOM storage enabled.
     * 2. Sets up a WebViewClient to handle page loading events.
     * 3. Adds a JavaScript interface to allow communication between JavaScript and Android.
     * 4. Loads a local HTML file into the WebView.
     * 5. Initializes NPM when the page finishes loading.
     *
     * If any exception occurs during setup, it calls the onInitialized callback with false.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        try {
            webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
//                        Log.d(TAG, "onPageFinished")
                        // Initialize NPM when the page is finished loading
                        initialize {
                            inited.set(it)
                            onInitialized(it)
                            Log.d(TAG, "initNPM: $it")
                        }
                    }
                }
                // Add this class as a JavaScript interface, accessible in JS as 'Android'
                addJavascriptInterface(this@JsBridge, "Android")
            }
            webView?.loadUrl("file:///android_asset/index.html")
        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up WebView: ${e.message}")
            onInitialized(false)
        }
    }

    /**
     * Call a custom JavaScript script.
     *
     * @param label A label for the script execution.
     * @param script The JavaScript code to execute.
     * @param callback Function to be called with the result of the script execution.
     * @throws IllegalArgumentException if the script is empty or too long.
     */
    fun callScript(label: String, script: String, callback: (String) -> Unit) {
        require(label.isNotBlank()) { "CallScript: Label cannot be blank or empty in ${this::class.simpleName}.callScript()" }
        require(script.isNotEmpty()) { "CallScript: Script cannot be empty in ${this::class.simpleName}.callScript()" }
        require(script.length <= MAX_SCRIPT_LENGTH) { "CallScript: Script is too long (max $MAX_SCRIPT_LENGTH characters) in ${this::class.simpleName}.callScript()" }
        if (label != "initialize") {
            require(isInitialized()) { "CallScript: SDK is not initialized in ${this::class.simpleName}.callScript()" }
        }

        callbackMap[label] = callback
        try {
            webView?.evaluateJavascript(script, null)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage =
                "Error executing custom script in ${this::class.simpleName}.callScript(): ${e.message}"
            Log.e(TAG, errorMessage, e)
            callback("Error: $errorMessage")
        }
    }

    /**
     * Handle the result returned from JavaScript.
     * This method is called by JavaScript using the 'Android' interface.
     *
     * @param functionName The name of the function that was called.
     * @param result The result returned by the JavaScript function.
     */
    @JavascriptInterface
    fun handleResult(functionName: String, result: String) {
        Log.d("$TAG:handleResult", "Received from JS: $functionName : $result")

        if (functionName.isBlank()) {
            Log.e("$TAG:handleResult", "Error: functionName is empty or blank")
            return
        }

        val callback = callbackMap.remove(functionName)
        if (callback == null) {
            Log.w("$TAG:handleResult", "Warning: No callback found for function: $functionName")
            return
        }
        callback.invoke(result)
    }

    /**
     * Check if the SDK has been initialized.
     *
     * @return Boolean indicating whether the SDK is initialized.
     */
    fun isInitialized(): Boolean = inited.get()

    /**
     * Initialize NPM by calling the 'initialize' JavaScript function.
     *
     * @param callback Function to be called with the result of the initialization.
     */
    private fun initialize(callback: (Boolean) -> Unit) {
        val label = "initialize"
        val script = """
                (function(){
                        const init = async () => {
                            try {
                                console.log('initialize');
                                return { success: true };
                            } catch (error) {
                                return { success: false, error: error.toString() };
                            }
                        };

                        init().then(result => {
                            window.Android.handleResult(`${label}`, JSON.stringify(result));
                        });
                    })();
            """.trimIndent()

        callScript(label, script) {
            try {
                val jsonResult = JSONObject(it)
                val result = jsonResult.getBoolean("success")
                callback(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Dispose of the SDK by clearing the WebView.
     */
    fun dispose() {
        clearWebView()
    }

    private fun clearWebView() {
        webView?.apply {
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            removeAllViews()
            webChromeClient = null
            destroy()
        }
        webView = null
        callbackMap.clear()
        inited.set(false)
    }

    @VisibleForTesting
    internal fun setInitedForTesting(initialized: Boolean) {
        inited.set(initialized)
    }

    @VisibleForTesting
    internal fun isInitedForTesting(): Boolean {
        return inited.get()
    }

    @VisibleForTesting
    internal fun getCallbackMapForTesting(): MutableMap<String, ((String) -> Unit)?> {
        return callbackMap
    }

    @VisibleForTesting
    internal fun getWebViewForTesting(): WebView? {
        return webView
    }

    @VisibleForTesting
    internal fun getInitedForTesting(): AtomicBoolean {
        return inited
    }

    @VisibleForTesting
    internal fun clearWebViewForTesting() {
        clearWebView()
    }
}