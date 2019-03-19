
package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.util.MidiProcessor;

import org.billthefarmer.mididriver.MidiDriver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class RNAndroidMidiModule extends ReactContextBaseJavaModule implements MidiDriver.OnMidiStartListener, ScopeLogger {

    private final ReactApplicationContext reactContext;

    private MidiManager mMidiManager;
    private MidiOutputPortSelector mLogSenderSelector;

    private MidiFile midi;
    private MidiTrack track;
    private MidiProcessor processor;

    private MidiDriver midiDriver;
    private byte[] event;
    private int[] config;

    MidiManager midiManager;
    MidiFramer mConnectFramer;
    MidiDeviceInfo[] infos;

    private IntentFilter intentFilter;
    private BroadcastReceiver receiver;

    public RNAndroidMidiModule(ReactApplicationContext reactContext) {
    super(reactContext);
//    initializeBroadcastReceiver();
    this.reactContext = reactContext;
        midiDriver = new MidiDriver();
        midiDriver.setOnMidiStartListener(this);
        this.midiManager = (MidiManager) reactContext.getSystemService(reactContext.MIDI_SERVICE);

        // Receiver that prints the messages.
        LoggingReceiver mLoggingReceiver = new LoggingReceiver(this);

        // Receivers that parses raw data into complete messages.
        this.mConnectFramer = new MidiFramer(mLoggingReceiver);

//        // Setup a menu to select an input source.
//        mLogSenderSelector = new MidiOutputPortSelector(mMidiManager, this,
//                R.id.spinner_senders) {
//
//            @Override
//            public void onPortSelected(final MidiPortWrapper wrapper) {
//                super.onPortSelected(wrapper);
//                if (wrapper != null) {
//                    log(MidiPrinter.formatDeviceInfo(wrapper.getDeviceInfo()));
//                }
//            }
//        };

        MyDirectReceiver mDirectReceiver = new MyDirectReceiver();
//        mLogSenderSelector.getSender().connect(mDirectReceiver);
  }

    class MyDirectReceiver extends MidiReceiver {
        @Override
        public void onSend(byte[] data, int offset, int count,
                           long timestamp) throws IOException {
//            if (mShowRaw) {
//                String prefix = String.format("0x%08X, ", timestamp);
//                logByteArray(prefix, data, offset, count);
//            }
            // Send raw data to be parsed into discrete messages.
            mConnectFramer.send(data, offset, count, timestamp);
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
        File file = reactContext.getFilesDir();
        String path = file.getPath();
        successCallback.invoke(path);
    }

    /**
     * @param string
     */
    @Override
    public void log(final String string) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                logFromUiThread(string);
//            }
//        });
    }

    @ReactMethod
    public void playMidi(String filename, int tempo, Callback successCallback, Callback errorCallback) {
        if (tempo <= 0) {
            tempo = 120;
        }
        try {

            midiDriver.start();

            InputStream is = reactContext.getResources().openRawResource(
                    reactContext.getResources().getIdentifier("rolnik", "raw", reactContext.getPackageName())
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
}