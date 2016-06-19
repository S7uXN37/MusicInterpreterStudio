package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import me.marc_himmelberger.musicinterpreter.interpretation.Interpreter;
import me.marc_himmelberger.musicinterpreter.interpretation.Note;

public class WaveformPreview extends WaveformView {
    private static final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Interpreter mInterpreter;

    public WaveformPreview(Context context) {
        super(context);
    }
    public WaveformPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInterpreter (Interpreter interpreter) {
        mInterpreter = interpreter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode())
            return;

        float pxPerSample = canvas.getWidth() / (float) super.mWaveform.mMainActivity.getSamples().size();

        for (Note n : mInterpreter.notes) {
            float x = n.frame * pxPerSample;

            mPaint.setColor(Color.RED);
            canvas.drawLine(x, 0, x, canvas.getHeight(), mPaint);
        }
    }
}
