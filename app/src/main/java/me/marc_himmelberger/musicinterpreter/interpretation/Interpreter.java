package me.marc_himmelberger.musicinterpreter.interpretation;

import java.util.ArrayList;

import android.util.Log;

public class Interpreter {
	// FAIL-SAFE
	private static final int MAX_NOTES = 100;
	
	// RESULTS
	public ArrayList<Short> data = new ArrayList<>();
	public ArrayList<Note> notes = new ArrayList<>();
	
	// PARAMETERS
	private int noteSensitivity = 2048; // should be in the thousands (maxima can get bigger locally because of instrument)
	private int freqWindowSizeLog2 = 11; // determines length per note for FFT, using log2(length)
	private float freqScalar = 0.5f; // scale frequencies
	private float freqA4 = 440f; // frequency of A4
	private int noteThreshold = 130; // at least 10 to remove noise, 500 also removes echoes
	private int minNoteDistance = 100; // to avoid duplicate notes
	private float framesPerSecond = -1;
	
	public void read(byte[] input, float frames_per_second) {
		// copy to ArrayList
		int skipEvery = Integer.MAX_VALUE;

		int frames = 0;
		for (byte b : input) {
			if (frames % skipEvery != 0)
				data.add((short) b);
			
			frames++;
		}
		framesPerSecond = frames_per_second * (1f - 1f / skipEvery);
		
		Log.i("MusicInterpreter", "Input received, length=" + data.size());
	}
	
	public void analyze() {
		// find local maxima -> peak in waveform
		ArrayList<Integer> localMaxInd = findMaxima(data, 1);
		
		// find local maxima in local maxima (a maxima bigger than all other maxima in some distance) -> start of notes
		ArrayList<Short> localMaxima = resolveList(localMaxInd, data); // values of local maxima
		ArrayList<Integer> tmpNoteIndRel = findMaxima(localMaxima, noteSensitivity); // super-local maxima, index relative to localMaxInd
		ArrayList<Integer> tmpNoteIndAbs = resolveList(tmpNoteIndRel, localMaxInd); // super-local maxima, index absolute
		
		// filter "notes" with low amplitude -> noise => noteThreshold
		notes.clear();
		for (int i=0; i < tmpNoteIndAbs.size(); i++) {
			int ind = tmpNoteIndAbs.get(i);
			short amp = data.get(ind);
			
			int lastNote = notes.size() > 0 ? notes.get(notes.size()-1).frame : -minNoteDistance;
			
			if (notes.size() > MAX_NOTES) {
				Log.i("MusicInterpreter", "MAX_NOTES exceeded, aborting...");
				return;
			} else if (amp >= noteThreshold && ind - lastNote > minNoteDistance) {
				if (notes.size() > 0)
					notes.get(notes.size()-1).duration = ind - lastNote;
				notes.add(new Note(ind));
			}
		}
		if (notes.size() > 0)
			notes.get(notes.size()-1).duration = data.size() - notes.get(notes.size()-1).frame;
		
		// determine frequency using FFT
		for (Note n : notes) {
			int windowSize = (int) Math.pow(2, freqWindowSizeLog2);
			Complex[] input = new Complex[windowSize];
			for (int i = 0; i < windowSize; i++) {
				input[i] = new Complex((double) data.get(n.frame + i), 0d);
			}
			
			// FFT
			Complex[] output = Fourier.fft(input);
			double[] outMag = new double[output.length];
			for (int i = 0; i < output.length; i++) {
				outMag[i] = output[i].abs();
			}
			
			// find maximum in first half (symmetry: outMag[i] = outMag[N-i])
			int maxInd = -1;
			double maxVal = -1;
			for (int i = 0; i < outMag.length/2; i++) {
				if (outMag[i] > maxVal) {
					maxVal = outMag[i];
					maxInd = i;
				}
			}
			
			// sinusoid frequency
			float hz = framesPerSecond * maxInd / windowSize * freqScalar;
			Log.i("MusicInterpreter",
								"Frequency identified: "
								+ hz + "Hz\n"
								+ "with " + outMag[maxInd] + "\n"
								+ "Frequencies with factor 2:\n"
								+ (hz*2) + "Hz with " + outMag[maxInd*2] + "\n"
								+ (hz/2) + "Hz with " + outMag[maxInd/2]
								);
			
			n.setFreq(hz, freqA4);
		}
		
		for (Note n : notes) {
			Log.i("MusicInterpreter", "Note found! " + n.toString());
		}
	}

	
	
	private <T> ArrayList<T> resolveList(ArrayList<Integer> indices, ArrayList<T> data) {
		ArrayList<T> resolved = new ArrayList<>();
		
		for (int i = 0; i < indices.size(); i++) {
			resolved.add(data.get(indices.get(i)));
		}
		
		return resolved;
	}
	
	private <T extends Comparable<T>> ArrayList<Integer> findMaxima(ArrayList<T> dataList, int epsilon) {
		ArrayList<Integer> maxmaIndices = new ArrayList<>();
		
		for (int i = epsilon; i < dataList.size()-epsilon; i++) {
			T t_i = dataList.get(i);
			boolean biggestBefore = true;
			boolean biggestAfter = true;
			
			for (int n = i-epsilon; n < i && biggestBefore; n++) {
				T t_n = dataList.get(n);
				if (t_i.compareTo(t_n) < 0) {
					biggestBefore = false;
				}
			}
			
			for (int n = i; n < i+epsilon && biggestAfter && biggestBefore; n++) {
				T t_n = dataList.get(n);
				if (t_i.compareTo(t_n) < 0) {
					biggestAfter = false;
				}
			}
			
			if (biggestBefore && biggestAfter) {
				maxmaIndices.add(i);
			}
		}
		
		return maxmaIndices;
	}
}
