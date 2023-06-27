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

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import ai.picovoice.android.voiceprocessor.VoiceProcessor;
import ai.picovoice.android.voiceprocessor.VoiceProcessorBufferListener;
import ai.picovoice.android.voiceprocessor.VoiceProcessorErrorListener;
import ai.picovoice.android.voiceprocessor.VoiceProcessorException;

@RunWith(AndroidJUnit4.class)
public class VoiceProcessorTest {

    final int frameLength = 512;
    final int sampleRate = 16000;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Test
    public void testGetInstance() {
        VoiceProcessor vp = VoiceProcessor.getInstance(frameLength, sampleRate);
        assertNotNull(vp);

        vp = VoiceProcessor.getInstance(1024, 8000);
        assertNotNull(vp);
    }

    @Test
    public void testBasic() throws InterruptedException, VoiceProcessorException {

        final VoiceProcessor vp = VoiceProcessor.getInstance(frameLength, sampleRate);
        assertNotNull(vp);

        AtomicInteger frameCounter = new AtomicInteger(0);
        vp.addBufferListener(shorts -> {
            assertEquals(shorts.length, frameLength);
            frameCounter.getAndIncrement();
        });
        vp.addErrorListener(Assert::assertNull);

        assertFalse(vp.getIsRecording());
        vp.start();
        assertTrue(vp.getIsRecording());

        Thread.sleep(1000);

        vp.stop();

        assertTrue(frameCounter.get() > 0);
        assertFalse(vp.getIsRecording());

        vp.clearErrorListeners();
        vp.clearBufferListeners();
    }

    @Test
    public void testInvalidSetup() throws InterruptedException, VoiceProcessorException {
        final VoiceProcessor vp = VoiceProcessor.getInstance(frameLength, 1000);
        assertNotNull(vp);

        AtomicInteger frameCounter = new AtomicInteger(0);
        AtomicInteger errorCounter = new AtomicInteger(0);
        vp.addBufferListener(shorts -> {
            assertEquals(shorts.length, frameLength);
            frameCounter.getAndIncrement();
        });
        vp.addErrorListener(e -> {
            assertNotNull(e);
            errorCounter.getAndIncrement();
        });

        assertFalse(vp.getIsRecording());
        vp.start();
        Thread.sleep(1000);
        vp.stop();

        assertEquals(frameCounter.get(), 0);
        assertEquals(errorCounter.get(), 1);
        assertFalse(vp.getIsRecording());
        vp.clearErrorListeners();
        vp.clearBufferListeners();
    }

    @Test
    public void testAddRemoveListeners() {
        final VoiceProcessor vp = VoiceProcessor.getInstance(frameLength, sampleRate);
        assertNotNull(vp);

        VoiceProcessorBufferListener b1 = buffer -> {
        };
        VoiceProcessorBufferListener b2 = buffer -> {
        };

        VoiceProcessorErrorListener e1 = e -> {
        };
        VoiceProcessorErrorListener e2 = e -> {
        };

        vp.addBufferListener(b1);
        assertEquals(vp.getNumBufferListeners(), 1);
        vp.addBufferListener(b2);
        assertEquals(vp.getNumBufferListeners(), 2);
        vp.removeBufferListener(b1);
        assertEquals(vp.getNumBufferListeners(), 1);
        vp.removeBufferListener(b1);
        assertEquals(vp.getNumBufferListeners(), 1);
        vp.removeBufferListener(b2);
        assertEquals(vp.getNumBufferListeners(), 0);

        VoiceProcessorBufferListener[] bs = new VoiceProcessorBufferListener[]{b1, b2};
        vp.addBufferListeners(bs);
        assertEquals(vp.getNumBufferListeners(), 2);
        vp.removeBufferListeners(bs);
        assertEquals(vp.getNumBufferListeners(), 0);
        vp.addBufferListeners(bs);
        assertEquals(vp.getNumBufferListeners(), 2);
        vp.clearBufferListeners();
        assertEquals(vp.getNumBufferListeners(), 0);

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
