// src/index.js

// 首先确保所有需要的包都已经正确导入和挂载到 window 对象上
import * as DebeemWallet from 'debeem-wallet';
window.DebeemWallet = DebeemWallet;
import * as DebeemId from 'debeem-id';
window.DebeemId = DebeemId;
import * as DebeemCipher from 'debeem-cipher';
window.DebeemCipher = DebeemCipher;
import * as Ethers from 'ethers';
window.Ethers = Ethers;
import * as Idb from 'idb';
window.Idb = Idb;
import * as FakeIndexeddb from 'fake-indexeddb';
window.FakeIndexeddb = FakeIndexeddb;

import { serializable } from './utils';
window.serializable = serializable;

// Initialize function to be called from Kotlin
window.initializeWallet = (initDB = true, callback) => {
    const init = async () => {
        try {
            console.log('initializeWallet(' + initDB + ')');
            if (initDB) {

            } else {

            }

            return { success: true, message: "Wallet initialized successfully", dbInitialized: initDB };
        } catch (error) {
            return { success: false, error: error.toString() };
        }
    };

    init().then(result => {
        window.WalletBridge.handleResult("initializeWallet", JSON.stringify(result));
    });
};

// Helper function to clear cache
window.clearWalletCache = (callback) => {
    const clear = async () => {
        if (dbWrapper) {
            try {
                await dbWrapper.clear();
                return { success: true, message: "Cache cleared successfully" };
            } catch (error) {
                return { success: false, error: error.toString() };
            }
        } else {
            return { success: false, error: "Database not initialized" };
        }
    };

    clear().then(result => {
        window.WalletBridge.handleResult("clearWalletCache", JSON.stringify(result));
    });
};

// 通用方法: 调用npm的静态方法或全局函数
window.callNpmMethod = (packageName, methodPath, args, callback) => {
    const call = async () => {
        try {
            const pkg = window[packageName];
            if (!pkg) {
                throw new Error(`Package ${packageName} not found`);
            }

            // Split the method path into parts
            const parts = methodPath.split('.');
            let current = pkg;

            // Traverse the object hierarchy
            for (let i = 0; i < parts.length - 1; i++) {
                current = current[parts[i]];
                if (!current) {
                    throw new Error(`${parts.slice(0, i + 1).join('.')} not found in package ${packageName}`);
                }
            }

            const method = current[parts[parts.length - 1]];
            if (typeof method !== 'function') {
                throw new Error(`Method ${methodPath} not found in package ${packageName}`);
            }

            const result = await method.apply(current, args);
            return { success: true, data: result };
        } catch (error) {
            return { success: false, error: error.toString() };
        }
    };

    call().then(result => {
        if (typeof callback === 'function') {
            callback(result);
        } else if (window.WalletBridge && typeof window.WalletBridge.handleResult === 'function') {
            // If no callback is provided but WalletBridge exists, use it
            window.WalletBridge.handleResult(`${packageName}.${methodPath}`, JSON.stringify(result));
        } else {
            console.log(`Result from ${packageName}.${methodPath}:`, result);
        }
    });
};

