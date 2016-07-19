package me.marc_himmelberger.musicinterpreter.filtering;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import me.marc_himmelberger.musicinterpreter.R;
import me.marc_himmelberger.musicinterpreter.ui.MainActivity;

public class FilterActivity extends AppCompatActivity {
    private ArrayList<Short> samples;
    private TextView freqDisplay;
    private SeekBar freqSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        samples = (ArrayList<Short>) getIntent().getExtras().get(MainActivity.EXTRA_SAMPLES);

        freqDisplay = (TextView) findViewById(R.id.filters_frequency);
        freqSeekBar = (SeekBar) findViewById(R.id.filters_frequency_SeekBar);

        // TODO setup playback
    }

    public void highPass(View view) {
        // TODO
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
}
