package me.marc_himmelberger.musicinterpreter.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

class Waveform {
    private final Paint mPaint;

    protected final MainActivity mMainActivity;

    public Waveform(MainActivity activity, Paint paint) {
        mMainActivity = activity;
        mPaint = paint;
    }

    public void drawOnCanvas(Canvas canvas) {
        canvas.drawColor(Color.LTGRAY);

        ArrayList<Short> samples;
        if ((samples = mMainActivity.getSamples()) != null) {
            int framesPerPx = samples.size() / canvas.getWidth();

            int maxVal = Short.MIN_VALUE;
            for (int i = 0; i < samples.size(); i++) {
                int s = Math.abs(samples.get(i));

                if (s > maxVal)
                    maxVal = s;
            }

            float pxPerSampleVal = canvas.getHeight() / (maxVal*2f);
            float yOffset = maxVal * pxPerSampleVal;

            for (int x = 0; x < canvas.getWidth(); x++) {
                int avg = 0;
                int max = Short.MIN_VALUE;

                for (int i = 0; i < framesPerPx; i++) {
                    int s = Math.abs(samples.get(x * framesPerPx + i));

                    if (s > max)
                        max = s;

                    avg += s;
                }

                avg /= (float) framesPerPx;

                float maxY = max * pxPerSampleVal;
                float avgY = avg * pxPerSampleVal;

                mPaint.setColor(Color.DKGRAY);
                canvas.drawLine(x, yOffset - maxY, x, yOffset + maxY, mPaint);

                mPaint.setColor(Color.BLACK);
                canvas.drawLine(x, yOffset - avgY, x, yOffset + avgY, mPaint);
            }
        }
    }
}
