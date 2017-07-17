package android.projects.jp.wavegenerator.wave_generators;

import android.media.AudioTrack;

/**
 * Created on 13/07/2017.
 */

public final class AudioSettings {

    protected static final double SAMPLE_RATE = 44100.0;
    protected static final int DEVICE_CLIPPING_POINT_IN_DB = 96; //in 16-bit audio
    protected static final int MAX_AMPLITUDE = Short.MAX_VALUE;
    protected static final double MAX_NORMALIZED_AMPLITUDE = 1.0;
    protected static final int NUMBER_OF_CHANNELS = 2; //stereo
    protected static final int BLOCK_ALIGN = 2 * NUMBER_OF_CHANNELS; //number of bytes per frame
    protected static final int DEFAULT_FREQUENCY = 500;
    protected static final int MAX_FREQ = 20000;
    protected static final int FREQ_STEP = 100;
    protected static final int DEFAULT_VOLUME = 50;

}
