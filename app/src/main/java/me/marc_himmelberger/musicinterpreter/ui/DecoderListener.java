package me.marc_himmelberger.musicinterpreter.ui;

public abstract class DecoderListener {
    public abstract void OnDecodeComplete(short[] data);
    public abstract void OnDecodeUpdate(int done_ms);
    public abstract void OnDecodeError(Exception e);
    public abstract void OnDecodeTerminated();
}
