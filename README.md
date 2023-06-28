# Android Voice Processor

<!-- markdown-link-check-disable -->
<!-- need to make a release first -->
[![GitHub release](https://img.shields.io/github/release/Picovoice/pvrecorder.svg)](https://github.com/Picovoice/pvrecorder/releases)
[![GitHub](https://img.shields.io/github/license/Picovoice/pvrecorder)](https://github.com/Picovoice/pvrecorder/)
<!-- markdown-link-check-enable -->

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

Access the singleton instance of `VoiceProcessor` with a desired audio frame length and sample rate:

```java
import ai.picovoice.android.voiceprocessor.*;

final int frameLength=512;
final int sampleRate=16000;

VoiceProcessor voiceProcessor = VoiceProcessor.getInstance(frameLength, sampleRate);
```

Add listeners for audio frames and errors:

```java
final VoiceProcessorBufferListener bufferListener = buffer -> {
    // use audio data
};
final VoiceProcessorErrorListener errorListener = e -> {
    // handle error
};

voiceProcessor.addBufferListener(bufferListener);
voiceProcessor.addErrorListener(errorListener);
```

Start/stop audio capture:

```java
voiceProcessor.start();
// ... use audio
voiceProcessor.stop();
```

Once audio capture has started successfully, any buffer listeners assigned to the `VoiceProcessor`
will start receiving audio buffers with the `frameLength` and `sampleRate` that was last set with
`getInstance()`.

### Capturing with Multiple Listeners

Any number of listeners can be added to and removed from the `VoiceProcessor` instance. However,
the instance can only record audio with a single audio configuration (`frameLength` and `sampleRate`),
which all listeners will receive once a call to `start()` has been made. To add multiple listeners:
```java
VoiceProcessorBufferListener listener1 = buffer -> {};
VoiceProcessorBufferListener listener2 = buffer -> {};
VoiceProcessorBufferListener[] listeners = new VoiceProcessorBufferListener[]{b1, b2};

voiceProcessor.addBufferListeners(listeners);

voiceProcessor.removeBufferListners(listeners);
// or
voiceProcessor.clearBufferListeners();
```

## Example

The [Android Voice Processor app](example/) demonstrates how to ask for user permissions and capture output from
the `VoiceProcessor`.
