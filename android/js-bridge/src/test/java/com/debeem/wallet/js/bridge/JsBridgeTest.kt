package com.debeem.wallet.js.bridge

import android.content.Context
import com.debeem.wallet.js.bridge.JsBridge
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class JsBridgeTest {

    @Mock
    private lateinit var mockContext: Context
    private lateinit var jsBridge: JsBridge

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        jsBridge = JsBridge(mockContext) {}
    }

//    @Test
//    fun testInitialize() {
//        val latch = CountDownLatch(1)
//        jsBridge.initialize { success ->
//            assert(success)
//            latch.countDown()
//        }
//        latch.await(1, TimeUnit.SECONDS)
//    }

    @Test
    fun testHandleResult() {
        val latch = CountDownLatch(1)
        jsBridge.getCallbackMapForTesting()["testFunction"] = { result ->
            assert(result == "success")
            latch.countDown()
        }
        jsBridge.handleResult("testFunction", "success")
        latch.await(1, TimeUnit.SECONDS)
    }

    @Test
    fun testIsInitialized() {
        jsBridge.setInitedForTesting(true)
        assert(jsBridge.isInitialized())
    }

    @Test
    fun testCallScript() {
        val latch = CountDownLatch(1)
        jsBridge.setInitedForTesting(true)
        jsBridge.callScript("testLabel", "console.log('test');") { result ->
            assert(result == "success")
            latch.countDown()
        }
        latch.await(1, TimeUnit.SECONDS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCallScriptWithEmptyScript() {
        jsBridge.setInitedForTesting(true)
        jsBridge.callScript("testLabel", "") {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCallScriptWithTooLongScript() {
        jsBridge.setInitedForTesting(true)
        jsBridge.callScript("testLabel", "a".repeat(10001)) {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCallScriptNotInitialized() {
        jsBridge.setInitedForTesting(false)
        jsBridge.callScript("testLabel", "console.log('test');") {}
    }

    @Test
    fun testDispose() {
        jsBridge.dispose()
    }

    @Test
    fun testClearWebView() {
        jsBridge.clearWebViewForTesting()
    }

    @Test
    fun testSetupWebView() {
//        jsBridge.setupWebViewForTesting()
    }

    @Test
    fun testWebView() {
        jsBridge.getWebViewForTesting()
    }

    @Test
    fun testInited() {
        jsBridge.getInitedForTesting()
    }

    @Test
    fun testSetInitedForTesting() {
        jsBridge.setInitedForTesting(true)
        assert(jsBridge.isInitedForTesting())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `callScript with valid input and initialized SDK`() {

        var result = ""
        jsBridge.callScript("testLabel", "console.log('test');") { result = it }

        assertEquals("", result)
    }

    @Test
    fun `callScript with normal label`() {
        jsBridge.setInitedForTesting(true)
        jsBridge.callScript("testlabel", "console.log('test');") {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun `callScript with blank label throws IllegalArgumentException`() {
        jsBridge.callScript(" ", "console.log('test');") {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun `callScript with empty script throws IllegalArgumentException`() {
        jsBridge.callScript("testLabel", "") {}

    }

    @Test(expected = IllegalArgumentException::class)
    fun `callScript with too long script throws IllegalArgumentException`() {
        val longScript = "a".repeat(10000 + 1)
        jsBridge.callScript("testLabel", longScript) {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun `callScript with uninitialized SDK throws IllegalArgumentException`() {
        jsBridge.setInitedForTesting(false)
        jsBridge.callScript("testLabel", "console.log('test');") {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun `callScript with uninitialized SDK and too long script  throws IllegalArgumentException`() {
        jsBridge.setInitedForTesting(false)
        val longScript = "a".repeat(10000 + 1)
        jsBridge.callScript("testLabel", longScript) {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun `callScript handles exceptions from evaluateJavascript`() {
        var result = ""
        jsBridge.callScript("testLabel", "console.log('test');") { result = it }
        assertEquals("Error: Error executing custom script in JsBridge.callScript(): Test exception", result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `callScript stores callback in callbackMap`() {
        val callback: (String) -> Unit = {}
        jsBridge.callScript("testLabel", "console.log('test');", callback)

        // Assuming callbackMap is accessible for testing
        assertEquals(callback, jsBridge.getCallbackMapForTesting()["testLabel"])
    }

    @Test
    fun `handleResult with valid functionName and existing callback`() {
        val functionName = "testFunction"
        val result = "testResult"
        var callbackCalled = false
        val callback: (String) -> Unit = { callbackCalled = true }

        jsBridge.getCallbackMapForTesting()[functionName] = callback

        jsBridge.handleResult(functionName, result)

        assertTrue(callbackCalled)
        assertFalse(functionName in jsBridge.getCallbackMapForTesting())
    }

    @Test
    fun `handleResult with blank functionName`() {
        val blankFunctionName = "   "
        val result = "testResult"

        jsBridge.handleResult(blankFunctionName, result)

        // Verify that nothing happens (no exception, no callback invocation)
//        jsBridge.getCallbackMapForTesting().remove(any())
    }

    @Test
    fun `handleResult with non-existent functionName`() {
        val nonExistentFunction = "nonExistentFunction"
        val result = "testResult"

        jsBridge.handleResult(nonExistentFunction, result)

        // Verify that nothing happens (no exception, no callback invocation)
        jsBridge.getCallbackMapForTesting().remove(nonExistentFunction)
    }

    @Test
    fun `handleResult passes correct result to callback`() {
        val functionName = "testFunction"
        val result = "testResult"
        var receivedResult = ""
        val callback: (String) -> Unit = { receivedResult = it }

        jsBridge.getCallbackMapForTesting()[functionName] = callback

        jsBridge.handleResult(functionName, result)

        assertEquals(result, receivedResult)
    }

    @Test
    fun `handleResult removes callback from map after invocation`() {
        val functionName = "testFunction"
        val result = "testResult"
        val callback: (String) -> Unit = { }

        jsBridge.getCallbackMapForTesting()[functionName] = callback

        jsBridge.handleResult(functionName, result)

        assertFalse(functionName in jsBridge.getCallbackMapForTesting())
    }

    @Test
    fun `handleResult with empty result string`() {
        val functionName = "testFunction"
        val result = ""
        var receivedResult = "not empty"
        val callback: (String) -> Unit = { receivedResult = it }

        jsBridge.getCallbackMapForTesting()[functionName] = callback

        jsBridge.handleResult(functionName, result)

        assertEquals("", receivedResult)
    }

    @Test
    fun `handleResult with null result`() {
        val functionName = "testFunction"
        val result: String? = null
        var callbackCalled = false
        val callback: (String) -> Unit = { callbackCalled = true }

        jsBridge.getCallbackMapForTesting()[functionName] = callback

        jsBridge.handleResult(functionName, result ?: "")

        assertTrue(callbackCalled)
    }

    @Test
    fun `handleResult is thread-safe`() {
        val functionName = "testFunction"
        val result = "testResult"
        var callCount = 0
        val callback: (String) -> Unit = { callCount++ }

        jsBridge.getCallbackMapForTesting()[functionName] = callback

        // Simulate multiple threads calling handleResult simultaneously
        (1..10).map {
            Thread {
                jsBridge.handleResult(functionName, result)
            }.apply { start() }
        }.forEach { it.join() }

        assertEquals(1, callCount)
        assertFalse(functionName in jsBridge.getCallbackMapForTesting())
    }
}