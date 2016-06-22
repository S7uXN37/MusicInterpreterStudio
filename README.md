# MusicInterpreterStudio
MusicInterpreter project, adapted for Android Studio

Here's how it works:

1. Downlaod and install the APK file https://github.com/S7uXN37/MusicInterpreterStudio/raw/master/app/app-release.apk (Yes, it's required that you turn on "apps from unknown sources" in your settings in order to install this - but by all means turn it off again when it's installed, and yes, this doesn't work for non-Android devices like iPhones - you're SOL)

2. Open the MusicInterpreter app

3. Select a short MP3, which:
  - has a bitrate of 44100
  - contains only one melody w/o any chords -> only one note at a time
  - should start and end with silence

4. In the next screen, press "Read file" and make sure you're seeing something resembling a waveform - this step might take some time...

5. Moving on, you can adjust some parameters:

  - Sensitivity: how far to look for new notes; avoids notes close together, higher means fewer notes

  - Threshold: how loud a note has to be; avoids fake notes in the noise, higher means notes must be louder

  - Window size: specify how long the shortest note is; adjust such that no two red regions overlap, bigger windows means more accuracy

6. Now each peak in the waveform (start of a note) should have a red vertical line through it

7. In the last screen, press "Analyze". The frequencies of the notes are now being determined. When that's done, you'll see the results below and you can also play back / pause / stop the audio using the buttons on the right


Note: It may be possible that groups of notes are in the wrong octave, you should be able to identify this by listening to the audio though
