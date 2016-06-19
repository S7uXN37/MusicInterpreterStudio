package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import me.marc_himmelberger.musicinterpreter.interpretation.Interpreter;
import me.marc_himmelberger.musicinterpreter.interpretation.Note;

public class WaveformPreview extends WaveformView {
    private static final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Interpreter mInterpreter;
    private SeekBar mSensitivityBar;
    private SeekBar mThresholdBar;
    private ProgressBar mIdleBar;

    public WaveformPreview(Context context) {
        super(context);
    }
    public WaveformPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setInterpreter (Interpreter interpreter) {
        mInterpreter = interpreter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || super.mWaveform.mMainActivity.getSamples() == null)
            return;

        float thresholdY = mWaveform.yOffset - getThreshold() * mWaveform.pxPerSampleVal;

        mPaint.setColor(Color.RED);
        canvas.drawLine(0, thresholdY, canvas.getWidth(), thresholdY, mPaint);
        Log.v("W", "mPaint=" + mPaint + " thresholdY=" + thresholdY + " offset=" + mWaveform.yOffset);

        float pxPerSample = 1 / (float) mWaveform.framesPerPx;

        for (Note n : mInterpreter.notes) {
            float x = n.frame * pxPerSample;

            canvas.drawLine(x, 0, x, canvas.getHeight(), mPaint);
        }
    }

    void update() {
        AsyncTask<Void, Void, Void> updateTask = new AsyncTask<Void, Void, Void>() {
            int sensitivity;
            int threshold;

            @Override
            protected void onPreExecute() {
                sensitivity = getSensitivity();
                threshold = getThreshold();
                mIdleBar.setIndeterminate(true);
            }

            @Override
            @Nullable
            protected Void doInBackground(Void... voids) {
                mInterpreter.setData(mWaveform.mMainActivity.getSamples());
                mInterpreter.analyzeNotes(sensitivity, threshold);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mIdleBar.setIndeterminate(false);
                postInvalidate();
            }
        };

        updateTask.execute();
    }

    void setParameterInputs(final SeekBar sensitivityBar, final SeekBar thresholdBar, final ProgressBar idleBar) {
        mSensitivityBar = sensitivityBar;
        mThresholdBar = thresholdBar;
        mIdleBar = idleBar;
    }

    @UiThread
    private int getThreshold() {
        return mThresholdBar.getProgress() + 100;
    }

    @UiThread
    private int getSensitivity() {
        return mSensitivityBar.getProgress() + 1000;
    }
}