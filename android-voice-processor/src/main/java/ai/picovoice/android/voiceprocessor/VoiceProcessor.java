/*
    Copyright 2023 Picovoice Inc.

    You may not use this file except in compliance with the license. A copy of the license is
    located in the "LICENSE" file accompanying this source.

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
    express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

package ai.picovoice.android.voiceprocessor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoiceProcessor {

    private static VoiceProcessor instance = null;
    private final ArrayList<VoiceProcessorBufferListener> bufferListeners = new ArrayList<>();
    private final ArrayList<VoiceProcessorErrorListener> errorListeners = new ArrayList<>();

    private final AtomicBoolean isStopRequested = new AtomicBoolean(false);
    private final Handler callbackHandler = new Handler(Looper.getMainLooper());
    private final Object listenerLock = new Object();

    private Future<Void> readThread = null;
    private int frameLength;
    private int sampleRate;

    private VoiceProcessor(int frameLength, int sampleRate) {
        this.frameLength = frameLength;
        this.sampleRate = sampleRate;
    }

    public static synchronized VoiceProcessor getInstance(
            Context context,
            int frameLength,
            int sampleRate) throws VoiceProcessorException {

        int permission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            throw new VoiceProcessorException("This app has not enabled recording permissions.");
        }

        if (instance == null) {
            instance = new VoiceProcessor(frameLength, sampleRate);
        } else {
            instance.frameLength = frameLength;
            instance.sampleRate = sampleRate;
        }

        return instance;
    }

    public boolean getIsRecording() {
        return readThread != null;
    }

    public void addBufferListener(VoiceProcessorBufferListener listener) {
        synchronized (listenerLock) {
            bufferListeners.add(listener);
        }
    }

    public void addBufferListeners(VoiceProcessorBufferListener[] listeners) {
        synchronized (listenerLock) {
            bufferListeners.addAll(Arrays.asList(listeners));
        }
    }

    public void removeBufferListener(VoiceProcessorBufferListener listener) {
        synchronized (listenerLock) {
            bufferListeners.remove(listener);
        }
    }

    public void removeBufferListeners(VoiceProcessorBufferListener[] listener) {
        synchronized (listenerLock) {
            bufferListeners.removeAll(Arrays.asList(listener));
        }
    }

    public void clearBufferListeners() {
        synchronized (listenerLock) {
            bufferListeners.clear();
        }
    }

    public int getNumBufferListeners() {
        return bufferListeners.size();
    }

    public void addErrorListener(VoiceProcessorErrorListener errorListener) {
        synchronized (listenerLock) {
            errorListeners.add(errorListener);
        }
    }

    public void removeErrorListener(VoiceProcessorErrorListener errorListener) {
        synchronized (listenerLock) {
            errorListeners.remove(errorListener);
        }
    }

    public void clearErrorListeners() {
        synchronized (listenerLock) {
            errorListeners.clear();
        }
    }

    public int getNumErrorListeners() {
        return errorListeners.size();
    }

    public synchronized void start() {
        if (getIsRecording()) {
            return;
        }

        readThread = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
            @Override
            public Void call() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                read();
                return null;
            }
        });
    }

    public synchronized void stop() throws VoiceProcessorException {
        if (!getIsRecording()) {
            return;
        }

        isStopRequested.set(true);
        try {
            readThread.get();
            readThread = null;
        } catch (ExecutionException | InterruptedException e) {
            throw new VoiceProcessorException(
                    "An error was encountered while requesting to stop the audio recording",
                    e);
        } finally {
            isStopRequested.set(false);
        }
    }

    @SuppressLint("MissingPermission")
    private void read() {
        final int minBufferSize = AudioRecord.getMinBufferSize(
                this.sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        final int bufferSize = Math.max(this.sampleRate / 2, minBufferSize);

        AudioRecord recorder;
        try {
            recorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    this.sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);
        } catch (IllegalArgumentException e) {
            onError(new VoiceProcessorException(
                    "Unable to initialize audio recorder with required parameters",
                    e));
            return;
        }

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            onError(new VoiceProcessorException(
                    "Audio recorder did not initialize successfully"));
            return;
        }

        final short[] buffer = new short[this.frameLength];
        try {
            recorder.startRecording();

            while (!isStopRequested.get()) {
                if (recorder.read(buffer, 0, buffer.length) == buffer.length) {
                    onBuffer(buffer);
                }
            }

            recorder.stop();
        } catch (IllegalStateException e) {
            onError(new VoiceProcessorException(
                    "Audio recorder entered invalid state",
                    e));
        } finally {
            recorder.release();
        }
    }

    private void onBuffer(final short[] buffer) {
        synchronized (listenerLock) {
            for (final VoiceProcessorBufferListener listener : bufferListeners) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onBuffer(buffer);
                    }
                });
            }
        }
    }

    private void onError(final VoiceProcessorException e) {
        synchronized (listenerLock) {
            for (final VoiceProcessorErrorListener listener : errorListeners) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(e);
                    }
                });
            }
        }
    }
}