package com.debeem.wallet.npm.js_bridge_npm

import android.content.Context
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
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
    private lateinit var mockSettings: WebSettings
    private lateinit var sdk: NpmServiceSDK
    private lateinit var onInitializedCallback: (Boolean) -> Unit

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockWebView = mock(WebView::class.java)
        mockSettings = mock(WebSettings::class.java)
        onInitializedCallback = mock(Function1::class.java) as (Boolean) -> Unit
        sdk = NpmServiceSDK(
            context = mockContext,
            webViewProvider = { mockWebView },
            onInitialized = onInitializedCallback
        )

        `when`(mockWebView.settings).thenReturn(mockSettings)

        mockWebView.apply {
            settings.javaScriptEnabled = true
            webChromeClient = mock(WebChromeClient::class.java)
            webViewClient = mock(WebViewClient::class.java)
            addJavascriptInterface(sdk, "Android")
            loadUrl("file:///android_asset/index.html")
        }

        sdk.initNpmForTesting {
            assertTrue(it)
        }
    }

    @Test
    fun testInitialization() {
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
    fun testCallJsFunctionSDKNotInit() {
        sdk.setInitedForTesting(false)
        val msg = "SDK is not initialized"
        var msgResult = ""
        try {
            sdk.callJsFunctionAsync("testPackage", "testFunction", "arg1", "arg2") {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCallJsFunctionAsync() {
        sdk.setInitedForTesting(true)
        sdk.callJsFunctionAsync("testPackage", "testFunction", "arg1", "arg2") {}
    }

    @Test
    fun testCallJsFunctionSync() {
        val msg = ""
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.callJsFunctionSync("testPackage", "testFunction", "arg1", "arg2") {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCallJsFunctionAsyncWithLongFunctionName() {
        val msg = "Function name is too long"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.callJsFunctionAsync("", "a".repeat(101), "arg") {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCallJsFunctionAsyncWithLongPackageName() {
        val msg = "Package name is too long"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.callJsFunctionAsync("a".repeat(101), "testFunction", "arg") {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCallJsFunctionAsyncWithTooManyArgs() {
        val msg = "Too many arguments provided"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.callJsFunctionAsync("", "testFunction", *Array(11) { "arg" }) {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCreateCallJsFunctionAsync() {
        sdk.setInitedForTesting(true)
        sdk.createCallJsFunctionAsync("testPackage", "TestClass", listOf("arg1"), "testMethod", listOf("arg2")) {}
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithLongPackageName() {
        val msg = "Package name is too long"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.createCallJsFunctionAsync("a".repeat(101), "TestClass", null, "testMethod", null) {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithLongClassName() {
        val msg = "Class name is too long"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.createCallJsFunctionAsync("testPackage", "a".repeat(101), null, "testMethod", null) {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithLongMethodName() {
        val msg = "Method name is too long"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.createCallJsFunctionAsync("testPackage", "TestClass", null, "a".repeat(101), null) {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithTooManyConstructorArgs() {
        val msg = "Too many constructor arguments"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.createCallJsFunctionAsync("testPackage", "TestClass", List(11) { "arg" }, "testMethod", null) {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCreateCallJsFunctionAsyncWithTooManyMethodArgs() {
        val msg = "Too many method arguments"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.createCallJsFunctionAsync("testPackage", "TestClass", null, "testMethod", List(11) { "arg" }) {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCreateCallJsFunctionAsyncWhenNotInitialized() {
        val msg = "SDK is not initialized"
        var msgResult = ""
        sdk.setInitedForTesting(false)
        try {
            sdk.createCallJsFunctionAsync("testPackage", "TestClass", null, "testMethod", null) {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCallScript() {
        sdk.setInitedForTesting(true)
        sdk.callScript("testLabel", "console.log('test');") {}
    }

    @Test
    fun testCallScriptWithEmptyScript() {
        val msg = "Script cannot be empty"
        var msgResult = ""
        sdk.setInitedForTesting(true)
        try {
            sdk.callScript("testLabel", "") {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCallScriptWithTooLongScript() {
        sdk.setInitedForTesting(true)
        val msg = "Script is too long"
        var msgResult = ""
        try {
            sdk.callScript("testLabel", "a".repeat(10001)) {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testCallScriptWhenNotInitialized() {
        val msg = "SDK is not initialized"
        var msgResult = ""
        sdk.setInitedForTesting(false)
        try {
            sdk.callScript("testLabel", "console.log('test');") {}
        } catch (e: Exception) {
            msgResult = e.message ?: ""
        }
        assertEquals(msg, msgResult)
    }

    @Test
    fun testDispose() {
        sdk.setInitedForTesting(true)
        sdk.dispose()
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

}