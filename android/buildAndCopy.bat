call gradlew build
robocopy "c:\astudio\AndroidMidi\android" "c:\astudio\rnAndroidMidiExample\node_modules\react-native-android-midi\android" /S /XO /XD ".idea" ".gradle" /XF "*.bat" > nul