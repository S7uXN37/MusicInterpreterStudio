package me.marc_himmelberger.musicinterpreter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
                        ((MainActivity) getActivity()).readFile(); // TODO pass parameters
                    }
                });

                ((WaveformView) rootView.findViewById(R.id.waveform)).setParentActivity(getActivity());

                break;
            case 2:
                rootView = inflater.inflate(R.layout.fragment_analysis, container, false);
                break;
            case 3:
                rootView = inflater.inflate(R.layout.fragment_results, container, false);
                break;
            default:
                return null;
        }

        return rootView;
    }
}
