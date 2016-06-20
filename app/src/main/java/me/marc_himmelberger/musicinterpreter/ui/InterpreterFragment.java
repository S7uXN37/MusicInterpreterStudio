package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

                ((WaveformView) rootView.findViewById(R.id.waveform)).setParentActivity(getActivity());

                break;
            case 2:
                rootView = inflater.inflate(R.layout.fragment_parameter_preview, container, false);

                final WaveformPreview preview = ((WaveformPreview) rootView.findViewById(R.id.waveform_preview));
                final SeekBar sensitivityBar = (SeekBar) rootView.findViewById(R.id.param_sensitivity);
                final SeekBar thresholdBar = (SeekBar) rootView.findViewById(R.id.param_threshold);
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

                sensitivityBar.setOnSeekBarChangeListener(previewUpdater);
                thresholdBar.setOnSeekBarChangeListener(previewUpdater);

                preview.setParameterInputs(sensitivityBar, thresholdBar, idleBar);
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

                break;
            case 4:
                rootView = inflater.inflate(R.layout.fragment_results, container, false);
                break;
            default:
                return null;
        }

        return rootView;
    }
}
