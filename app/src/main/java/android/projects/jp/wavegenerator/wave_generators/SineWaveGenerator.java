package android.projects.jp.wavegenerator.wave_generators;

import                 android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;
import android.widget.Toast;

public class SineWaveGenerator {

    private final String TAG = this.getClass().getSimpleName();
    private AudioTrack mAudioTrack;
    public static final int MAX_FREQUENCY = AudioSettings.MAX_FREQ;
    public static final int FREQUENCY_STEP = AudioSettings.FREQ_STEP;
    private final double mSampleRate = AudioSettings.SAMPLE_RATE;;
    private int mFrequency;
    private double mLeftAmplitude;
    private double mRightAmplitude;
    private double mCurrentAmplitudeFadeFactor;
    private double mAmplitudeFadeIncrement;
    private short[] mBufferStereo;
    private int mBufferSize;
    private boolean mPlaying;
    private boolean mFadingOut;


    public SineWaveGenerator() {
        mFrequency = AudioSettings.DEFAULT_FREQUENCY;
        mLeftAmplitude = dBToAmplitude(AudioSettings.DEFAULT_VOLUME);
        mRightAmplitude = dBToAmplitude(AudioSettings.DEFAULT_VOLUME);
    }

    private void init(){

        int minBufferSize= AudioTrack.getMinBufferSize((int)AudioSettings.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        double duration = minBufferSize / AudioSettings.SAMPLE_RATE;
        //buffersize should be multiple of sample rate to avoid clicking during playback
        duration  = roundToNDecimalCases(duration, 1);

        mBufferSize = (int) (duration * AudioSettings.SAMPLE_RATE);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                (int)mSampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize,
                AudioTrack.MODE_STREAM);

        Log.i(TAG, "AudioTrack successfully initialized with " + mSampleRate + " sample rate.");
        setUpToneFade();
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

        if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            mAudioTrack.play();
            mPlaying = true;
        }

        final Thread thread = new Thread(new Runnable() {
            public void run() {

                    while(mPlaying)
                    {
                        buildStereoBuffer(mBufferSize);
                        mAudioTrack.write(mBufferStereo, 0, mBufferStereo.length);
                    }
            }
        });
        thread.start();
        Log.i(TAG, "AudioTrack started playing at " + mFrequency + " hZ." );

    }

    private double preventClipping(double amplitude){

        return amplitude > AudioSettings.MAX_NORMALIZED_AMPLITUDE ? AudioSettings.MAX_NORMALIZED_AMPLITUDE : amplitude;
    }

    private void buildStereoBuffer(int bufferSize) {

        mBufferStereo = new short[bufferSize];

        for(int sampleIndex = 0, sampleNumber = 0; sampleIndex < mBufferStereo.length ; sampleIndex += 2, sampleNumber++){
            double time = sampleNumber / mSampleRate;
            double wavePoint = Math.sin(2.0  * Math.PI * mFrequency * time) * AudioSettings.MAX_AMPLITUDE  * mCurrentAmplitudeFadeFactor;
            mBufferStereo[sampleIndex] = (short)(wavePoint * mLeftAmplitude) ;
            mBufferStereo[sampleIndex + 1] = (short)(wavePoint * mRightAmplitude) ;
            // Fade in and fade out of amplitude over 10% of samples to avoid clicks when starting and stopping playback
            applyAmplitudeFade();
        }
    }

    private void applyAmplitudeFade()
    {
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

    public void setLeftVolume(int volume){
        mLeftAmplitude = dBToAmplitude(volume);
    }

    public void setRightVolume(int volume){
        mRightAmplitude = dBToAmplitude(volume);
    }

    public int getLeftVolume(){
        return amplitudeToDb(mLeftAmplitude);
    }

    public int getRightVolume(){
        return amplitudeToDb(mRightAmplitude);
    }

    public int getMaxVolume(){
        return AudioSettings.DEVICE_CLIPPING_POINT_IN_DB;
    }

    private double dBToAmplitude(double dB) {
        //- sign because resulting value will be the difference from reference max dB level
        return Math.pow(10, -(AudioSettings.DEVICE_CLIPPING_POINT_IN_DB - (dB)) / 20.0);
    }

    private int amplitudeToDb(double amplitude)
    {
        return (int)(20 * Math.log10(amplitude) + 96);// 0 amplitude should yield -96
    }

}