// 通用方法用于创建类的实例并调用其方法
window.createAndCallMethod = (packageName, className, constructorArgs, methodName, methodArgs, callback) => {
    const execute = async () => {
        try {
            const pkg = window[packageName];
            if (!pkg) {
                throw new Error(`Package ${packageName} not found`);
            }

            const ClassConstructor = pkg[className];
            if (typeof ClassConstructor !== 'function') {
                throw new Error(`Class ${className} not found in package ${packageName}`);
            }

            // Create an instance of the class using the default constructor.
            let instance;
            try {
                if (Array.isArray(constructorArgs) && constructorArgs.length > 0) {
                    instance = new ClassConstructor(...constructorArgs);
                } else if (constructorArgs === undefined || constructorArgs === null) {
                    instance = new ClassConstructor();
                } else {
                    instance = new ClassConstructor(constructorArgs);
                }

                if (!instance) {
                    throw new Error(`Failed to create instance of ${className}`);
                }
            } catch (constructorError) {
                console.error(`Error creating instance of ${className}:`, constructorError);
                throw new Error(`Failed to create instance of ${className}: ${constructorError.message}`);
            }

//            console.log(`Accessing ${methodName} on instance of ${className}`);
            let result;
            if (methodName in instance) {
                if (typeof instance[methodName] === 'function') {
                    // If it's a method, call it
//                    console.log(`Calling method ${methodName} with args:`, methodArgs);
                    result = await instance[methodName].apply(instance, methodArgs || []);
                } else {
                    // if it's a property, get its value directly
//                    console.log(`Getting property ${methodName}`);
                    result = instance[methodName];
                }
            } else {
                throw new Error(`Property or method ${methodName} not found in class ${className}`);
            }
            return { success: true, data: serializable(result) };
        } catch (error) {
            return { success: false, error: error.toString() };
        }
    };

    execute().then(result => {
        if (typeof callback === 'function') {
            callback(result);
        } else if (window.WalletBridge && typeof window.WalletBridge.handleResult === 'function') {
            // 如果没有提供回调，但存在 WalletBridge，则使用它
            window.WalletBridge.handleResult(`${packageName}.${className}.${methodName}`, JSON.stringify(result));
        } else {
            console.log(`Result from ${packageName}.${className}.${methodName}:`, result);
        }
    });
};

// BTC/USD
//window.queryPairPrice = async function(pair, callback) {
//    try {
//        console.log('queryPairPrice(' + pair + ')');
////        const mnemonic = 'frog science fold balance climb resist torch egg clay spell silk emotion';
////        const walletObj = new WalletFactory().createWalletFromMnemonic( mnemonic );
////        console.error('walletObj address:', walletObj.address);
////        const balance = await new WalletAccount().queryBalance( walletObj.address );
////        console.error('walletObj balance:', balance);
////
////        // switch chain/network to Eth.Sepolia
////        setCurrentChain( 11155111 );
////        const chainId = getCurrentChain();
////        console.error('getCurrentChain chainId after:', chainId);
//
//        const walletAccount = new WalletAccount();
//        const priceObj = await walletAccount.queryPairPrice(pair);
//        const jsonString = JSON.stringify(priceObj, replacer);
//        console.log(jsonString);
//        callback(jsonString);
//    } catch (error) {
//        console.error('Error querying pair price:', error);
//        return null;
//    }
//}
//
//window.getCurrentChain = function() {
//    const chainId = getCurrentChain();
//    console.log('chainId: ' + chainId);
//    return chainId
//}
//
//// 暴露一个全局函数来在 WebView 中调用
//// 写同步方法时，不需要传递 callbackScript
//window.callJsWithCallbackSync = function(data) {
//    console.log("callJsWithCallbackSync: JS method called with parameters: " + data);
//    // 执行一些操作
//    var result = "Result from JS for data: " + data;
//    // 直接返回结果
//    return result;
//}
//
//// 写异步方法时，需要传递 callbackScript
//window.callJsWithCallback = function(data, callback) {
//    console.log("callJsWithCallback: JS method called with parameters: " + data);
//    // 执行一些操作
//    var result = "ttt Result from JS for data: " + data;
//    // 调用回调函数并传递结果
//    callback(result);
//}
//
//window.callJsWithVariousParams = function(intParam, floatParam, stringParam, mapParam, arrayParam, callback) {
//    console.log("hhh JS method called with parameters ");
//
////    console.log("hhh JS method called with parameters: ", intParam, floatParam, stringParam, mapParam, arrayParam);
//    // 模拟处理参数
//    var result = {
//        intResult: intParam + 1,
//        floatResult: floatParam + 1.1,
//        stringResult: stringParam + " from JS",
//        mapResult: Object.assign({}, mapParam, { fromJs: true }),
//        arrayResult: arrayParam.map(item => item + " from JS")
//    };
//    // 调用回调函数并传递结果
//    callback(JSON.stringify(result));
//}