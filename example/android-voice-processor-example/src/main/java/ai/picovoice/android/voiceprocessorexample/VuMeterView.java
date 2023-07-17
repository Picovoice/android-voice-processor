package ai.picovoice.android.voiceprocessorexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

public class VuMeterView extends View {

    private static final int MAX_VOLUME = 100;
    private static final int MIN_VOLUME = 0;
    private static final double DBFS_OFFSET = 60.0;
    private static final int VOLUME_SMOOTHING_SIZE = 5;

    private final Queue<Double> volumeSmoothingQueue = new LinkedList<>();

    private Paint backgroundPaint;
    private Paint meterPaint;
    private Rect meterRect;

    public VuMeterView(Context context) {
        super(context);
        initialize();
    }

    public VuMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.GRAY);

        meterPaint = new Paint();
        meterPaint.setColor(Color.parseColor("#377dff"));

        meterRect = new Rect();
    }

    public void setVolumeLevel(double dbfsValue) {
        double adjustedVal = dbfsValue + DBFS_OFFSET;
        adjustedVal = (Math.max(MIN_VOLUME, adjustedVal) / DBFS_OFFSET);
        adjustedVal = Math.min(1.0, adjustedVal) * MAX_VOLUME;

        if (volumeSmoothingQueue.size() >= VOLUME_SMOOTHING_SIZE) {
            volumeSmoothingQueue.poll();
        }
        volumeSmoothingQueue.offer(adjustedVal);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        canvas.drawRect(0, 0, width, height, backgroundPaint);

        double smoothedValue = 0.0;
        for (double val : volumeSmoothingQueue) {
            smoothedValue += val;
        }
        smoothedValue = smoothedValue / volumeSmoothingQueue.size();

        int meterWidth = (int) (width * smoothedValue / MAX_VOLUME);
        meterRect.set(0, 0, meterWidth, height);
        canvas.drawRect(meterRect, meterPaint);
    }

}
