package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import me.marc_himmelberger.musicinterpreter.interpretation.Interpreter;
import me.marc_himmelberger.musicinterpreter.interpretation.Note;

public class AnalysisView extends View {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

    @Override
    protected void onDraw(Canvas canvas) {
        int numNotes = mInterpreter.notes.size();
        if (numNotes <= 0)
            return;

        int offset = 0;
        int duration = 0;

        int maxSteps = Integer.MIN_VALUE;
        int minSteps = Integer.MAX_VALUE;

        for (int i = 0; i < numNotes; i++) {
            Note n = mInterpreter.notes.get(i);

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

        mPaint.setColor(Color.BLUE);
        mPaint.setAlpha(180);
        for (Note n : mInterpreter.notes) {
            float left = (n.frame - offset) * pxPerFrame;
            float right = left + (n.duration * pxPerFrame);

            float top = (n.stepsFromFixed - minSteps) * pxPerStep;
            float bottom = top + pxPerStep;

            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }
}
