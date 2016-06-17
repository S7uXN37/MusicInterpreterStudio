package me.marc_himmelberger.musicinterpreter.ui;

class Mp3DecoderException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2878058850493494589L;
	
	public Mp3DecoderException (String msg) {
		super(msg);
	}

	public Mp3DecoderException (Throwable e) {
		super(e);
	}
}
