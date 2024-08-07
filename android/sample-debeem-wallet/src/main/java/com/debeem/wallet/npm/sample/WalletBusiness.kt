package com.debeem.wallet.npm.sample

import android.content.Context
import android.util.Log
import com.debeem.wallet.npm.js_bridge_npm.NpmServiceSDK
import org.json.JSONObject

class WalletBusiness(context: Context, callback: (Boolean) -> Unit) {
    private var npmServiceSDK: NpmServiceSDK

    init {
        npmServiceSDK = NpmServiceSDK(context, callback)
    }

    fun initializeWallet(initDB: Boolean = true, callback: (Boolean) -> Unit) {
        npmServiceSDK.callJsFunctionAsync("", "initializeWallet", initDB) {
            Log.d("WalletBusiness", "initializeWallet: $it")
            val jsonResult = JSONObject(it)
            Log.d("WalletBusiness", "initializeWallet jsonResult: $jsonResult")
            val result = jsonResult.getBoolean("success")
            callback(result)
        }
    }

    fun callJsFunctionAsync(
        packageName: String,
        functionName: String,
        vararg args: Any,
        callback: (String) -> Unit,
    ) {
        npmServiceSDK.callJsFunctionAsync(packageName, functionName, *args) {
            callback(it)
        }
    }

    fun createCallJsFunctionAsync(
        packageName: String,
        className: String,
        constructorArgs: List<Any>? = null,
        methodName: String,
        methodArgs: List<Any>? = null,
        callback: (String) -> Unit,
    ) {
        npmServiceSDK.createCallJsFunctionAsync(
            packageName,
            className,
            constructorArgs,
            methodName,
            methodArgs
        ) {
            callback(it)
        }
    }

    fun customScript(label: String, script: String, callback: (String) -> Unit) {
        npmServiceSDK.customScript(label, script) {
            callback(it)
        }
    }

    suspend fun clearCache(): Boolean {
//        val result = npmServiceSDK.evaluateJavascriptAsync("window.clearWalletCache()")
//        val jsonResult = JSONObject(result)
//        return jsonResult.getBoolean("success")
        return false
    }


}