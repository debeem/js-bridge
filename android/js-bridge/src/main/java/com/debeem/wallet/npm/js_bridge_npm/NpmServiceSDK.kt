package com.debeem.wallet.npm.js_bridge_npm

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NpmServiceSDK(private val context: Context, private val callback: (Boolean) -> Unit) {
    private var webView: WebView? = null
    private var inited: Boolean = false
    private val callbackMap = mutableMapOf<String, ((String) -> Unit)?>()

    companion object {
        const val TAG = "JSBridgeSDK"
    }

    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = WebView(context).apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(this@NpmServiceSDK, "Android")
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    // 页面加载完成后的操作（如果需要）
                    super.onPageFinished(view, url)
                    Log.d(TAG, "onPageFinished")
                    initNpm(initDB = false) {
                        inited = it
                        callback(it)
                        Log.d(TAG, "initNPM: $it")
                    }
                }
            }
        }
        webView?.loadUrl("file:///android_asset/index.html")
    }


    private fun initNpm(initDB: Boolean = false, callback: (Boolean) -> Unit) {
        callJsFunctionAsync(packageName = "", functionName = "initialize", initDB) {
            Log.d(TAG, "initNpm: $it")
            val jsonResult = JSONObject(it)
            val result = jsonResult.getBoolean("success")
            callback(result)
        }
    }

    // JavaScript 接口类
    @JavascriptInterface
    fun handleResult(functionName: String, result: String) {
        // 处理从 JS 接收的数据
        Log.d("$TAG:handleResult", "Received from JS: $functionName : $result")
        callbackMap.remove(functionName)?.invoke(result)
    }

    // 异步调用
    // 使用场景：调用 js 方法不能立即返回结果，需要执行一段时间才能完成任务。
    // 例如网络请求等，这就需要完成异步操作后主动调用原生端的回调函数（onJsCallback）
    fun callJsFunctionAsync(
        packageName: String = "",
        functionName: String,
        vararg args: Any,
        callback: (String) -> Unit,
    ) {
        // 保存回调方法，等待 js 回调 onJsCallback
        callbackMap[if (packageName.isEmpty()) functionName else "$packageName.$functionName"] =
            callback
        // 调用 JS 函数
        webView?.evaluateJavascript(script(packageName, functionName, *args), null)
    }

    fun createCallJsFunctionAsync(
        packageName: String,
        className: String,
        constructorArgs: List<Any>? = null,
        methodName: String,
        methodArgs: List<Any>? = null,
        callback: (String) -> Unit,
    ) {
        // 保存回调方法，等待 js 回调 onJsCallback
        callbackMap["$packageName.$className.$methodName"] = callback

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
    }


    // 同步调用
    // 所谓的“同步”是指等待JavaScript的执行结果，而不是阻塞主线程
    // 使用场景：调用 js 方法可以立即返回结果（result）
    fun callJsFunctionSync(
        packageName: String,
        functionName: String,
        vararg args: Any?,
        callback: (String) -> Unit,
    ) {
        // 调用 JS 函数
        webView?.evaluateJavascript(script(packageName, functionName, *args)) { result ->
            Log.d("$TAG:webView", "Callback data: $result")
            callback(result)
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

        // 同步不需要传递 callbackScript，异步需要传递
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
     * val script = """
     *
     * """
     */
    fun customScript(label: String, script: String, callback: (String) -> Unit) {
        callbackMap[label] = callback
        webView?.evaluateJavascript(script, null)
    }

    fun dispose() {
        clearWebView()
        // 清理其他资源...
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
    }
}