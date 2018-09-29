
# react-native-android-midi

## Getting started

`$ npm install react-native-android-midi --save`

### Mostly automatic installation

`$ react-native link react-native-android-midi`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-android-midi` and add `RNAndroidMidi.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNAndroidMidi.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNAndroidMidiPackage;` to the imports at the top of the file
  - Add `new RNAndroidMidiPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-android-midi'
  	project(':react-native-android-midi').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-android-midi/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-android-midi')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNAndroidMidi.sln` in `node_modules/react-native-android-midi/windows/RNAndroidMidi.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Android.Midi.RNAndroidMidi;` to the usings at the top of the file
  - Add `new RNAndroidMidiPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNAndroidMidi from 'react-native-android-midi';

// TODO: What to do with the module?
RNAndroidMidi;
```
  "# RNAndroidMidiLib" 
