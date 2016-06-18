package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import me.marc_himmelberger.musicinterpreter.R;
import me.marc_himmelberger.musicinterpreter.interpretation.Interpreter;

public class MainActivity extends FragmentActivity {
	public static final int GET_FILE_REQ_CODE = 0;
	
	private Interpreter mInterpreter;
	private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private ViewPagerLock mViewPagerLock;

    private Uri selectedUri;
    private short[] samples;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mToolbar.setTitle(R.string.app_name);

		mInterpreter = new Interpreter();

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), getResources());

        mViewPager = (ViewPager) findViewById(R.id.pager);
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

    protected synchronized void readFile() {
        final ProgressBar progressBar = ((ProgressBar) findViewById(R.id.read_file_progress));

        int durationMs = (int) (10f * 1000);
        progressBar.setMax(durationMs);
        progressBar.setIndeterminate(true);

        try {
            final InputStream inputStream = getContentResolver().openInputStream(selectedUri);

            Mp3Decoder.startDecode(inputStream, durationMs, new DecoderListener() {
                @Override
                public void OnDecodeComplete(short[] data) {
                    samples = data;
                    mViewPagerLock.screenUnlocked = 2;
                    Log.v("Mp3Decoder", "Decoder completed");
                }

                @Override
                public void OnDecodeUpdate(int done_ms) {
                    progressBar.setIndeterminate(false);
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
                        inputStream.close();
                    } catch (IOException e) {
                        readError(e);
                    }
                }
            });
        } catch (FileNotFoundException e) {
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

    protected short[] getSamples() {
        return samples;
    }
}