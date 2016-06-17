package me.marc_himmelberger.musicinterpreter.ui;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.view.Menu;
import android.view.MenuItem;
import me.marc_himmelberger.musicinterpreter.R;
import me.marc_himmelberger.musicinterpreter.interpretation.Interpreter;

public class MainActivity extends AppCompatActivity {
	public static final int GET_FILE_REQ_CODE = 0;
	
	private Interpreter interpreter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		
		interpreter = new Interpreter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.action_settings:
				// TODO
				return true;
			case R.id.action_open:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("file/*");
				startActivityForResult(intent, GET_FILE_REQ_CODE);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case GET_FILE_REQ_CODE:
				if (resultCode == RESULT_OK) {
					Uri uri =  data.getData();
					
					try {
						InputStream is = getContentResolver().openInputStream(uri);

						Log.i("MusicInterpreter", "Stream acquired!");
						Log.i("MusicInterpreter", is.toString());
						

						Log.i("MusicInterpreter", "Decoding stream...");
						byte[] audioFrames = null;
						try {
							audioFrames = Mp3Decoder.decode(is, 10 * 1000); // TODO load from Settings
						} catch (Exception e) {
							Log.e("MusicInterpreter", e.getMessage());
						}
						Log.i("MusicInterpreter", "Decoding ended!");
						
						Log.i("MusicInterpreter", "Starting interpreter...");
						interpreter.read(audioFrames, 44100);
						Log.i("MusicInterpreter", "Read input! Analyzing...");
						interpreter.analyze();
						Log.i("MusicInterpreter", "Analyzed! notes=" + interpreter.notes.toString());
						
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
				return;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				return;
		}
	}
}
