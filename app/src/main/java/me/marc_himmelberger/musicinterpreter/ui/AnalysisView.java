package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import me.marc_himmelberger.musicinterpreter.interpretation.Interpreter;
import me.marc_himmelberger.musicinterpreter.interpretation.Note;

public class AnalysisView extends View {
    private final static int noteColor = Color.rgb(0, 204, 255);
    final static int MSG_WHAT = 1;
    static Handler cursorUpdate = null;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int cursorPos = 0;
    private Interpreter mInterpreter;

    public AnalysisView(Context context) {
        super(context);
    }
    public AnalysisView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInterpreter(Interpreter interpreter) {
        mInterpreter = interpreter;
    }

    public void setMediaPlayer(final MainActivity mainActivity) {
        cursorUpdate = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (mainActivity.mMediaPlayer == null)
                        return; // stop onDestroy

                    msg = obtainMessage(MSG_WHAT);
                    sendMessageDelayed(msg, 100);

                    try {
                        if (!mainActivity.mMediaPlayer.isPlaying())
                            return; // skip if paused/stopped
                    } catch (IllegalStateException e) {
                        return; // skip if not initialized
                    }

                    cursorPos = mainActivity.mMediaPlayer.getCurrentPosition();
                    postInvalidate();
                }
            }
        };
        cursorUpdate.sendEmptyMessage(MSG_WHAT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.LTGRAY);

        if (isInEditMode()) {
            mPaint.setTextSize(100f);
            mPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("SAMPLE RESULTS", canvas.getWidth() / 2f, canvas.getHeight() / 2f, mPaint);
        }

        if (mInterpreter == null || mInterpreter.mNotes == null)
            return;

        int numNotes = mInterpreter.mNotes.size();
        if (numNotes <= 0)
            return;

        int offset = 0;
        int duration = 0;

        int maxSteps = Integer.MIN_VALUE;
        int minSteps = Integer.MAX_VALUE;

        for (int i = 0; i < numNotes; i++) {
            Note n = mInterpreter.mNotes.get(i);

            if (n.stepsFromFixed > maxSteps)
                maxSteps = n.stepsFromFixed;
            if (n.stepsFromFixed < minSteps)
                minSteps = n.stepsFromFixed;

            if (i == 0)
                offset = n.frame;
            if (i == numNotes - 1)
                duration = n.frame + n.duration - offset;
        }


        float pxPerFrame = canvas.getWidth() / (float)  duration;
        float pxPerStep = canvas.getHeight() / (float) (maxSteps - minSteps + 1);

        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(pxPerStep);

        for (Note n : mInterpreter.mNotes) {
            float left = (n.frame - offset) * pxPerFrame;
            float right = left + (n.duration * pxPerFrame);

            float top = (maxSteps - n.stepsFromFixed) * pxPerStep;
            float bottom = top + pxPerStep;

            mPaint.setColor(noteColor);
            mPaint.setAlpha(180);
            canvas.drawRect(left, top, right, bottom, mPaint);

            mPaint.setColor(Color.DKGRAY);
            canvas.drawText(n.note, left, bottom, mPaint);
        }

        mPaint.setColor(Color.BLUE);
        float cursorX = (cursorPos * 44.1f - offset) * pxPerFrame;
        canvas.drawRect(cursorX, 0, cursorX + 10, canvas.getHeight(), mPaint);
    }
}
