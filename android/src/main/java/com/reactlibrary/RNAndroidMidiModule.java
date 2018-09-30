
package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.util.MidiProcessor;

import android.os.SystemClock;
import android.util.Log;

import org.billthefarmer.mididriver.MidiDriver;

import java.io.File;
import java.io.IOException;

public class RNAndroidMidiModule extends ReactContextBaseJavaModule implements MidiDriver.OnMidiStartListener {

  private final ReactApplicationContext reactContext;

  private MidiFile midi;
  private MidiTrack track;
  private MidiProcessor processor;

    private MidiDriver midiDriver;
    private byte[] event;
    private int[] config;


    public RNAndroidMidiModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNAndroidMidiModule";
  }

  @ReactMethod
  public void doSomeMidi() {
      MidiDriver midiDriver = new MidiDriver();
      midiDriver.setOnMidiStartListener(this);

      // Get the configuration.
      config = midiDriver.config();
      // Print out the details.
      Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
      Log.d(this.getClass().getName(), "numChannels: " + config[1]);
      Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
      Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);

      this.selectInstrument(1);


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