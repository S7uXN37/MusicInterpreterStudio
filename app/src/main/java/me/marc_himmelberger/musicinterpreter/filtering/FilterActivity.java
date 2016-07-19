package me.marc_himmelberger.musicinterpreter.filtering;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.InputStream;

import me.marc_himmelberger.musicinterpreter.R;
import me.marc_himmelberger.musicinterpreter.ui.MainActivity;

public class FilterActivity extends AppCompatActivity {
    private short[] samples;
    private TextView freqDisplay;
    private SeekBar freqSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        samples = (short[]) getIntent().getExtras().get(MainActivity.EXTRA_SAMPLES);

        freqDisplay = (TextView) findViewById(R.id.filters_frequency);
        freqSeekBar = (SeekBar) findViewById(R.id.filters_frequency_SeekBar);

        // TODO setup playback
    }

    public void highPass(View view) {

    }

    public void lowPass(View view) {
        // TODO
    }

    public void undo(View view) {
        // TODO
    }

    public void mediaPlayPause(View view) {
        // TODO
    }

    public void mediaStop(View view) {
        // TODO
    }

    public InputStream getMediaStream() {
        // TODO
        return null;
    }


    /**
     * Code from: <a href="https://en.wikipedia.org/wiki/High-pass_filter#Algorithmic_implementation">Wikipedia</a>
     */
    private static short[] highPassFilter(short[] samples, float freqCut) {
        short[] filtered = new short[samples.length];
        double sampleInterval = 1d / 44100d;
        double alpha = 1d / (2d * Math.PI * sampleInterval * freqCut + 1);

        filtered[0] = samples[0];
        for (int i = 1; i < samples.length; i++) {
            short real = samples[i];
            double deltaX = (double) (real - samples[i-1]);
            double cutReal = alpha * filtered[i - 1] + deltaX;

            filtered[i] = (short) cutReal;
        }

        return filtered;
    }
}
