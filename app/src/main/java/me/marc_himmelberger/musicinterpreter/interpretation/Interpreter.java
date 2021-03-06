package me.marc_himmelberger.musicinterpreter.interpretation;

import android.util.Log;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class Interpreter {
	// FAIL-SAFE
	private static final int MAX_NOTES = 100;
	
	// RESULTS
	public final ArrayList<Note> mNotes = new ArrayList<>();
	
	// PACKAGE LOCAL PARAMETERS
//	int noteSensitivity = 2048; // should be in the thousands (maxima can get bigger locally because of instrument)
//	int freqWindowSizeLog2 = 11; // determines length per note for FFT, using log2(length)
//	float freqScalar = 0.5f; // scale frequencies
//	private final float freqA4 = 440f; // frequency of A4
//	int noteThreshold = 500; // at least 10 to remove noise, 500 also removes echoes
	private static final int minNoteDistance = 100; // to avoid duplicate notes
    private static final float framesPerSecond = 44100f;

	// PRIVATE PARAMETERS
	private ArrayList<Short> mData;

	public void setData(ArrayList<Short> data) {
		mData = data;
	}

	public void analyzeNotes(int noteSensitivity, float relNoteThreshold) {
        if (mData == null)
            return;

		// find local maxima -> peak in waveform
		ArrayList<Integer> localMaxInd = findMaxima(mData, 1);
		
		// find local maxima in local maxima (a maxima bigger than all other maxima in some distance) -> start of notes
		ArrayList<Short> localMaxima = resolveList(localMaxInd, mData); // values of local maxima
		ArrayList<Integer> tmpNoteIndRel = findMaxima(localMaxima, noteSensitivity); // super-local maxima, index relative to localMaxInd
		ArrayList<Integer> tmpNoteIndAbs = resolveList(tmpNoteIndRel, localMaxInd); // super-local maxima, index absolute

		short globalMax = Short.MIN_VALUE;
		for (short s : localMaxima) {
			if (s > globalMax)
				globalMax = s;
		}
		int noteThreshold = (int) (globalMax * relNoteThreshold);

		// filter "notes" with low amplitude -> noise => noteThreshold
		mNotes.clear();
		for (int i=0; i < tmpNoteIndAbs.size(); i++) {
			int ind = tmpNoteIndAbs.get(i);
			short amp = mData.get(ind);
			
			int lastNote = mNotes.size() > 0 ? mNotes.get(mNotes.size()-1).frame : -minNoteDistance;
			
			if (mNotes.size() > MAX_NOTES) {
				Log.i("MusicInterpreter", "MAX_NOTES exceeded, aborting...");
				return;
			} else if (amp >= noteThreshold && ind - lastNote > minNoteDistance) {
				if (mNotes.size() > 0)
					mNotes.get(mNotes.size()-1).duration = ind - lastNote;
				mNotes.add(new Note(ind));
			}
		}
		if (mNotes.size() > 0) {
			Note lastNote = mNotes.get(mNotes.size() - 1);
			float reqAmp = 0.1f * mData.get(lastNote.frame);

			for (int i = lastNote.frame; i < mData.size(); i++) {
				if (mData.get(i) < reqAmp)
					lastNote.duration = i - lastNote.frame;
			}
		}
	}

	public void analyzeFrequencies(int freqWindowSizeLog2, float freqA4, float minRelAmp, ProgressBar progressBar) {
		progressBar.setProgress(0);

		ArrayList<Note> newNotes = new ArrayList<>();

		// determine frequency using FFT
		for (Note n : mNotes) {
			progressBar.setProgress(progressBar.getProgress() + 1);

			int windowSize = (int) Math.pow(2, freqWindowSizeLog2);
			Complex[] input = new Complex[windowSize];

			for (int i = 0; i < windowSize; i++) {
				input[i] = new Complex(mData.get(n.frame + i), 0d);
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
			float hz = framesPerSecond * maxInd / windowSize;

			Log.i("MusicInterpreter",
					"Frequency identified: "
							+ hz + "Hz\n"
							+ "with " + outMag[maxInd] + "\n"
							+ "Frequencies with factor 2:\n"
							+ (hz*2) + "Hz with " + outMag[maxInd*2] + "\n"
							+ (hz/2) + "Hz with " + outMag[maxInd/2]
			);

			n.setFreq(hz, freqA4);

            // find other notes, with >minRelAmp amplitude and not at indices k*maxInd
            int scnd_maxInd = -1;
            double scnd_maxVal = -1;
            for (int i = 0; i < outMag.length/2; i++) {
                boolean isOvertone = (i % maxInd == 0);

                if (outMag[i] > scnd_maxVal && outMag[i] > minRelAmp * maxVal && !isOvertone) {
                    scnd_maxVal = outMag[i];
                    scnd_maxInd = i;
                }
            }

            if (scnd_maxInd != -1) {
                float scnd_hz = framesPerSecond * scnd_maxInd / windowSize;
                Note scnd_note = new Note(n.frame);
                scnd_note.setFreq(scnd_hz, freqA4);
                scnd_note.duration = n.duration;

                newNotes.add(scnd_note);
            }
		}

//		SortedSet<Note> sortedNotes = new TreeSet<>(new Comparator<Note>() {
//			@Override
//			public int compare(Note n1, Note n2) {
//				if (n1.frame == n2.frame)
//					return 1;
//				else
//					return Integer.compare(n1.frame, n2.frame);
//			}
//		});
//		for (Note n : newNotes) {
//			sortedNotes.add(n);
//		}
//		for (Note n : mNotes) {
//			sortedNotes.add(n);
//		}
//
//		mNotes.clear();
		for (Note n : newNotes /*sortedNotes*/) {
			mNotes.add(n);
		}

        progressBar.setProgress(progressBar.getMax());
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

	public void shiftNotes(int amount) {
		for (Note n : mNotes) {
			n.shift(amount);
		}
	}
}
