
package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;

import org.billthefarmer.mididriver.MidiDriver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class RNAndroidMidiModule extends ReactContextBaseJavaModule implements MidiDriver.OnMidiStartListener, ScopeLogger {

    private final ReactApplicationContext mReactContext;
    public final static String TAG = "rnAndroidMidiModule";

    private MidiDriver midiDriver;
    private byte[] event;
    private int[] config;

    MidiDevice midiDevice = null;
    MidiManager midiManager;
    MidiDeviceInfo[] infos;
    MidiOutputPort outputPort;
    MsgManager msgM;

    public RNAndroidMidiModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mReactContext = reactContext;
        this.msgM = new MsgManager(mReactContext);

        midiDriver = new MidiDriver();
        midiDriver.setOnMidiStartListener(this);
        this.midiManager = (MidiManager) reactContext.getSystemService(reactContext.MIDI_SERVICE);
    }

    public class LocalBroadcastReceiver extends BroadcastReceiver {
        private String eventName;

        LocalBroadcastReceiver(String eventName) {
            this.eventName = eventName;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String someData = intent.getStringExtra("my-extra-data");
            mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, someData);
        }
    }

//    class MyDirectReceiver extends MidiReceiver {
//        @Override
//        public void onSend(byte[] data, int offset, int count,
//                           long timestamp) throws IOException {
//    //            if (mShowRaw) {
//    //                String prefix = String.format("0x%08X, ", timestamp);
//    //                logByteArray(prefix, data, offset, count);
//    //            }
//            // Send raw data to be parsed into discrete messages.
//            keyboardReceiver.send(data, offset, count, timestamp);
//        }
//    }
    @ReactMethod
    public void selectMidiDevice(Integer deviceNo) {
        String deviceName = midiManager.getDevices()[deviceNo].getProperties().getString("name");
        String deviceProduct = midiManager.getDevices()[deviceNo].getProperties().getString("product");
        Log.e("RNAndroidMidiModule", "Android selecting the device: " + deviceNo);

//        if (deviceName.contains("Yamaha") || deviceProduct.contains("Yamaha")) { // to check the nae of the deive...
            openDevice(deviceNo);
            msgM.emitMsg("Opened a port on device..." + deviceName);
//        }
    }

    private void openDevice(final Integer deviceNo) {
        final String deviceName = midiManager.getDevices()[deviceNo].getProperties().getString("name");
        Log.e(TAG, "Android opening device... " + deviceNo);
        msgM.emitMsg("Android opening device... " + deviceNo);
        try {
            if (this.midiManager.getDevices().length > deviceNo) {
                Log.e(TAG, "Opening device: " + deviceName);
                this.midiManager.openDevice(
                        this.midiManager.getDevices()[deviceNo],
                        new MidiManager.OnDeviceOpenedListener() {
                            @Override
                            public void onDeviceOpened(MidiDevice device) {
                                msgM.emitMsg("onDeviceOpened is executing...");
                                if (device == null) {
                                    Log.e(TAG, "Could not open device " + deviceName);
                                    msgM.emitMsg("Could not open device " + deviceName);
                                } else {
                                    midiDevice = device;
                                    if (midiManager.getDevices()[deviceNo].getOutputPortCount() > 0) {
                                        outputPort = device.openOutputPort(0);
                                        outputPort.connect(new KeyboardReceiver(msgM));
                                        Log.i(TAG, "Opened device " + deviceName);
                                        msgM.emitMsg("Opened device " + deviceName);
                                    } else {
                                        Log.i(TAG, "No output ports for the deice " + deviceName);
                                        msgM.emitMsg("No output ports for the deice " + deviceName);
                                    }
                                }
                            }
                        },
                        new Handler(Looper.getMainLooper())
                );
            } else {
                Log.e(TAG, "DeviceNo " + deviceNo + " is higher than no of devices!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not open device " + midiManager.getDevices()[deviceNo]);
        }
    }

    private void logByteArray(String prefix, byte[] value, int offset, int count) {
        StringBuilder builder = new StringBuilder(prefix);
        for (int i = 0; i < count; i++) {
            builder.append(String.format("0x%02X", value[offset + i]));
            if (i != count - 1) {
                builder.append(", ");
            }
        }
        log(builder.toString());
    }

    @Override
    public String getName() {
        return "RNAndroidMidiModule";
    }

    @ReactMethod
    public void getMidiDevices(Callback successCallback) {
        infos = this.midiManager.getDevices();
        StringBuilder devices = new StringBuilder("No devices");
        int i = 0;
        for (MidiDeviceInfo info : infos) {
            if (i == 0) {
                devices = new StringBuilder("");
            }
            devices.append("Device ").append(i).append(": ")
                    .append(/*info.toString() + */info.getProperties().getString("product"))
                    .append(" ")
                    .append(info.getProperties().getString("name"))
                    .append("\n\n");
            ++i;
        }
        successCallback.invoke(devices.toString());
    }

    @ReactMethod
    public void getMidiDevicesArray(final Promise promise) {
        try {
            infos = this.midiManager.getDevices();
            WritableArray devicesArray = Arguments.createArray();
            for (MidiDeviceInfo info : infos) {
                devicesArray.pushString(
                        info.getProperties().getString("product")
                                + " "
                                + info.getProperties().getString("name")
                                + " " + info.getOutputPortCount()
                );
            }
            promise.resolve(devicesArray);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void doSomeMidi() {

        //Get the configuration.
        config = midiDriver.config();
        Log.d(this.getClass().getName(), "getConfig: " + midiDriver.config());
        //Print out the details.
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);

        selectInstrument(1);

        this.playNote(60); //middle C
        SystemClock.sleep(1000);
        stopNote(60, false);

        this.playNote(62); //middle D
        SystemClock.sleep(1000);
        stopNote(62, false);

        this.playNote(64); //middle E
        SystemClock.sleep(1000);
        stopNote(64, false);

        this.playNote(65); //middle F
        SystemClock.sleep(1000);
        stopNote(65, false);

        this.playNote(67); //middle G
        SystemClock.sleep(1000);
        stopNote(67, false);

        this.playNote(69); //middle A
        SystemClock.sleep(1000);
        stopNote(69, false);

        this.playNote(71); //middle H/B
        SystemClock.sleep(1000);
        stopNote(71, false);

        this.playNote(72); //C2
        SystemClock.sleep(1000);
        stopNote(72, false);

        midiDriver.stop();

    }

    @ReactMethod
    public void getDir(Callback successCallback) {
        File file = mReactContext.getFilesDir();
        String path = file.getPath();
        successCallback.invoke(path);
    }

    @ReactMethod
    public void playMidi(String filename, int tempo, Callback successCallback, Callback errorCallback) {
        if (tempo <= 0) {
            tempo = 120;
        }
        try {
            midiDriver.start();
            InputStream is = mReactContext.getResources().openRawResource(
                    mReactContext.getResources().getIdentifier("rolnik", "raw", mReactContext.getPackageName())
            );
            MidiFile midi = new MidiFile(is);
            ArrayList<String> midiLog = new ArrayList<>();
            MidiTrack track = midi.getTracks().get(1);
            Iterator<MidiEvent> it = track.getEvents().iterator();
            float secondsPerMinute = 60f;
            float tempoFactor = secondsPerMinute / (float) tempo;

            while (it.hasNext()) {
                MidiEvent event = it.next();
                midiLog.add(event.toString());

                if ((event instanceof NoteOn)) {
                    SystemClock.sleep((long) ((float) event.getDelta() * tempoFactor));
                    playNote(((NoteOn) event).getNoteValue());
                    midiLog.add("playNote: " + ((NoteOn) event).getNoteValue());
                } else if ((event instanceof NoteOff)) {
                    SystemClock.sleep((long) ((float) event.getDelta() * tempoFactor));
                    stopNote(((NoteOff) event).getNoteValue(), false);
                    midiLog.add("stopNote: " + ((NoteOff) event).getNoteValue());
                } else if ((event instanceof Tempo)) {
                    midiLog.add("Tempo: " + ((Tempo) event).getBpm() + "BPM");
                }
            }
            midiDriver.stop();

            successCallback.invoke("Opened file rolink " +
                    "\n" + midiLog.toString());

            is.close();
        } catch (IOException e) {
            errorCallback.invoke(e.getMessage() + " file: " + filename);
            e.printStackTrace();
        }
    }

    private void selectInstrument(int instrument) {
        // Construct a program change to select the instrument on channel 1:
        event = new byte[2];
        event[0] = (byte)(0xC0 | 0x00); // 0xC0 = program change, 0x00 = channel 1
        event[1] = (byte)instrument;

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);
    }

    private void playNote(int noteNumber) {
        // Construct a note ON message for the note at maximum velocity on channel 1:
        event = new byte[3];
        event[0] = (byte) (0x90 | 0x00);  // 0x90 = note On, 0x00 = channel 1
        event[1] = (byte) noteNumber;
        event[2] = (byte) 0x7F;  // 0x7F = the maximum velocity (127)

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);
    }

    private void stopNote(int noteNumber, boolean sustainUpEvent) {
        // Stop the note unless the sustain button is currently pressed. Or stop the note if the
        // sustain button was depressed and the note's button is not pressed.
//        if (!buttonSustain.isPressed() || sustainUpEvent) {
            // Construct a note OFF message for the note at minimum velocity on channel 1:
            event = new byte[3];
            event[0] = (byte) (0x80 | 0x00);  // 0x80 = note Off, 0x00 = channel 1
            event[1] = (byte) noteNumber;
            event[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)

            // Send the MIDI event to the synthesizer.
            midiDriver.write(event);
//        }
    }

//  @ReactMethod
//  public void loadMidiFile(String filename) {
//      System.err.println("Loading file... " + filename);
//      File input = new File(filename);
//
//      try {
//          this.midi = new MidiFile(input);
//
//      } catch (IOException e) {
//          System.out.println("Error occurred while opening file " + filename);
//          e.printStackTrace();
//      }
//  }
//
//    public void loadMidiFileAndPlay(String filename) {
//        System.err.println("Loading file... " + filename);
//        File input = new File(filename);
//
//        try {
//            this.midi = new MidiFile(input);
//            MidiProcessor processor = new MidiProcessor(midi);
//
//        } catch (IOException e) {
//            System.out.println("Error occurred while opening file " + filename);
//            e.printStackTrace();
//        }
//    }
//
//  @ReactMethod
//  public void getFirstTrack() {
//      this.track = midi.getTracks().get(1);
//      System.out.println("Midi track: " + track.toString());
//  }

    @Override
    public void onMidiStart() {
        Log.d(this.getClass().getName(), "onMidiStart()");
    }

    protected void onPause() {
        midiDriver.stop();
    }

    @Override
    public void log(final String string) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                logFromUiThread(string);
//            }
//        });
    }
}

