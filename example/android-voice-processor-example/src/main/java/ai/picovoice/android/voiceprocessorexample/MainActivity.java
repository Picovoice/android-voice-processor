/*
    Copyright 2023 Picovoice Inc.

    You may not use this file except in compliance with the license. A copy of the license is
    located in the "LICENSE" file accompanying this source.

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
    express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

package ai.picovoice.android.voiceprocessorexample;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ai.picovoice.android.voiceprocessor.VoiceProcessor;
import ai.picovoice.android.voiceprocessor.VoiceProcessorFrameListener;
import ai.picovoice.android.voiceprocessor.VoiceProcessorErrorListener;
import ai.picovoice.android.voiceprocessor.VoiceProcessorException;

public class MainActivity extends AppCompatActivity {

    private final VoiceProcessorFrameListener frameListener = frame -> {
        Log.i("VoiceProcessor", String.format("Frame of size %d received!", frame.length));
    };
    private final VoiceProcessorErrorListener errorListener = this::onAppError;
    private VoiceProcessor vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vp = VoiceProcessor.getInstance();
        vp.addFrameListener(frameListener);
        vp.addErrorListener(errorListener);
    }

    private void requestRecordPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.RECORD_AUDIO},
                0);
    }

    private void start() {
        vp.start(512, 16000);
    }

    private void stop() {
        try {
            vp.stop();
        } catch (VoiceProcessorException e) {
            onAppError(e);
        }
    }

    private void onAppError(Exception e) {
        runOnUiThread(() -> {
            TextView errorText = findViewById(R.id.errorMessage);
            errorText.setText(e.getMessage());
            errorText.setVisibility(View.VISIBLE);

            ToggleButton recordButton = findViewById(R.id.record_button);
            recordButton.setBackground(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.button_disabled));
            recordButton.setChecked(false);
            recordButton.setEnabled(false);
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            onAppError(new VoiceProcessorException("Microphone permission denied"));
        } else {
            start();
        }
    }

    public void onRecordClick(View view) {
        ToggleButton recordButton = findViewById(R.id.record_button);
        if (recordButton.isChecked()) {
            if (vp.hasRecordAudioPermission(this)) {
                start();
            } else {
                requestRecordPermission();
            }
        } else {
            stop();
        }
    }
}
