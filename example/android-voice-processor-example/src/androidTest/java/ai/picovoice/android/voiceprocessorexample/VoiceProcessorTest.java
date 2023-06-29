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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import ai.picovoice.android.voiceprocessor.VoiceProcessor;
import ai.picovoice.android.voiceprocessor.VoiceProcessorErrorListener;
import ai.picovoice.android.voiceprocessor.VoiceProcessorException;
import ai.picovoice.android.voiceprocessor.VoiceProcessorFrameListener;

@RunWith(AndroidJUnit4.class)
public class VoiceProcessorTest {

    final int FRAME_LENGTH = 512;
    final int SAMPLE_RATE = 16000;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Test
    public void testGetInstance() {
        VoiceProcessor vp = VoiceProcessor.getInstance();
        assertNotNull(vp);
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        assertTrue(vp.hasRecordAudioPermission(context));
    }

    @Test
    public void testBasic() throws InterruptedException, VoiceProcessorException {

        final VoiceProcessor vp = VoiceProcessor.getInstance();
        assertNotNull(vp);

        AtomicInteger frameCounter = new AtomicInteger(0);
        vp.addFrameListener(shorts -> {
            assertEquals(shorts.length, FRAME_LENGTH);
            frameCounter.getAndIncrement();
        });
        vp.addErrorListener(Assert::assertNull);

        assertFalse(vp.getIsRecording());
        vp.start(FRAME_LENGTH, SAMPLE_RATE);
        assertTrue(vp.getIsRecording());

        Thread.sleep(1000);

        vp.stop();

        assertTrue(frameCounter.get() > 0);
        assertFalse(vp.getIsRecording());

        vp.clearErrorListeners();
        vp.clearFrameListeners();
    }

    @Test
    public void testInvalidSetup() throws InterruptedException, VoiceProcessorException {
        final VoiceProcessor vp = VoiceProcessor.getInstance();
        assertNotNull(vp);

        AtomicInteger frameCounter = new AtomicInteger(0);
        AtomicInteger errorCounter = new AtomicInteger(0);
        vp.addFrameListener(shorts -> {
            assertEquals(shorts.length, FRAME_LENGTH);
            frameCounter.getAndIncrement();
        });
        vp.addErrorListener(e -> {
            assertNotNull(e);
            errorCounter.getAndIncrement();
        });

        assertFalse(vp.getIsRecording());
        vp.start(FRAME_LENGTH, 1000);
        Thread.sleep(1000);
        vp.stop();

        assertEquals(frameCounter.get(), 0);
        assertEquals(errorCounter.get(), 1);
        assertFalse(vp.getIsRecording());
        vp.clearErrorListeners();
        vp.clearFrameListeners();
    }

    @Test
    public void testAddRemoveListeners() {
        final VoiceProcessor vp = VoiceProcessor.getInstance();
        assertNotNull(vp);

        VoiceProcessorFrameListener b1 = frame -> {
        };
        VoiceProcessorFrameListener b2 = frame -> {
        };

        VoiceProcessorErrorListener e1 = e -> {
        };
        VoiceProcessorErrorListener e2 = e -> {
        };

        vp.addFrameListener(b1);
        assertEquals(vp.getNumFrameListeners(), 1);
        vp.addFrameListener(b2);
        assertEquals(vp.getNumFrameListeners(), 2);
        vp.removeFrameListener(b1);
        assertEquals(vp.getNumFrameListeners(), 1);
        vp.removeFrameListener(b1);
        assertEquals(vp.getNumFrameListeners(), 1);
        vp.removeFrameListener(b2);
        assertEquals(vp.getNumFrameListeners(), 0);

        VoiceProcessorFrameListener[] bs = new VoiceProcessorFrameListener[]{b1, b2};
        vp.addFrameListeners(bs);
        assertEquals(vp.getNumFrameListeners(), 2);
        vp.removeFrameListeners(bs);
        assertEquals(vp.getNumFrameListeners(), 0);
        vp.addFrameListeners(bs);
        assertEquals(vp.getNumFrameListeners(), 2);
        vp.clearFrameListeners();
        assertEquals(vp.getNumFrameListeners(), 0);

        vp.addErrorListener(e1);
        assertEquals(vp.getNumErrorListeners(), 1);
        vp.addErrorListener(e2);
        assertEquals(vp.getNumErrorListeners(), 2);
        vp.removeErrorListener(e1);
        assertEquals(vp.getNumErrorListeners(), 1);
        vp.removeErrorListener(e1);
        assertEquals(vp.getNumErrorListeners(), 1);
        vp.removeErrorListener(e2);
        assertEquals(vp.getNumErrorListeners(), 0);
        vp.addErrorListener(e1);
        assertEquals(vp.getNumErrorListeners(), 1);
        vp.clearErrorListeners();
        assertEquals(vp.getNumErrorListeners(), 0);
    }
}
