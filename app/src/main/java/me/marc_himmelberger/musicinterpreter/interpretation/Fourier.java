package me.marc_himmelberger.musicinterpreter.interpretation;

class Fourier {
	// compute the FFT of data[], assuming its length is a power of 2
    public static Complex[] fft(Complex[] data) {
        int N = data.length;

        // base case
        if (N == 1) return new Complex[] { data[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = data[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            odd[k] = data[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
}
