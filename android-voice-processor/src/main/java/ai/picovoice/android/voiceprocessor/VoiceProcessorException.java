package ai.picovoice.android.voiceprocessor;

public class VoiceProcessorException extends Exception {
    public VoiceProcessorException(Throwable cause) {
        super(cause);
    }

    public VoiceProcessorException(String message) {
        super(message);
    }

    public VoiceProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}