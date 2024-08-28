package com.debeem.wallet.npm.sample

import android.content.Context
import android.util.Log
import com.debeem.wallet.js.bridge.JsBridge
import org.json.JSONObject

class WalletBusiness(context: Context, callback: (Boolean) -> Unit) {
    private var jsBridge: JsBridge

    init {
        jsBridge = JsBridge(context) {
            callback(it)
        }
    }

    fun customScript(label: String, script: String, callback: (String) -> Unit) {
        jsBridge.callScript(label, script) {
            callback(it)
        }
    }

    fun dispose() {
        jsBridge.dispose()
    }

}