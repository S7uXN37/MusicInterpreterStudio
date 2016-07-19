package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;

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

                rootView.findViewById(R.id.filterButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).openFilterActivity();
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

                final MainActivity mMainActivity = (MainActivity) getActivity();
                final Button playPauseButton = (Button) rootView.findViewById(R.id.playPauseButton);
                final View stopButton = rootView.findViewById(R.id.stopButton);
                final View upButton = rootView.findViewById(R.id.upButton);
                final View downButton = rootView.findViewById(R.id.downButton);
                final AnalysisView analysisView = (AnalysisView) rootView.findViewById(R.id.analysisView);

                analysisView.setInterpreter(mMainActivity.mInterpreter);
                analysisView.setMediaPlayer(mMainActivity);

                playPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mMainActivity.mMediaPlayer.isPlaying()) {
                            mMainActivity.mMediaPlayer.pause();
                            playPauseButton.setText(getString(R.string.playPauseButton_textPlay));
                        } else {
                            mMainActivity.mMediaPlayer.start();
                            playPauseButton.setText(getString(R.string.playPauseButton_textPause));
                        }
                    }
                });
                stopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMainActivity.mMediaPlayer.pause();
                        mMainActivity.mMediaPlayer.seekTo(0);

                        playPauseButton.setText(getString(R.string.playPauseButton_textPlay));
                    }
                });
                upButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMainActivity.mInterpreter.shiftNotes(1);
                        analysisView.postInvalidate();
                    }
                });
                downButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMainActivity.mInterpreter.shiftNotes(-1);
                        analysisView.postInvalidate();
                    }
                });

                AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                int result = audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                    boolean shouldPlay = true;

                    @Override
                    public void onAudioFocusChange(int i) {
                        switch (i) {
                            case AudioManager.AUDIOFOCUS_GAIN:
                                if (!mMainActivity.mMediaPlayer.isPlaying() && shouldPlay)
                                    mMainActivity.mMediaPlayer.start();
                                mMainActivity.mMediaPlayer.setVolume(1.0f, 1.0f);
                                break;

                            case AudioManager.AUDIOFOCUS_LOSS:
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                shouldPlay = mMainActivity.mMediaPlayer.isPlaying();
                                if (mMainActivity.mMediaPlayer.isPlaying())
                                    mMainActivity.mMediaPlayer.pause();
                                break;

                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                if (mMainActivity.mMediaPlayer.isPlaying())
                                    mMainActivity.mMediaPlayer.setVolume(0.1f, 0.1f);
                                break;

                            default:
                                break;
                        }
                    }
                }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Log.w("MusicInterpreter", "Could not acquire AudioFocus!");
                }

                break;
            default:
                return null;
        }

        return rootView;
    }
}
