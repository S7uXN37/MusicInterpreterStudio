package me.marc_himmelberger.musicinterpreter.ui;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.util.Log;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

class Mp3Decoder {
	public static byte[] decode(InputStream in, int max_ms) 
			throws IOException, Mp3DecoderException {
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
		
		try (InputStream inputStream = new BufferedInputStream(in)) {
			Bitstream bitstream = new Bitstream(inputStream);
			Decoder decoder = new Decoder();
			
			float total_ms = 0f;
			
			boolean done = false;
			while (! done) {
				Header frameHeader = bitstream.readFrame();
				
				if (frameHeader == null || total_ms > max_ms) {
					done = true;
				} else {
					total_ms += frameHeader.ms_per_frame();
					
					SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
					
					if (output.getSampleFrequency() != 44100 || output.getChannelCount() != 2) {
						throw new Mp3DecoderException("mono or non-44100 MP3 not supported");
					}
					
					short[] pcm = output.getBuffer();
					for (short s : pcm) {
						int i1 = s & 0xff;
						int i2 = (s >> 8 ) & 0xff;
						
						outStream.write(i1);
						outStream.write(i2);
					}
				}
				
				bitstream.closeFrame();
			}
			
			return outStream.toByteArray();
		} catch (BitstreamException e) {
			throw new IOException("Bitstream error: " + e);
		} catch (DecoderException e) {
			Log.w("Mp3Decoder", "Decoder error", e);
			throw new Mp3DecoderException(e);
		}
	}
}
