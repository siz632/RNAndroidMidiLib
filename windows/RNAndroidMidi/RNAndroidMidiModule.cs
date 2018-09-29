using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Android.Midi.RNAndroidMidi
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNAndroidMidiModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNAndroidMidiModule"/>.
        /// </summary>
        internal RNAndroidMidiModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNAndroidMidi";
            }
        }
    }
}
