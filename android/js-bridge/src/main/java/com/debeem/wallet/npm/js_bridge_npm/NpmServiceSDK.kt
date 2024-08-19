package com.debeem.wallet.npm.js_bridge_npm

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * NpmServiceSDK: A class that provides a bridge between Android and JavaScript using WebView.
 * It allows calling JavaScript functions from Android and handling the results.
 *
 * @param context The Android Context, used for creating the WebView.
 * @param onInitialized A function to be called when the SDK is initialized, passing a Boolean indicating success.
 * @throws IllegalArgumentException if the context is null.
 */
class NpmServiceSDK(
    private val context: Context,
    private val webViewProvider: () -> WebView = { (WebView(context)) },
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
        private const val MAX_FUNCTION_NAME_LENGTH = 100
        private const val MAX_PACKAGE_NAME_LENGTH = 100
        private const val MAX_ARGS_COUNT = 10
    }

    init {
        require(context != null) { "Context cannot be null" }
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        try {
            webView = webViewProvider().apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "onPageFinished")
                        // Initialize NPM when the page is finished loading
                        initNpm(initialized = true) {
                            inited.set(it)
                            onInitialized(it)
                            Log.d(TAG, "initNPM: $it")
                        }
                    }
                }
                // Add this class as a JavaScript interface, accessible in JS as 'Android'
                addJavascriptInterface(this@NpmServiceSDK, "Android")
            }
            webView?.loadUrl("file:///android_asset/index.html")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up WebView: ${e.message}")
            onInitialized(false)
        }
    }


    /**
     * Initialize NPM by calling the 'initialize' JavaScript function.
     *
     * @param initialized Boolean flag to indicate if the somethings should be initialized.
     * @param callback Function to be called with the result of the initialization.
     */
    private fun initNpm(initialized: Boolean = false, callback: (Boolean) -> Unit) {
        callJsFunctionAsync(packageName = "", functionName = "initialize", initialized) {
            try {
                Log.d(TAG, "initNpm: $it")
                val jsonResult = JSONObject(it)
                val result = jsonResult.getBoolean("success")
                callback(result)
            } catch (e: JSONException) {
                Log.e(TAG, "Error parsing JSON in initNpm: ${e.message}")
                callback(false)
            }
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
        callbackMap.remove(functionName)?.invoke(result)
    }

    /**
     * Check if the SDK has been initialized.
     *
     * @return Boolean indicating whether the SDK is initialized.
     */
    fun isInitialized(): Boolean = inited.get()

    /**
     * Call a JavaScript function asynchronously.
     *
     * @param packageName The package name of the function (can be empty).
     * @param functionName The name of the function to call.
     * @param args Variable number of arguments to pass to the function.
     * @param callback Function to be called with the result of the JavaScript function.
     * @throws IllegalArgumentException if function name or package name is too long, or too many arguments are provided.
     */
    fun callJsFunctionAsync(
        packageName: String = "",
        functionName: String,
        vararg args: Any,
        callback: (String) -> Unit,
    ) {
        require(functionName.length <= MAX_FUNCTION_NAME_LENGTH) { "Function name is too long" }
        require(packageName.length <= MAX_PACKAGE_NAME_LENGTH) { "Package name is too long" }
        require(args.size <= MAX_ARGS_COUNT) { "Too many arguments provided" }
        check(isInitialized()) { "SDK is not initialized" }

        val key = if (packageName.isEmpty()) functionName else "$packageName.$functionName"
        callbackMap[key] = callback
        try {
            webView?.evaluateJavascript(script(packageName, functionName, *args), null)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling JS function: ${e.message}")
            callback("Error: ${e.message}")
        }
    }

    /**
     * Call a JavaScript function synchronously.
     *
     * @param packageName The package name of the function.
     * @param functionName The name of the function to call.
     * @param args Variable number of arguments to pass to the function.
     * @param callback Function to be called with the result of the JavaScript function.
     * @throws IllegalArgumentException if function name or package name is too long, or too many arguments are provided.
     */
    fun callJsFunctionSync(
        packageName: String,
        functionName: String,
        vararg args: Any?,
        callback: (String) -> Unit,
    ) {
        require(functionName.length <= MAX_FUNCTION_NAME_LENGTH) { "Function name is too long" }
        require(packageName.length <= MAX_PACKAGE_NAME_LENGTH) { "Package name is too long" }
        require(args.size <= MAX_ARGS_COUNT) { "Too many arguments provided" }
        check(isInitialized()) { "SDK is not initialized" }

        try {
            webView?.evaluateJavascript(
                script(
                    packageName,
                    functionName,
                    *args,
                    async = false
                )
            ) { result ->
                Log.d("$TAG:webView", "Callback data: $result")
                callback(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling JS function synchronously: ${e.message}")
            callback("Error: ${e.message}")
        }
    }

    private fun script(
        packageName: String,
        functionName: String,
        vararg args: Any?,
        async: Boolean = true,
    ): String {
        val jsArgs =
            args.joinToString(", ") { "JSON.parse('${it.toString().replace("'", "\\'")}')" }

        val callbackScript = """
            function(result) {
                console.log(result);
                Android.handleResult('$functionName', result);
            }
        """.trimIndent()

//        Log.d(TAG, "callbackScript: $callbackScript, jsArgs: $jsArgs")

        val script = when {
            packageName.isEmpty() -> {
                when {
                    jsArgs.isNotEmpty() -> {
                        if (async) """
                            $functionName($jsArgs, $callbackScript);
                        """.trimIndent()
                        else """
                            $functionName($jsArgs);
                        """.trimIndent()
                    }

                    else -> {
                        if (async) """
                            $functionName($callbackScript);
                        """.trimIndent()
                        else """
                            $functionName();
                        """.trimIndent()
                    }
                }
            }

            else -> {
                when {
                    jsArgs.isNotEmpty() -> {
                        """
                            callNpmMethod('$packageName', '$functionName', $jsArgs, $callbackScript);
                        """.trimIndent()
                    }

                    else -> {
                        """
                            callNpmMethod('$packageName', '$functionName', $callbackScript);
                        """.trimIndent()
                    }
                }

            }

        }
        Log.d(TAG, "script: $script")
        return script
    }

    /**
     * Create and call a JavaScript method on a class instance.
     *
     * @param packageName The package name of the class.
     * @param className The name of the class.
     * @param constructorArgs Optional list of constructor arguments.
     * @param methodName The name of the method to call.
     * @param methodArgs Optional list of method arguments.
     * @param callback Function to be called with the result of the method call.
     * @throws IllegalArgumentException if any name is too long or too many arguments are provided.
     */
    fun createCallJsFunctionAsync(
        packageName: String,
        className: String,
        constructorArgs: List<Any>? = null,
        methodName: String,
        methodArgs: List<Any>? = null,
        callback: (String) -> Unit,
    ) {
        require(packageName.length <= MAX_PACKAGE_NAME_LENGTH) { "Package name is too long" }
        require(className.length <= MAX_FUNCTION_NAME_LENGTH) { "Class name is too long" }
        require(methodName.length <= MAX_FUNCTION_NAME_LENGTH) { "Method name is too long" }
        require((constructorArgs?.size ?: 0) <= MAX_ARGS_COUNT) { "Too many constructor arguments" }
        require((methodArgs?.size ?: 0) <= MAX_ARGS_COUNT) { "Too many method arguments" }
        check(isInitialized()) { "SDK is not initialized" }

        callbackMap["$packageName.$className.$methodName"] = callback

        try {
            val constructorArgsJson = constructorArgs?.let { JSONArray(it).toString() } ?: "null"
            val methodArgsJson = methodArgs?.let { JSONArray(it).toString() } ?: "[]"

            val script = """
                window.createAndCallMethod(
                    '$packageName',
                    '$className',
                    $constructorArgsJson,
                    '$methodName',
                    $methodArgsJson,
                    null
                );
            """.trimIndent()

            Log.d(TAG, "script: $script")
            webView?.evaluateJavascript(script, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating and calling JS function: ${e.message}")
            callback("Error: ${e.message}")
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
        require(script.isNotEmpty()) { "Script cannot be empty" }
        require(script.length <= 10000) { "Script is too long" }  // Arbitrary limit, adjust as needed
        check(isInitialized()) { "SDK is not initialized" }

        callbackMap[label] = callback
        try {
            webView?.evaluateJavascript(script, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing custom script: ${e.message}")
            callback("Error: ${e.message}")
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

    // Add a test method to set the inited status.
    @VisibleForTesting
    internal fun setInitedForTesting(value: Boolean) {
        inited.set(value)
    }

    // Add a test method to get the inited status.
    @VisibleForTesting
    internal fun isInitedForTesting(): Boolean {
        return inited.get()
    }
}