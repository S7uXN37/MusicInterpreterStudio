package me.marc_himmelberger.musicinterpreter.ui;

import java.util.ArrayList;

abstract class DecoderListener {
    public abstract void OnDecodeComplete(ArrayList<Short> data);
    public abstract void OnDecodeUpdate(int done_ms);
    public abstract void OnDecodeError(Exception e);
    public abstract void OnDecodeTerminated();
}
