package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class MsgManager {

    private LocalBroadcastReceiver lastMsgLocalBroadcastReceiver;
    private LocalKeyBroadcastReceiver lastKeyLocalBroadcastReceiver;
    private ReactApplicationContext mReactContext;

    MsgManager(ReactApplicationContext mReactContext) {
        this.mReactContext = mReactContext;
        this.lastMsgLocalBroadcastReceiver = new LocalBroadcastReceiver(mReactContext, "onSentMsgEvent");
        this.lastKeyLocalBroadcastReceiver = new LocalKeyBroadcastReceiver(mReactContext, "onKeyPressed");

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mReactContext);
        localBroadcastManager.registerReceiver(lastMsgLocalBroadcastReceiver, new IntentFilter("onSentMsgEvent"));
        localBroadcastManager.registerReceiver(lastKeyLocalBroadcastReceiver, new IntentFilter("onKeyPressed"));
    }

    public void emitMsg(String errorMessage) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mReactContext);
        Intent customEvent = new Intent("onSentMsgEvent");
        customEvent.putExtra("my-extra-data", errorMessage);
        localBroadcastManager.sendBroadcast(customEvent);
    }

    public void emitKeyPressed(String errorMessage) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mReactContext);
        Intent customEvent = new Intent("onKeyPressed");
        customEvent.putExtra("my-extra-key", errorMessage);
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

    public class LocalKeyBroadcastReceiver extends BroadcastReceiver {
        private String eventName;
        private ReactApplicationContext mReactContext;

        LocalKeyBroadcastReceiver(ReactApplicationContext mReactContext, String eventName) {
            this.eventName = eventName;
            this.mReactContext = mReactContext;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String someData = intent.getStringExtra("my-extra-key");
            mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, someData);
        }
    }

}
