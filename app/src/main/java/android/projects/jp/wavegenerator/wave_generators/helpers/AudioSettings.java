package android.projects.jp.wavegenerator.wave_generators.helpers;

import android.media.AudioTrack;

/**
 * Created on 13/07/2017.
 */

public final class AudioSettings {

    public static final short MAX_AMPLITUDE = Short.MAX_VALUE; // 16 bit audio
    public static final int NUMBER_OF_CHANNELS = 2; //stereo
    public static final int BLOCK_ALIGN = Short.SIZE / 2 * NUMBER_OF_CHANNELS; //number of bytes per frame
    public static final double GENERATED_AUDIO_DURATION_IN_SECONDS = 0.01;
    public static final int DEVICE_CLIPPING_POINT_IN_DB = 96; //in 16-bit audio
    public static final double MAX_NORMALIZED_AMPLITUDE = 1.0;

}
