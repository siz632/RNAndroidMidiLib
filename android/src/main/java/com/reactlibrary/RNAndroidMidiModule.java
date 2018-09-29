
package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.util.MidiProcessor;

import java.io.File;
import java.io.IOException;

public class RNAndroidMidiModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  private MidiFile midiFile;
  private MidiTrack track;
  private MidiProcessor processor;

  public RNAndroidMidiModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNAndroidMidiModule";
  }

  @ReactMethod
  public void loadMidiFile(String filename) {

      File input = new File(filename);

      try {
          this.midiFile = new MidiFile(input);
      } catch (IOException e) {
          System.out.println("Error occurred while opening file " + filename);
          e.printStackTrace();
      }
  }

  @ReactMethod
  public void getFirstTrack() {
      this.track = midiFile.getTracks().get(1);
      System.out.println("Midi track: " + track.toString());
  }

}