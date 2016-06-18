package me.marc_himmelberger.musicinterpreter.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.util.Log;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

class Mp3Decoder {
    private DecoderListener mListener;

    private Mp3Decoder (DecoderListener listener) {
        mListener = listener;
    }

    public static synchronized void startDecode(final InputStream in, final int max_ms, final DecoderListener listener) {
        final Mp3Decoder decoder = new Mp3Decoder(listener);
        Thread decoderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    decoder.decode(in, max_ms);
                } catch (Exception e) {
                    listener.OnDecodeError(e);
                }

                listener.OnDecodeTerminated();
            }
        }, "Mp3Decoder");

        decoderThread.start();
        Log.v("Mp3Decoder", "Decoding started...");
    }

	private synchronized void decode(InputStream in, int max_ms)
			throws BitstreamException, DecoderException, IOException {
		
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

			if (frameHeader == null || total_ms > max_ms) {
				done = true;
			} else {
				total_ms += frameHeader.ms_per_frame();

				SampleBuffer buffer = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);

				if (buffer.getSampleFrequency() != 44100 || buffer.getChannelCount() != 2) {
					throw new DecoderException("mono or non-44100 MP3 not supported", null);
				}

				short[] pcm = buffer.getBuffer();
				for (short s : pcm) {
					output.add(s);
				}
			}

			bitstream.closeFrame();
		}
		bitstream.close();

        short[] outputArray = new short[output.size()];
        for (int i = 0; i < outputArray.length; i++) {
            outputArray[i] = output.get(i);
        }

		mListener.OnDecodeComplete(outputArray);
	}
}
