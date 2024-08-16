package com.debeem.wallet.npm.sample

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.debeem.wallet.npm.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var walletBusiness: WalletBusiness

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.jsInit.text = "JS初始化中..."
        walletBusiness = WalletBusiness(this) {

            runOnUiThread {
                binding.jsInit.text = if (it) "JS初始化完成" else "JS未初始化"
            }
        }

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "请求数据中...", Snackbar.LENGTH_LONG).setAction("Action", null)
                .show()

            testNpmServiceAsync()
//            testNpmServiceSync()
        }
    }

    private fun testNpmServiceAsync() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 初始化钱包
//                walletBusiness.initializeWallet {
//                    Log.d(TAG,"initialize wallet: $it")
//                }

//                walletBusiness.callJsFunctionAsync("DebeemWallet", "getCurrentChain") { result ->
//                    Log.e(TAG, "getCurrentChain: $result")
//
//                    runOnUiThread {
//                        binding.jsResultTv.text = result
//                    }
//                }
//
//                walletBusiness.callJsFunctionAsync(
//                    "DebeemWallet",
//                    "WalletFactory.isValidWalletFactoryData"
//                ) { result ->
//                    Log.e(TAG, "WalletFactory.isValidWalletFactoryData: $result")
//
//                    runOnUiThread {
//                        binding.jsResultTv.text = result
//                    }
//                }
//
//                walletBusiness.createCallJsFunctionAsync(
//                    "DebeemWallet",
//                    "WalletAccount",
//                    emptyList(),
//                    "queryPairPrice",
//                    listOf("BTC/USD")
//                ) { result ->
//                    Log.e(TAG, "WalletAccount.queryPairPrice: $result")
//
//                    runOnUiThread {
//                        binding.jsResultTv.text = result
//                    }
//                }
//
//                walletBusiness.createCallJsFunctionAsync(
//                    "DebeemWallet",
//                    "ChainService",
//                    emptyList(),
//                    "exists",
//                    listOf(1)
//                ) { result ->
//                    Log.e(TAG, "ChainService.exists: $result")
//
//                    runOnUiThread {
//                        binding.jsResultTv.text = result
//                    }
//                }

//                walletBusiness.createCallJsFunctionAsync(
//                    "DebeemWallet",
//                    "TokenService",
//                    listOf(11155111),
//                    "nativeTokenAddress",
//                    emptyList(),
//                ) { result ->
//                    Log.e(TAG, "TokenService.nativeTokenAddress: $result")
//
//                    runOnUiThread {
//                        binding.jsResultTv.text = result
//                    }
//                }

//                walletBusiness.createCallJsFunctionAsync(
//                    "DebeemWallet",
//                    "WalletStorageService",
//                    emptyList(),
//                    "getByCurrentWallet",
//                    emptyList(),
//                ) { result ->
//                    Log.e(TAG, "WalletStorageService.getByCurrentWallet: $result")
//
//                    runOnUiThread {
//                        binding.jsResultTv.text = result
//                    }
//                }

//                walletBusiness.callJsFunctionAsync(
//                    "DebeemWallet",
//                    "getCurrentWalletAsync"
//                ) { result ->
//                    Log.e(TAG, "getCurrentWalletAsync: $result")
//
//                    runOnUiThread {
//                        binding.jsResultTv.text = result
//                    }
//                }


                // custom script
                val label = "custom_test"
//                val script = """
//                (function(){
//                    const execute = async () => {
//                            try {
//                                const walletAccount = new DebeemWallet.WalletAccount();
//                                const result = await walletAccount.queryPairPrice('BTC/USD');
//                                return { success: true, data: serializable(result) };
//                            } catch (error) {
//                                return { success: false, error: error.toString() };
//                            }
//                        };
//
//                        execute().then(result => {
//                            window.Android.handleResult(`${label}`, JSON.stringify(result));
//                        });
//                })();
//            """.trimIndent()

//                val script = """
//                (function(){
//                    const execute = async () => {
//                            try {
//                                const walletName = `MyWalletTestByJimmy`;
//                                const chainId = 1;
//                                const pinCode = '111111'
//                                const walletObject = DebeemId.EtherWallet.createWalletFromMnemonic( `olympic cradle tragic crucial exit annual silly cloth scale fine gesture ancient` );
//
//                                const toBeCreatedWalletItem = {
//                                    ...walletObject,
//                                    name : walletName,
//                                    chainId : chainId,
//                                    pinCode : ``
//                                };
//                                const created = await DebeemWallet.initWalletAsync( toBeCreatedWalletItem, pinCode, true );
//
//                                const walletStorage = new DebeemWallet.WalletStorageService( pinCode );
//
//                                const itemKey = walletStorage.getKeyByItem( walletObject );
//
//                                const value = await walletStorage.get( itemKey );
//
//                                const result = value;
//                                return { success: true, data: result };
//                            } catch (error) {
//                                return { success: false, error: error.toString() };
//                            }
//                        };
//
//                        execute().then(result => {
//                            window.Android.handleResult(`${label}`, JSON.stringify(result));
//                        });
//                })();
//            """.trimIndent()

                val script = """
                (function(){
                    const execute = async () => {
                            try {
                                const pinCode = '111111'
                                const walletObject = DebeemId.EtherWallet.createWalletFromMnemonic( `olympic cradle tragic crucial exit annual silly cloth scale fine gesture ancient` );
                                const walletStorage = new DebeemWallet.WalletStorageService( pinCode );

                                const itemKey = walletStorage.getKeyByItem( walletObject );

                                const value = await walletStorage.get( itemKey );

                                const result = value;
                                return { success: true, data: result };
                            } catch (error) {
                                return { success: false, error: error.toString() };
                            }
                        };

                        execute().then(result => {
                            window.Android.handleResult(`${label}`, JSON.stringify(result));
                        });
                })();
            """.trimIndent()

//            Log.d(TAG, "custom script: $script")
                walletBusiness.customScript(label, script) { result ->
                    Log.e(TAG, "customScript result: $result")

                    runOnUiThread {
                        binding.jsResultTv.text = result
                    }
                }

            } catch (e: Exception) {
                println("Error: ${e.message}")
            }


        }

        // 调用带各种参数和回调的 JavaScript 方法
//        npmServiceSDK.callJsFunctionAsync(
//            "callJsWithVariousParams",
//            42,
//            3.14f,
//            "Hello from Kotlin",
//            mapOf("key1" to "value1", "key2" to 2),
//            listOf("item1", "item2", "item3")
//        ) { result ->
//            Log.d("MainActivity", "000 Received result from JS: $result")
//        }

        // 异步
//        npmServiceSDK.callJsFunctionAsync("callJsWithCallback", "000 hello") { result ->
//            Log.d("MainActivity", "000 Received result from JS: $result")
//
//            runOnUiThread {
//                binding.jsResultTv.text = result
//            }
//        }

//        npmServiceSDK.callJsFunctionAsync("queryPairPrice", "BTC/USD") { result ->
//            Log.d("queryPairPrice", "$result")
//
//            runOnUiThread {
//                binding.jsResultTv.text = result
//            }
//        }
//
//        // 同步
//        npmServiceSDK.callJsFunctionSync("getCurrentChain") { result ->
//            Log.d("MainActivity", "result: $result")
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}