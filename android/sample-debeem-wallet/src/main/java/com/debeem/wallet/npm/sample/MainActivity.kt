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

    private lateinit var walletBusiness: WalletBusiness

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.jsInit.text = "JS initializing..."
        walletBusiness = WalletBusiness(this) {

            runOnUiThread {
                binding.jsInit.text = if (it) {
                    binding.getCurrentChain.isEnabled = true
                    binding.nativeTokenAddress.isEnabled = true
                    binding.queryPairPrice.isEnabled = true
                    binding.initWalletAsync.isEnabled = true
                    binding.walletStorage.isEnabled = true
                    binding.clearJSData.isEnabled = true
                    "JS Initialization complete"
                }
                else
                    "JS Uninitialized"
            }
        }

        getCurrentChainView()
        nativeTokenAddressView()
        queryPairPriceView()
        initWalletAsyncView()
        walletStorageView()
        clearView()
    }

    override fun onDestroy() {
        super.onDestroy()
        walletBusiness.dispose()
    }

    private fun clearView() {
        binding.clearJSData.setOnClickListener {
            binding.jsResultTv.text = "clearing..."
            CoroutineScope(Dispatchers.Main).launch {
                val label = "custom_test_clear"
                val script = """
                (function(){
                        const execute = async () => {
                            try {
                                const pinCode = '111111'
                                const walletStorage = new DebeemWallet.WalletStorageService( pinCode );
                                
                                const result = await walletStorage.clear();
                                
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

                walletBusiness.customScript(label, script) { result ->
                    Log.e(TAG, "customScript result: $result")

                    runOnUiThread {
                        binding.jsResultTv.text = result
                    }
                }
            }
        }
    }

    private fun getCurrentChainView() {
        binding.getCurrentChain.setOnClickListener {
            binding.jsResultTv.text = "loading..."
            CoroutineScope(Dispatchers.Main).launch {
                val label = "custom_test_get_chain"
                val script = """
                (function(){
                    const execute = async () => {
                            try {
                                const result = DebeemWallet.getCurrentChain();
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

                walletBusiness.customScript(label, script) { result ->
                    Log.e(TAG, "customScript result: $result")

                    runOnUiThread {
                        binding.jsResultTv.text = result
                    }
                }
            }
        }
    }

    private fun nativeTokenAddressView() {
        binding.nativeTokenAddress.setOnClickListener {
            binding.jsResultTv.text = "loading..."
            CoroutineScope(Dispatchers.Main).launch {
                val label = "custom_test_token_address"
                val script = """
                (function(){
                    const execute = async () => {
                            try {
                                const result = await new DebeemWallet.TokenService().nativeTokenAddress;
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

                walletBusiness.customScript(label, script) { result ->
                    Log.e(TAG, "customScript result: $result")

                    runOnUiThread {
                        binding.jsResultTv.text = result
                    }
                }
            }
        }
    }

    private fun queryPairPriceView() {
        binding.queryPairPrice.setOnClickListener {
            binding.jsResultTv.text = "loading..."
            CoroutineScope(Dispatchers.Main).launch {
                val label = "custom_test_1"
                val script = """
                (function(){
                    const execute = async () => {
                            try {
                                const priceObj = await new DebeemWallet.WalletAccount().queryPairPrice( `BTC/USD` );
                                const result = priceObj;
                                return { success: true, data: serializable(result) };
                            } catch (error) {
                                return { success: false, error: error.toString() };
                            }
                        };

                        execute().then(result => {
                            window.Android.handleResult(`${label}`, JSON.stringify(result));
                        });
                })();
            """.trimIndent()

                walletBusiness.customScript(label, script) { result ->
                    Log.e(TAG, "customScript result: $result")

                    runOnUiThread {
                        binding.jsResultTv.text = result
                    }
                }
            }
        }
    }

    private fun initWalletAsyncView() {
        binding.initWalletAsync.setOnClickListener {
            binding.jsResultTv.text = "loading..."
            CoroutineScope(Dispatchers.Main).launch {
                val label = "custom_test_2"
                val script = """
                (function(){
                    const execute = async () => {
                            try {
                                const walletName = `MyWalletTestByJimmy`;
                                const chainId = 1;
                                const pinCode = '111111'
                                const walletObject = DebeemId.EtherWallet.createWalletFromMnemonic( `olympic cradle tragic crucial exit annual silly cloth scale fine gesture ancient` );

                                const toBeCreatedWalletItem = {
                                    ...walletObject,
                                    name : walletName,
                                    chainId : chainId,
                                    pinCode : ``
                                };
                                const created = await DebeemWallet.initWalletAsync( toBeCreatedWalletItem, pinCode, true );
                                
                                const result = created;
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

                walletBusiness.customScript(label, script) { result ->
                    Log.e(TAG, "customScript result: $result")

                    runOnUiThread {
                        binding.jsResultTv.text = result
                    }
                }
            }
        }
    }

    private fun walletStorageView() {
        binding.walletStorage.setOnClickListener {
            binding.jsResultTv.text = "loading..."
            CoroutineScope(Dispatchers.Main).launch {
                val label = "custom_test_3"
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

                walletBusiness.customScript(label, script) { result ->
                    Log.e(TAG, "customScript result: $result")

                    runOnUiThread {
                        binding.jsResultTv.text = result
                    }
                }
            }
        }
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