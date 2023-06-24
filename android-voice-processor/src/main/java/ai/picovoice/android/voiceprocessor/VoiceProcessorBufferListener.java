package ai.picovoice.android.voiceprocessor;

public interface VoiceProcessorBufferListener {
    void onBuffer(short[] buffer);
}
