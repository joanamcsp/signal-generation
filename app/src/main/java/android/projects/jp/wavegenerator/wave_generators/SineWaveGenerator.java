package android.projects.jp.wavegenerator.wave_generators;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.projects.jp.wavegenerator.wave_generators.helpers.AudioSettings;
import android.util.Log;

/**
 * Created on 10/07/2017.
 */

public class SineWaveGenerator {

    private AudioTrack mAudioTrack;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private int mNumberOfSamplesPerChannel;
    private double mSampleRate;
    private short[] bufferStereo;
    private boolean mPlaying;
    private int mFrequency;
    private double mLeftAmplitude;
    private double mRightAmplitude;
    private double currentAmplitudeFadeFactor = 0;
    private double mAmplitudeFadeIncrement;
    private boolean mFadingOut;


    public SineWaveGenerator(double sampleRate) {

        mSampleRate = sampleRate;
        //TODO: filter for unsupported values
    }

    private void init(){

        mNumberOfSamplesPerChannel = (int)(AudioSettings.GENERATED_AUDIO_DURATION_IN_SECONDS * mSampleRate);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                (int)mSampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                mNumberOfSamplesPerChannel * AudioSettings.BLOCK_ALIGN,
                AudioTrack.MODE_STREAM);

        Log.i(this.toString(), "AudioTrack successfully initialized");

        mFadingOut = false;
        currentAmplitudeFadeFactor = 0;
    }

    private void reset(){
        if(mAudioTrack == null)return;
        if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
            mFadingOut = true;
            mPlaying = false;
            mAudioTrack.stop();
        }
        Log.d(this.toString(), "AudioTrack stopped");
    }

    private double dBToAmplitude(double dB)
    {
        //- sign because resulting value will be the difference from reference max dB level
        return Math.pow(10, -(AudioSettings.DEVICE_CLIPPING_POINT_IN_DB - (dB)) / 20.0);
    }

    private void buildStereoBuffer(int numberOfSamplesPerChannel) {

        bufferStereo = new short[numberOfSamplesPerChannel * AudioSettings.NUMBER_OF_CHANNELS];
        double ramp = bufferStereo.length / 20;
        mAmplitudeFadeIncrement = 1 / ramp;

        for(int sample = 0; sample < bufferStereo.length; sample += 2){
            double time = sample / mSampleRate;
            short wavePoint = (short)(Math.sin(2 * Math.PI * mFrequency * time)  *  AudioSettings.MAX_AMPLITUDE * currentAmplitudeFadeFactor);
            bufferStereo[sample] = (short)(wavePoint * mLeftAmplitude);
            bufferStereo[sample + 1] = (short)(wavePoint * mRightAmplitude);
        }
        applyAmplitudeFade();
    }

    /// <summary>
    /// Fade in and fade out of amplitude over 10% of samples to avoid clicks when starting and stopping playback
    /// </summary>
    private void applyAmplitudeFade()
    {
        if (mFadingOut)
        {
            if (currentAmplitudeFadeFactor > 0 + mAmplitudeFadeIncrement)
            {
                currentAmplitudeFadeFactor -= mAmplitudeFadeIncrement;
            }
        }
        else
        {
            if (currentAmplitudeFadeFactor < 1 - mAmplitudeFadeIncrement)
            {
                currentAmplitudeFadeFactor += mAmplitudeFadeIncrement;
            }
        }
    }

    private double preventClipping(double amplitude){

        return amplitude > AudioSettings.MAX_NORMALIZED_AMPLITUDE ? AudioSettings.MAX_NORMALIZED_AMPLITUDE : amplitude;
    }

    public void playTone(int frequency, double  leftVolume, double rightVolume){

        init();

        double leftAmplitude = dBToAmplitude(leftVolume);
        double rightAmplitude = dBToAmplitude(rightVolume);
        Log.d(LOG_TAG, "Left amplitude: " + leftAmplitude + " Right amplitude: " + rightAmplitude);


        preventClipping(leftAmplitude);
        preventClipping(rightAmplitude);

        mFrequency = frequency;
        mLeftAmplitude = leftAmplitude;
        mRightAmplitude = rightAmplitude;

        if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            mAudioTrack.play();
            mPlaying = true;
        }

        final Thread thread = new Thread(new Runnable() {
            public void run() {

                while(mPlaying)
                {
                    buildStereoBuffer(mNumberOfSamplesPerChannel);
                    mAudioTrack.write(bufferStereo, 0, bufferStereo.length);
                }

            }
        });
        thread.start();
        Log.v(this.toString(), "AudioTrack playing...");

    }

    public void stop(){
        reset();
        mAudioTrack.release();
        Log.d(this.toString(), "AudioTrack released");
    }

    public boolean isPlaying(){
        return mPlaying;
    }

    public void setmLeftAmplitude(int amplitude){
        mLeftAmplitude = dBToAmplitude(amplitude);
    }

    public void setmRightAmplitude(int amplitude){
        mRightAmplitude = dBToAmplitude(amplitude);
    }

    public void setmFrequency(int mFrequency) {
        this.mFrequency = mFrequency;
    }
}
