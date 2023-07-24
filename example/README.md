# Android Voice Processor Example

This is an example app that demonstrates how to ask for user permissions and capture output from
the `VoiceProcessor`.

## Requirements

- Java SDK (11+)
- Android SDK (21+)

## Compatibility

- Android 5.0+ (API 21+)

## Building

Build with gradle:
```console
cd example
./gradlew assembleDebug
```

Or open with Android Studio and run `android-voice-processor-example`.

## Usage

Toggle recording on and off with the button in the center of the screen. While recording, the VU meter on the screen will respond to the volume of incoming audio.

## Running the Unit Tests
Ensure you have an Android device connected or simulator running. 
Then run the following from the terminal:
```console
cd example
./gradlew connectedAndroidTest
```

The test results are stored in `android-voice-processor-example/build/reports`.
