/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reactlibrary;

import android.app.Activity;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.media.midi.MidiManager.DeviceCallback;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.HashSet;

/**
 * Base class that uses a Spinner to select available MIDI ports.
 */
public abstract class MidiPortSelector extends DeviceCallback {
    private int mType = MidiDeviceInfo.PortInfo.TYPE_INPUT;
    protected HashSet<MidiPortWrapper> mBusyPorts = new HashSet<MidiPortWrapper>();
    protected MidiManager mMidiManager;
    private MidiPortWrapper mCurrentWrapper;

    public MidiPortSelector(MidiManager midiManager, int type) {
        mMidiManager = midiManager;
        mType = type;
//        mSpinner = (Spinner) activity.findViewById(spinnerId);

        MidiDeviceMonitor.getInstance(mMidiManager).registerDeviceCallback(this,
                new Handler(Looper.getMainLooper()));

        MidiDeviceInfo[] infos = mMidiManager.getDevices();
        for (MidiDeviceInfo info : infos) {
            onDeviceAdded(info);
        }
    }

    /**
     * Set to no port selected.
     */
    private int getInfoPortCount(final MidiDeviceInfo info) {
        int portCount = (mType == MidiDeviceInfo.PortInfo.TYPE_INPUT)
                ? info.getInputPortCount() : info.getOutputPortCount();
        return portCount;
    }

    @Override
    public void onDeviceAdded(final MidiDeviceInfo info) {
        int portCount = getInfoPortCount(info);
        for (int i = 0; i < portCount; ++i) {
            MidiPortWrapper wrapper = new MidiPortWrapper(info, mType, i);
            Log.i(MidiConstants.TAG, wrapper + " was added to " + this);
        }
    }

    @Override
    public void onDeviceRemoved(final MidiDeviceInfo info) {
        int portCount = getInfoPortCount(info);
        for (int i = 0; i < portCount; ++i) {
            MidiPortWrapper wrapper = new MidiPortWrapper(info, mType, i);
            MidiPortWrapper currentWrapper = mCurrentWrapper;
            // If the currently selected port was removed then select no port.
            Log.i(MidiConstants.TAG, wrapper + " was removed");
        }
    }

    @Override
    public void onDeviceStatusChanged(final MidiDeviceStatus status) {
        // If an input port becomes busy then remove it from the menu.
        // If it becomes free then add it back to the menu.
        if (mType == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
            MidiDeviceInfo info = status.getDeviceInfo();
            Log.i(MidiConstants.TAG, "MidiPortSelector.onDeviceStatusChanged status = " + status
                    + ", mType = " + mType
                    + ", info = " + info);
            // Look for transitions from free to busy.
            int portCount = info.getInputPortCount();
            for (int i = 0; i < portCount; ++i) {
                MidiPortWrapper wrapper = new MidiPortWrapper(info, mType, i);
                if (!wrapper.equals(mCurrentWrapper)) {
                    if (status.isInputPortOpen(i)) { // busy?
                        if (!mBusyPorts.contains(wrapper)) {
                            // was free, now busy
                            mBusyPorts.add(wrapper);
                        }
                    } else {
                        if (mBusyPorts.remove(wrapper)) {
                            // was busy, now free
                        }
                    }
                }
            }
        }
    }

    /**
     * Implement this method to handle the user selecting a port on a device.
     *
     * @param wrapper
     */
    public abstract void onPortSelected(MidiPortWrapper wrapper);

    /**
     * Implement this method to clean up any open resources.
     */
    public void onClose() {
    }

    /**
     * Implement this method to clean up any open resources.
     */
    public void onDestroy() {
        MidiDeviceMonitor.getInstance(mMidiManager).unregisterDeviceCallback(this);
    }

    /**
     *
     */
    public void close() {
        onClose();
    }
}
