package me.marc_himmelberger.musicinterpreter.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {
    private MainActivity mMainActivity;

    public WaveformView(Context context) {
        super(context);
    }
    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setParentActivity(Activity newParent) {
        if (newParent instanceof MainActivity) {
            mMainActivity = (MainActivity) newParent;
        } else {
            throw new IllegalArgumentException("Parent must be of type MainActivity");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMainActivity.getSamples() != null) {

        }
    }
}
