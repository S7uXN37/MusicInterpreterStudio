package me.marc_himmelberger.musicinterpreter.ui;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

class Mp3Decoder {
    private final DecoderListener mListener;

    private Mp3Decoder (DecoderListener listener) {
        mListener = listener;
    }

    public static synchronized void startDecode(final InputStream in, final DecoderListener listener) {
        final Mp3Decoder decoder = new Mp3Decoder(listener);

        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Nullable
            @Override
            protected Void doInBackground(Void[] objects) {
                try {
                    decoder.decode(in);
                } catch (Exception e) {
                    listener.OnDecodeError(e);
                }

                listener.OnDecodeTerminated();
                return null;
            }
        };

        asyncTask.execute();

        Log.v("Mp3Decoder", "Decoding started...");
    }

	private synchronized void decode(InputStream in)
			throws BitstreamException, DecoderException {
		
		ArrayList<Short> output = new ArrayList<>(1024);

		Bitstream bitstream = new Bitstream(in);
		Decoder decoder = new Decoder();

		float total_ms = 0f;
        float nextNotify = -1f;

		boolean done = false;
		while (! done) {
			Header frameHeader = bitstream.readFrame();

            if (total_ms > nextNotify) {
                mListener.OnDecodeUpdate((int) total_ms);
                nextNotify += 500f;
            }

			if (frameHeader == null) {
				done = true;
			} else {
				total_ms += frameHeader.ms_per_frame();

				SampleBuffer buffer = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream); // CPU intense

				if (buffer.getSampleFrequency() != 44100 || buffer.getChannelCount() != 2) {
					throw new DecoderException("mono or non-44100 MP3 not supported", null);
				}

				short[] pcm = buffer.getBuffer();
				for (int i = 0; i < pcm.length-1; i += 2) {
                    short l = pcm[i];
                    short r = pcm[i+1];

                    short mono = (short) ((l + r) / 2f);

					output.add(mono); // RAM intense
				}
			}

			bitstream.closeFrame();
		}
		bitstream.close();

		mListener.OnDecodeComplete(output);
	}
}
