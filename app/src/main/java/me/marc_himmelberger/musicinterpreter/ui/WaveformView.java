package me.marc_himmelberger.musicinterpreter.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Waveform mWaveform;

    public WaveformView(Context context) {
        super(context);
    }
    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setParentActivity(@NonNull Activity newParent) {
        if (newParent instanceof MainActivity) {
            mWaveform = new Waveform((MainActivity) newParent, mPaint);

            postInvalidate();
        } else {
            throw new IllegalArgumentException("Parent must be of type MainActivity");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawColor(Color.LTGRAY);

            mPaint.setColor(Color.BLACK);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setTextSize(100f);
            canvas.drawText("SAMPLE DATA", getWidth() / 2f, getHeight() / 2f, mPaint);
        } else
            mWaveform.drawOnCanvas(canvas);
    }
}
