// index.js
import './business.js';

// Initialize function to be called from Kotlin
window.initialize = (initialized = true, callback) => {
    const init = async () => {
        try {
            console.log('initialize(' + initialized + ')');
            if (initialized) {
            } else {
            }
            return { success: true, message: "Initialized successfully", initialized: initialized };
        } catch (error) {
            return { success: false, error: error.toString() };
        }
    };

    init().then(result => {
        window.Android.handleResult("initialize", JSON.stringify(result));
    });
};
