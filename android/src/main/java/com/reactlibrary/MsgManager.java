package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class MsgManager {

    private LocalBroadcastReceiver mLocalBroadcastReceiver;
    private LocalBroadcastReceiver lastMsgLocalBroadcastReceiver;
    private ReactApplicationContext mReactContext;

    MsgManager(ReactApplicationContext mReactContext) {
        this.mReactContext = mReactContext;
        this.mLocalBroadcastReceiver = new LocalBroadcastReceiver(mReactContext, "onTestEvent");
        this.lastMsgLocalBroadcastReceiver = new LocalBroadcastReceiver(mReactContext, "onErrorMessageEvent");

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mReactContext);
        localBroadcastManager.registerReceiver(mLocalBroadcastReceiver, new IntentFilter("onTestEvent"));
        localBroadcastManager.registerReceiver(lastMsgLocalBroadcastReceiver, new IntentFilter("onErrorMessageEvent"));
    }

    public void emitErrorMessage(String errorMessage) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mReactContext);
        Intent customEvent = new Intent("onErrorMessageEvent");
        customEvent.putExtra("my-extra-data", errorMessage);
        localBroadcastManager.sendBroadcast(customEvent);
    }

    public void emitMessage(String message) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mReactContext);
        Intent customEvent = new Intent("onTestEvent");
        customEvent.putExtra("my-extra-data", message);
        localBroadcastManager.sendBroadcast(customEvent);
    }

    public class LocalBroadcastReceiver extends BroadcastReceiver {
        private String eventName;
        private ReactApplicationContext mReactContext;

        LocalBroadcastReceiver(ReactApplicationContext mReactContext, String eventName) {
            this.eventName = eventName;
            this.mReactContext = mReactContext;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String someData = intent.getStringExtra("my-extra-data");
            mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, someData);
        }
    }

}
