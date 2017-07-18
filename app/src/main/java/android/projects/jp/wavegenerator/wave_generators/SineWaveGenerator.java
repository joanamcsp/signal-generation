package android.projects.jp.wavegenerator.wave_generators;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class SineWaveGenerator {

    private final String TAG = this.getClass().getSimpleName();
    private final int DEVICE_CLIPPING_POINT_IN_DB = 96;
    private final double SAMPLE_RATE = 44100;
    private int mFrequency = ControlsConfiguration.DEFAULT_FREQUENCY;
    private double mLeftAmplitude = dBToAmplitude(ControlsConfiguration.DEFAULT_VOLUME);
    private double mRightAmplitude = dBToAmplitude(ControlsConfiguration.DEFAULT_VOLUME);
    private AudioTrack mAudioTrack;
    private int mBufferSize;
    private double mCurrentAmplitudeFadeFactor;
    private double mAmplitudeFadeIncrement;
    private boolean mPlaying;
    private boolean mFadingOut;

    private void init(){

        int minBufferSize = AudioTrack.getMinBufferSize((int) SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        double duration = minBufferSize / SAMPLE_RATE;
        //buffersize should be multiple of sample rate to avoid clicking during playback
        duration  = roundToNDecimalCases(duration, 1);

        mBufferSize = (int) (duration * SAMPLE_RATE);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                (int)SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize,
                AudioTrack.MODE_STREAM);
        Log.i(TAG, "AudioTrack successfully initialized with " + SAMPLE_RATE + " sample rate.");
        setUpToneFade( );
    }

    private double roundToNDecimalCases(double value, int n){
        double factor = 10 * n;
        return Math.round(value * factor) / factor;
    }

    private void setUpToneFade(){
        //fade in and out over 10 % first and last sample
        double amplitudeFadeRamp = mBufferSize / 10;
        mAmplitudeFadeIncrement = 1 / amplitudeFadeRamp;
        mCurrentAmplitudeFadeFactor = 0;
        mFadingOut = false;
    }

    public void playTone(){

        init();

        preventClipping(mLeftAmplitude);
        preventClipping(mRightAmplitude);

        try{
            if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                mAudioTrack.play();
                mPlaying = true;
            }
        }catch (IllegalArgumentException e){
            throw e;
        }

        Thread thread = new Thread(new Runnable() {
            public void run() {

                short[] bufferStereo;
                while(mPlaying)
                {
                    bufferStereo = buildStereoBuffer();
                    mAudioTrack.write(bufferStereo, 0, bufferStereo.length);
                }
            }
        });
        thread.start();
        Log.i(TAG, "AudioTrack started playing at " + mFrequency + " hZ." );
    }

    private double preventClipping(double amplitude){

        return amplitude > dBToAmplitude(DEVICE_CLIPPING_POINT_IN_DB) ? dBToAmplitude(DEVICE_CLIPPING_POINT_IN_DB) : amplitude;
    }

    private short[]  buildStereoBuffer() {

        short[] bufferStereo = new short[mBufferSize];
        double time;
        double sample;
        short fullAmplitude = Short.MAX_VALUE;

        for(int sampleIndex = 0, sampleNumber = 0; sampleIndex <  bufferStereo.length ; sampleIndex += 2, sampleNumber++){
            time = sampleNumber / SAMPLE_RATE;
            sample = getSineWavePoint(time) * fullAmplitude * mCurrentAmplitudeFadeFactor;
            bufferStereo[sampleIndex] = (short)(sample * mLeftAmplitude) ;
            bufferStereo[sampleIndex + 1] = (short)(sample * mRightAmplitude) ;
            applyAmplitudeFade();
        }
        return bufferStereo;
    }

    private double getSineWavePoint(double time){
        return Math.sin(2.0 * Math.PI * mFrequency * time);
    }

    private void applyAmplitudeFade()
    {
        // Fade in and fade out of amplitude over 10% of samples to avoid clicks when starting and stopping playback
        if (mFadingOut)
        {
            if (mCurrentAmplitudeFadeFactor > mAmplitudeFadeIncrement)
            {
                mCurrentAmplitudeFadeFactor -= mAmplitudeFadeIncrement;
            }
        }
        else
        {
            if (mCurrentAmplitudeFadeFactor < 1.0 - mAmplitudeFadeIncrement)
            {
                mCurrentAmplitudeFadeFactor += mAmplitudeFadeIncrement;

            }
        }
    }

    public void stop(){
        close();
        Log.i(TAG, "AudioTrack released");
    }

    private void close() {
        if(mAudioTrack == null)return;
        if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
            mFadingOut = true;
            mAudioTrack.stop();
            mAudioTrack.release();
            mPlaying = false;
        }
        Log.i(TAG, "AudioTrack stopped");
    }

    public boolean isPlaying(){
        return mPlaying;
    }

    public int getFrequency(){
        return mFrequency;
    }

    public void setFrequency(int frequency) {
        mFrequency = frequency;
    }

    public int getMaxFrequency(){
        return ControlsConfiguration.MAX_FREQUENCY;
    }

    public int getFrequencyStep(){
        return ControlsConfiguration.FREQUENCY_STEP;
    }

    public int getLeftVolume(){
        return amplitudeToDb(mLeftAmplitude);
    }

    public void setLeftVolume(int volume){
        mLeftAmplitude = dBToAmplitude(volume);
    }

    public int getRightVolume(){
        return amplitudeToDb(mRightAmplitude);
    }

    public void setRightVolume(int volume){
        mRightAmplitude = dBToAmplitude(volume);
    }

    public int getMaxVolume(){
        return DEVICE_CLIPPING_POINT_IN_DB;
    }

    private double dBToAmplitude(double dB) {
        //- sign because resulting value will be the difference from reference max dB level
        return Math.pow(10, -(DEVICE_CLIPPING_POINT_IN_DB - (dB)) / 20.0);
    }

    private int amplitudeToDb(double amplitude)
    {
        return (int)(20 * Math.log10(amplitude) + 96);// 0 amplitude should yield -96
    }

}
