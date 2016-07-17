package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import me.marc_himmelberger.musicinterpreter.R;
import me.marc_himmelberger.musicinterpreter.interpretation.Interpreter;
import me.marc_himmelberger.musicinterpreter.io.DecoderListener;
import me.marc_himmelberger.musicinterpreter.io.Mp3Decoder;

public class MainActivity extends FragmentActivity {
	public static final int GET_FILE_REQ_CODE = 0;
	
	Interpreter mInterpreter;
    MediaPlayer mMediaPlayer;
    ViewPagerLock mViewPagerLock;

    private Uri selectedUri;
    private ArrayList<Short> samples = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // setup title bar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mToolbar.setTitle(R.string.app_name);

        // instantiate Interpreter
		mInterpreter = new Interpreter();

        // setup PagerAdapter, ViewPager and ViewPagerLock
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), getResources());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPagerLock = new ViewPagerLock(this);
        mViewPager.addOnPageChangeListener(mViewPagerLock);
	}

    // called when a file was selected -> put uri into filePath TextView, unlock screen no. 1
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_FILE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                // read out & save Uri
                selectedUri = data.getData();

                // display Uri
                ((TextView) findViewById(R.id.filePath)).setText(selectedUri.toString());

                // unlock next screen
                mViewPagerLock.screenUnlocked = 1;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
	}

    synchronized void readFile() {
        final ProgressBar progressBar = ((ProgressBar) findViewById(R.id.read_file_progress));
        findViewById(R.id.readFileButton).setEnabled(false);

        progressBar.setIndeterminate(true);

        try {
            // Setup max for progress bar in background, then remove indeterminate flag
            AsyncTask<Void, Void, Void> setupProgressBar = new AsyncTask<Void, Void, Void>() {
                @Override
                @Nullable
                protected Void doInBackground(Void[] objects) {
                    if (mMediaPlayer != null)
                        mMediaPlayer.release();

                    mMediaPlayer = null;

                    try {
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setDataSource(getApplicationContext(), selectedUri);
                        mMediaPlayer.prepare();

                        final int total_ms = mMediaPlayer.getDuration();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setMax(total_ms);
                                progressBar.setIndeterminate(false);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

            setupProgressBar.execute();

            // Get input stream to file
            final InputStream inputStream = getContentResolver().openInputStream(selectedUri);

            // create DecoderListener and start decoding task
            Mp3Decoder.startDecode(inputStream, new DecoderListener() {
                @Override
                public void OnDecodeComplete(ArrayList<Short> data) {
                    // successfully read file -> unlock next screen, update WaveformView, fill ProgressBar
                    samples = data;
                    mViewPagerLock.screenUnlocked = 2;

                    findViewById(R.id.waveform).postInvalidate();
                    progressBar.setProgress(progressBar.getMax());

                    Log.v("Mp3Decoder", "Decoder completed, samples==null = " + (samples == null));
                }

                @Override
                public void OnDecodeUpdate(int done_ms) {
                    progressBar.setProgress(done_ms);
                    Log.v("Mp3Decoder", "Progress: " + done_ms);
                }

                @Override
                public void OnDecodeError(Exception e) {
                    readError(e);
                }

                @Override
                public void OnDecodeTerminated() {
                    try {
                        if (inputStream != null)
                            inputStream.close();
                    } catch (IOException e) {
                        readError(e);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.readFileButton).setEnabled(true);
                        }
                    });
                }
            });
        } catch (IOException e) {
            readError(e);
        }
    }

    private void readError(Exception e) {
        Snackbar.make(
                findViewById(R.id.pager),
                getString(R.string.read_file_error),
                Snackbar.LENGTH_LONG
        ).show();

        Log.e("Mp3Decoder", "Decoder crashed", e);
    }

    ArrayList<Short> getSamples() {
        return samples;
    }

    void analyze() {
        AsyncTask<Void, Void, Void> analyzeTask = new AsyncTask<Void, Void, Void>() {
            int windowSizeLog2;
            float freqCut;
            ProgressBar progressBar;

            @Override
            protected void onPreExecute() {
                findViewById(R.id.analyzeButton).setEnabled(false);

                progressBar = (ProgressBar) findViewById(R.id.analyzeProgressBar);
                progressBar.setMax(mInterpreter.mNotes.size());
                progressBar.setIndeterminate(true);

                SeekBar windowSizeBar = (SeekBar) findViewById(R.id.param_windowSize);
                SeekBar cutoffFreqBar = (SeekBar) findViewById(R.id.analyze_cutoffFreq);

                windowSizeLog2 = windowSizeBar.getProgress() + 9;
                freqCut = cutoffFreqBar.getProgress();
            }

            @Override
            @Nullable
            protected Void doInBackground(Void... voids) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setIndeterminate(false);
                    }
                });
                mInterpreter.analyzeFrequencies(windowSizeLog2, 440f, freqCut, progressBar);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                findViewById(R.id.analyzeButton).setEnabled(true);
                findViewById(R.id.analysisView).postInvalidate();
            }
        };

        analyzeTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMediaPlayer.release();
        mMediaPlayer = null;

        AnalysisView.cursorUpdate.removeMessages(AnalysisView.MSG_WHAT);
        AnalysisView.cursorUpdate = null;
    }
}
