package com.debeem.wallet.npm.js_bridge_npm

import android.content.Context
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.contains
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.util.Objects.isNull
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class NpmServiceSDKTest {

    companion object {
        const val TAG = "TestLog"
    }

    private lateinit var mockContext: Context
    private lateinit var mockWebView: WebView
    private lateinit var sdk: NpmServiceSDK
    private lateinit var onInitializedCallback: (Boolean) -> Unit

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockWebView = mock(WebView::class.java)
        onInitializedCallback = mock(Function1::class.java) as (Boolean) -> Unit
        sdk = NpmServiceSDK(
            context = mockContext,
            webViewProvider = { mockWebView },
            onInitialized = onInitializedCallback
        )
        sdk.setInitedForTesting(true) // Directly set the SDK to initialized for testing
    }

    @Test
    fun testInitialization() {
        mockWebView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webChromeClient = mock(WebChromeClient::class.java)
            webViewClient = mock(WebViewClient::class.java)
            addJavascriptInterface(sdk, "Android")
            loadUrl("file:///android_asset/index.html")
        }
    }

    @Test
    fun testIsInitialized() {
        assertFalse(sdk.isInitialized())
        sdk.setInitedForTesting(true)
        assertTrue(sdk.isInitialized())
    }

    @Test
    fun testHandleResult() {
        val latch = CountDownLatch(1)
        sdk.setInitedForTesting(true)
        sdk.callJsFunctionAsync("", "testFunction") {
            assertEquals("testResult", it)
            latch.countDown()
        }
        sdk.handleResult("testFunction", "testResult")
        assertTrue(latch.await(1, TimeUnit.SECONDS))
    }

    @Test
    fun testCallJsFunctionAsync() {
        sdk.setInitedForTesting(true)
        sdk.callJsFunctionAsync("testPackage", "testFunction", "arg1", "arg2") {}
        verify(mockWebView).evaluateJavascript(contains("testPackage"), null)
    }

    @Test
    fun testCallJsFunctionSync() {
        sdk.setInitedForTesting(true)
        sdk.callJsFunctionSync("testPackage", "testFunction", "arg1", "arg2") {}
        verify(mockWebView).evaluateJavascript(contains("testPackage"), any())
    }

    @Test
    fun testCallJsFunctionAsyncWithLongFunctionName() {
        sdk.setInitedForTesting(true)
        sdk.callJsFunctionAsync("", "a".repeat(101), "arg") {}
    }

    @Test
    fun testCallJsFunctionAsyncWithLongPackageName() {
        sdk.setInitedForTesting(true)
        sdk.callJsFunctionAsync("a".repeat(101), "testFunction", "arg") {}
    }

    @Test
    fun testCallJsFunctionAsyncWithTooManyArgs() {
        sdk.setInitedForTesting(true)
        sdk.callJsFunctionAsync("", "testFunction", *Array(11) { "arg" }) {}
    }

    @Test
    fun testCallJsFunctionAsyncWhenNotInitialized() {
        sdk.callJsFunctionAsync("", "testFunction") {}
    }

    @Test
    fun testCreateCallJsFunctionAsync() {
        sdk.setInitedForTesting(true)
        sdk.createCallJsFunctionAsync("testPackage", "TestClass", listOf("arg1"), "testMethod", listOf("arg2")) {}
        verify(mockWebView).evaluateJavascript(contains("createAndCallMethod"), isNull())
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithLongPackageName() {
        sdk.setInitedForTesting(true)
        sdk.createCallJsFunctionAsync("a".repeat(101), "TestClass", null, "testMethod", null) {}
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithLongClassName() {
        sdk.setInitedForTesting(true)
        sdk.createCallJsFunctionAsync("testPackage", "a".repeat(101), null, "testMethod", null) {}
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithLongMethodName() {
        sdk.setInitedForTesting(true)
        sdk.createCallJsFunctionAsync("testPackage", "TestClass", null, "a".repeat(101), null) {}
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithTooManyConstructorArgs() {
        sdk.setInitedForTesting(true)
        sdk.createCallJsFunctionAsync("testPackage", "TestClass", List(11) { "arg" }, "testMethod", null) {}
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithTooManyMethodArgs() {
        sdk.setInitedForTesting(true)
        sdk.createCallJsFunctionAsync("testPackage", "TestClass", null, "testMethod", List(11) { "arg" }) {}
    }

    @Test  
    fun testCreateCallJsFunctionAsyncWhenNotInitialized() {
        sdk.createCallJsFunctionAsync("testPackage", "TestClass", null, "testMethod", null) {}
    }

    @Test
    fun testCallScript() {
        sdk.setInitedForTesting(true)
        sdk.callScript("testLabel", "console.log('test');") {}
        verify(mockWebView).evaluateJavascript(eq("console.log('test');"), null)
    }

    @Test
    fun testCallScriptWithEmptyScript() {
        sdk.setInitedForTesting(true)
        sdk.callScript("testLabel", "") {}
    }

    @Test
    fun testCallScriptWithTooLongScript() {
        sdk.setInitedForTesting(true)
        sdk.callScript("testLabel", "a".repeat(10001)) {}
    }

    @Test  
    fun testCallScriptWhenNotInitialized() {
        sdk.callScript("testLabel", "console.log('test');") {}
    }

    @Test
    fun testDispose() {
        sdk.dispose()
        verify(mockWebView).clearHistory()
        verify(mockWebView).clearCache(true)
        verify(mockWebView).loadUrl("about:blank")
        verify(mockWebView).removeAllViews()
        verify(mockWebView).destroy()
        assertFalse(sdk.isInitedForTesting())
    }

    @Test
    fun testInitWithNullContext() {
        (null as Context?)?.let {
            NpmServiceSDK(
                context = it,
                webViewProvider = { mockWebView },
                onInitialized = {}
            )
        }
    }

    @Test
    fun testWebViewLoadFailure() {
        doThrow(RuntimeException("WebView load failed")).`when`(mockWebView).loadUrl(any())

        var initializationResult = true
        NpmServiceSDK(
            mockContext,
            webViewProvider = { mockWebView },
            onInitialized = { result -> initializationResult = result }
        )

        assertFalse(initializationResult)
    }

    @Test
    fun testWebViewClientOnPageFinished() {
//        val webViewClient = mock(WebViewClient::class.java)
//        `when`(mockWebView.webViewClient).thenReturn(webViewClient)
//
//        val captor = argumentCaptor<WebViewClient>()
//        verify(mockWebView).webViewClient = captor.capture()
//
//        captor.value.onPageFinished(mockWebView, "https://example.com")
//
//        verify(mockWebView).evaluateJavascript(contains("initialize"), isNull())
    }
}