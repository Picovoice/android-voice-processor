# Android Voice Processor

[![GitHub release](https://img.shields.io/github/release/Picovoice/android-voice-processor.svg)](https://github.com/Picovoice/android-voice-processor/releases)
[![GitHub](https://img.shields.io/github/license/Picovoice/android-voice-processor)](https://github.com/Picovoice/android-voice-processor/)

[![Maven Central](https://img.shields.io/maven-central/v/ai.picovoice/android-voice-processor?label=maven-central)](https://repo1.maven.org/maven2/ai/picovoice/android-voice-processor/)

Made in Vancouver, Canada by [Picovoice](https://picovoice.ai)

<!-- markdown-link-check-disable -->
[![Twitter URL](https://img.shields.io/twitter/url?label=%40AiPicovoice&style=social&url=https%3A%2F%2Ftwitter.com%2FAiPicovoice)](https://twitter.com/AiPicovoice)
<!-- markdown-link-check-enable -->
[![YouTube Channel Views](https://img.shields.io/youtube/channel/views/UCAdi9sTCXLosG1XeqDwLx7w?label=YouTube&style=social)](https://www.youtube.com/channel/UCAdi9sTCXLosG1XeqDwLx7w)

The Android Voice Processor is an asynchronous audio capture library designed for real-time audio
processing. Given some specifications, the library delivers frames of raw audio data to the user via
listeners.

## Table of Contents

- [Android Voice Processor](#android-voice-processor)
    - [Table of Contents](#table-of-contents)
    - [Requirements](#requirements)
    - [Compatibility](#compatibility)
    - [Installation](#installation)
    - [Permissions](#permissions)
    - [Usage](#usage)
        - [Capturing with Multiple Listeners](#capturing-with-multiple-listeners)
    - [Example](#example)

## Requirements

- Java SDK (11+)
- Android SDK (21+)

## Compatibility

- Android 5.0+ (API 21+)

## Installation

Android Voice Processor can be found on Maven Central. To include the package in your Android
project, ensure you have included `mavenCentral()` in your top-level `build.gradle` file and then
add the following to your app's `build.gradle`:

```groovy
dependencies {
    // ...
    implementation 'ai.picovoice:android-voice-processor:${LATEST_VERSION}'
}
```

## Permissions

To enable audio recording with your Android device's microphone, you must add the following line to
your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

See our [example app](example/) or [this guide](https://developer.android.com/training/permissions/requesting)
for how to properly request for this permission from your users.

## Usage

Access the singleton instance of `VoiceProcessor`:

```java
import ai.picovoice.android.voiceprocessor.*;

VoiceProcessor voiceProcessor = VoiceProcessor.getInstance();
```

Add listeners for audio frames and errors:

```java
final VoiceProcessorFrameListener frameListener = frame -> {
    // use audio data
};
final VoiceProcessorErrorListener errorListener = e -> {
    // handle error
};

voiceProcessor.addFrameListener(frameListener);
voiceProcessor.addErrorListener(errorListener);
```

Start audio capture with the desired frame length and audio sample rate:

```java
final int frameLength = 512;
final int sampleRate = 16000;

voiceProcessor.start(frameLength, sampleRate);
```

Stop audio capture:
```java
voiceProcessor.stop();
```

Once audio capture has started successfully, any frame listeners assigned to the `VoiceProcessor`
will start receiving audio frames with the given `frameLength` and `sampleRate`.

### Capturing with Multiple Listeners

Any number of listeners can be added to and removed from the `VoiceProcessor` instance. However,
the instance can only record audio with a single audio configuration (`frameLength` and `sampleRate`),
which all listeners will receive once a call to `start()` has been made. To add multiple listeners:
```java
VoiceProcessorFrameListener listener1 = frame -> { };
VoiceProcessorFrameListener listener2 = frame -> { };
VoiceProcessorFrameListener[] listeners = new VoiceProcessorFrameListener[] {
        listener1, listener2 
};

voiceProcessor.addFrameListeners(listeners);

voiceProcessor.removeFrameListeners(listeners);
// or
voiceProcessor.clearFrameListeners();
```

## Example

The [Android Voice Processor app](example/) demonstrates how to ask for user permissions and capture output from
the `VoiceProcessor`.

## Releases

### v1.0.0 - July 18, 2023

- Initial public release.
