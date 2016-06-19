package me.marc_himmelberger.musicinterpreter.interpretation;

import android.util.Log;

import java.util.ArrayList;

public class Interpreter {
	// FAIL-SAFE
	private static final int MAX_NOTES = 100;
	
	// RESULTS
	public ArrayList<Note> notes = new ArrayList<>();
	
	// PACKAGE LOCAL PARAMETERS
//	int noteSensitivity = 2048; // should be in the thousands (maxima can get bigger locally because of instrument)
//	int freqWindowSizeLog2 = 11; // determines length per note for FFT, using log2(length)
//	float freqScalar = 0.5f; // scale frequencies
//	private final float freqA4 = 440f; // frequency of A4
//	int noteThreshold = 500; // at least 10 to remove noise, 500 also removes echoes
	private static final int minNoteDistance = 100; // to avoid duplicate notes

	// PRIVATE PARAMETERS
	private ArrayList<Short> mData;
	private float framesPerSecond = -1; // TODO set

	public void setData(ArrayList<Short> data) {
		mData = data;
	}

	public void analyzeNotes(int noteSensitivity, int noteThreshold) {
        if (mData == null)
            return;

		// find local maxima -> peak in waveform
		ArrayList<Integer> localMaxInd = findMaxima(mData, 1);
		
		// find local maxima in local maxima (a maxima bigger than all other maxima in some distance) -> start of notes
		ArrayList<Short> localMaxima = resolveList(localMaxInd, mData); // values of local maxima
		ArrayList<Integer> tmpNoteIndRel = findMaxima(localMaxima, noteSensitivity); // super-local maxima, index relative to localMaxInd
		ArrayList<Integer> tmpNoteIndAbs = resolveList(tmpNoteIndRel, localMaxInd); // super-local maxima, index absolute
		
		// filter "notes" with low amplitude -> noise => noteThreshold
		notes.clear();
		for (int i=0; i < tmpNoteIndAbs.size(); i++) {
			int ind = tmpNoteIndAbs.get(i);
			short amp = mData.get(ind);
			
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
			notes.get(notes.size()-1).duration = mData.size() - notes.get(notes.size()-1).frame;
	}

	public void analyzeFrequencies(int freqWindowSizeLog2, float freqScalar, float freqA4) {
		// determine frequency using FFT
		for (Note n : notes) {
			int windowSize = (int) Math.pow(2, freqWindowSizeLog2);
			Complex[] input = new Complex[windowSize];
			for (int i = 0; i < windowSize; i++) {
				input[i] = new Complex((double) mData.get(n.frame + i), 0d);
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
	}
	
	private <T> ArrayList<T> resolveList(ArrayList<Integer> indices, ArrayList<T> data) {
		ArrayList<T> resolved = new ArrayList<>();
		
		for (int i = 0; i < indices.size(); i++) {
			resolved.add(data.get(indices.get(i)));
		}
		
		return resolved;
	}
	
	private <T extends Comparable<T>> ArrayList<Integer> findMaxima(ArrayList<T> dataList, int epsilon) {
		ArrayList<Integer> maximaIndices = new ArrayList<>();
		
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
				maximaIndices.add(i);
			}
		}
		
		return maximaIndices;
	}
}
