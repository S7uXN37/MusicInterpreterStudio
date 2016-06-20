package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import java.io.IOException;

import me.marc_himmelberger.musicinterpreter.R;

public class InterpreterFragment extends Fragment {
    public static final String ARG_ID_KEY = "screenId";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;
        switch (getArguments().getInt(ARG_ID_KEY)) {
            case 0:
                rootView = inflater.inflate(R.layout.fragment_open_file, container, false);

                rootView.findViewById(R.id.openFileButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent request = new Intent(Intent.ACTION_GET_CONTENT);
                        request.setType("audio/*");
                        getActivity().startActivityForResult(request, MainActivity.GET_FILE_REQ_CODE);
                    }
                });

                break;
            case 1:
                rootView = inflater.inflate(R.layout.fragment_read_file, container, false);

                rootView.findViewById(R.id.readFileButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).readFile();
                    }
                });

                ((WaveformView) rootView.findViewById(R.id.waveform)).setParentActivity(getActivity());

                break;
            case 2:
                rootView = inflater.inflate(R.layout.fragment_parameter_preview, container, false);

                final WaveformPreview preview = ((WaveformPreview) rootView.findViewById(R.id.waveform_preview));
                final SeekBar sensitivityBar = (SeekBar) rootView.findViewById(R.id.param_sensitivity);
                final SeekBar thresholdBar = (SeekBar) rootView.findViewById(R.id.param_threshold);
                final SeekBar windowSizeBar = (SeekBar) rootView.findViewById(R.id.param_windowSize);
                final ProgressBar idleBar = (ProgressBar) rootView.findViewById(R.id.param_idleBar);

                SeekBar.OnSeekBarChangeListener previewUpdater = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (!idleBar.isIndeterminate())
                            preview.update();
                    }
                };
                SeekBar.OnSeekBarChangeListener previewInvalidator = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (!idleBar.isIndeterminate())
                            preview.postInvalidate();
                    }
                };

                sensitivityBar.setOnSeekBarChangeListener(previewUpdater);
                thresholdBar.setOnSeekBarChangeListener(previewUpdater);
                windowSizeBar.setOnSeekBarChangeListener(previewInvalidator);

                preview.setParameterInputs(sensitivityBar, thresholdBar, windowSizeBar, idleBar);
                preview.setInterpreter(((MainActivity) getActivity()).mInterpreter);
                preview.setParentActivity(getActivity());

                break;
            case 3:
                rootView = inflater.inflate(R.layout.fragment_analysis, container, false);

                rootView.findViewById(R.id.analyzeButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).analyze();
                    }
                });

                AnalysisView analysisView = (AnalysisView) rootView.findViewById(R.id.resultsView);
                analysisView.setInterpreter(
                        ((MainActivity) getActivity()).mInterpreter
                );

                final MediaPlayer mediaPlayer = new MediaPlayer();
                final View playPauseButton = rootView.findViewById(R.id.playPauseButton);
                final View stopButton = rootView.findViewById(R.id.stopButton);

                AsyncTask<Void, Void, Void> mediaSetupTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        playPauseButton.setEnabled(false);
                        stopButton.setEnabled(false);
                        Log.v("m", "setup...");
                    }

                    @Override
                    @Nullable
                    protected Void doInBackground(Void... voids) {
                        try {
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                            Context context = getContext();
                            MainActivity mainActivity = ((MainActivity) getActivity());

                            if (context == null || mainActivity == null || mainActivity.getSelectedUri() == null) {
                                mediaPlayer.release();
                                return null;
                            }
                            mediaPlayer.setDataSource(context, mainActivity.getSelectedUri());
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            Log.e("MusicInterpreter", "Error playing back file", e);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        playPauseButton.setEnabled(true);
                        stopButton.setEnabled(true);
                        Log.v("m", "setup done");
                    }
                };
                mediaSetupTask.execute();
                analysisView.setMediaPlayer(mediaPlayer);

                playPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v("m", "play/pause");
                        if (mediaPlayer.isPlaying())
                            mediaPlayer.pause();
                        else
                            mediaPlayer.start();
                    }
                });
                stopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v("m", "stop");
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(0);
                    }
                });

                AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                int result = audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int i) {
                        switch (i) {
                            case AudioManager.AUDIOFOCUS_GAIN:
                                if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                                mediaPlayer.setVolume(1.0f, 1.0f);
                                break;

                            case AudioManager.AUDIOFOCUS_LOSS:
                                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                                mediaPlayer.release();
                                break;

                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                                break;

                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                                break;
                        }
                    }
                }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mediaPlayer.release();
                }

                break;
            default:
                return null;
        }

        return rootView;
    }
}
