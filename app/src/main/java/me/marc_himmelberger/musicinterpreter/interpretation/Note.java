package me.marc_himmelberger.musicinterpreter.interpretation;

class Note {
	private final static String[] NAMES = new String[]{"A", "A#/Bb", "B", "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab"};
	
	public int frame;
	public int duration;
	public int stepsFromFixed;
	public String note;
	private double freq;
	
	public Note(int position) {
		frame = position;
	}
	
	public double getFreq() {
		return freq;
	}
	
	public void setFreq(double hz, float freqA4) {
		freq = hz;
		stepsFromFixed = calculateSteps(freqA4);
		note = lookupNote();
	}
	
	private int calculateSteps(float freqA4) {
		// calculate number of half steps up or down from A4: n
		// hz = freqA4 * root_12(2) ^ n
		float n = (float) (Math.log(freq / freqA4) / Math.log(Math.pow(2, 1d/12d)));
		
		return round(n);
	}

	private String lookupNote() {
		// convert to steps upwards
		int tmpStepsUp = stepsFromFixed;
		while (tmpStepsUp < 0)
			tmpStepsUp += 12;
		
		// convert to steps upwards in one octave
		int stepsUp = tmpStepsUp % 12;
		
		// get note name 'stepsUp' steps upwards from A, append octave (floor(steps up from C0 /12))
		return NAMES[stepsUp] + ((stepsFromFixed + 9/*to C4*/ + 48 /*to C0*/) / 12);
	}
	
	private int round(float f) {
		int tmp = (int) f;
		
		return Math.abs(f - tmp) < 0.5d ? tmp : tmp + (int)Math.signum(f);
	}

	@Override
	public String toString() {
		return "frame:" + frame + " freq:" + freq + " note:" + note + " duration:" + duration;
	}
}
