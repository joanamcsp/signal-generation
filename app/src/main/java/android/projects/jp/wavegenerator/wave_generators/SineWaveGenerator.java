package android.projects.jp.wavegenerator.wave_generators;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created on 10/07/2017.
 */

public class SineWaveGenerator {
    private AudioTrack mAudioTrack;
    private final short FULL_AMPLITUDE = Short.MAX_VALUE;
    private final int BYTES_PER_SAMPLE = 2;//16 bit audio
    private final int NUMBER_OF_CHANNELS = 2; //stereo
    private int mBufferLength;
    private double mSampleRate;
    private final int mSamplesPerFrame = 2 * NUMBER_OF_CHANNELS ;
    private byte[] byteBufferStereo;
    private int mToneDurationInSeconds;


    public SineWaveGenerator(double sampleRate, int toneDurationInSeconds) {

        mSampleRate = sampleRate;
        mToneDurationInSeconds = toneDurationInSeconds;

    }


    private void init(){

        mBufferLength = (int)(mToneDurationInSeconds * mSampleRate);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                (int)mSampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                mBufferLength * mSamplesPerFrame,
                AudioTrack.MODE_STATIC);


        mAudioTrack.setNotificationMarkerPosition(mBufferLength);

        mAudioTrack.setPlaybackPositionUpdateListener(
                new AudioTrack.OnPlaybackPositionUpdateListener() {
                    public void onPeriodicNotification(AudioTrack track) {
                    }

                    public void onMarkerReached(AudioTrack track) {

                        mAudioTrack.release();

                    }
                }
        );

    }

    private void release(){
        stop();
        mAudioTrack.release();
    }

    /// <summary>
    ///  Applies amplitude ramp in first and last 10% of samples to avoid clicks in audio
    /// (stable amplitude starts at sample 4410 at 44100 sr)
    /// </summary>
    /// <param name="bufferLeft">amplified wave points computed as float to avoid distortion</param>
    /// <param name="bufferRight">amplified wave points computed as float to avoid distortion</param>
    /// <param name="byteBufferLeft">final sound buffer 2-byte samples (16 bit audio)</param>
    /// <param name="byteBufferRight">final sound buffer 2-byte samples (16 bit audio)</param>
    private void ApplyAmplitudeToSamples(float[] bufferLeft, float[] bufferRight, byte[] byteBufferLeft, byte[] byteBufferRight)
    {
        //sound wave samples must be calculated in double / float and stored as short to avoid distortion (in 16-bit audio)
        double amplifiedSampleLeft;
        double amplifiedSampleRight;
        short finalAmplifiedSampleLeft;
        short finalAmplifiedSampleRight;

        int ramp = byteBufferLeft.length / 20;

        //apply amplitude ramp to each channel
        for (int i = 0, j = 0, bufferIndexLeft = 0, bufferIndexRight = 0; i < byteBufferLeft.length; i++, j++)
        {
            amplifiedSampleLeft = bufferLeft[bufferIndexLeft++] * FULL_AMPLITUDE ;
            amplifiedSampleRight = bufferRight[bufferIndexRight++] * FULL_AMPLITUDE;

            //fade in
            if (i < ramp)
            {
                amplifiedSampleLeft = amplifiedSampleLeft * i / ramp;
                amplifiedSampleRight = amplifiedSampleRight * j / ramp;
            }
            //fade out
            else if (i >= byteBufferLeft.length - ramp)
            {
                amplifiedSampleLeft = amplifiedSampleLeft * (byteBufferLeft.length - i) / ramp;
                amplifiedSampleRight = amplifiedSampleRight * (byteBufferRight.length - j) / ramp;
            }

            // in 16 bit wav PCM, first byte is the low order byte
            finalAmplifiedSampleLeft = (short)amplifiedSampleLeft;
            byteBufferLeft[i] = (byte)(finalAmplifiedSampleLeft & 0x00ff); // low
            byteBufferLeft[++i] = (byte)((finalAmplifiedSampleLeft & 0xff00) >> 8);  // high

            finalAmplifiedSampleRight = (short)amplifiedSampleRight;
            byteBufferRight[j] = (byte)(finalAmplifiedSampleRight & 0x00ff); // low
            byteBufferRight[++j] = (byte)((finalAmplifiedSampleRight & 0xff00) >> 8);  // high
        }
    }

    public void playTone(int frequency, int channel, double amplitude){

        init();
        //avoid clipping
        amplitude = amplitude > 1 ? 1 : amplitude;

        //control amplitude in L and R channel
        double leftAmplitude = channel == 1 ? amplitude : 0;
        double rightAmplitude = channel == 1 ? 0 : amplitude;

        int sample ;

        float[] bufferLeft = new float[mBufferLength];
        float[] bufferRight = new float[mBufferLength];

        //Prepare buffers for amplified samples (two bytes per sample in 16 bit size)
        byte[] byteBufferLeft = new byte[mBufferLength * BYTES_PER_SAMPLE];
        byte[] byteBufferRight = new byte[mBufferLength * BYTES_PER_SAMPLE];

        //LL RR LL RR 4 bytes per audio frame (2 bytes per sample * 2 channels)
        //stereo PCM audio is interleaved
        byteBufferStereo = new byte[mBufferLength * BYTES_PER_SAMPLE * NUMBER_OF_CHANNELS];

        //generate sine wave
        for (sample = 0; sample < bufferLeft.length; sample++)
        {
            double time = sample / mSampleRate;
            bufferLeft[sample] = (float)(leftAmplitude * Math.sin(2* Math.PI * frequency * time ));
            bufferRight[sample] = (float)(rightAmplitude * Math.sin(2* Math.PI * frequency * time ));

        }

        ApplyAmplitudeToSamples(bufferLeft, bufferRight, byteBufferLeft, byteBufferRight);

        //fill final stereo array for playback
        for (int k = 0, index = 0; index < byteBufferStereo.length - 4; k += 2)
        {
            byteBufferStereo[index] = byteBufferLeft[k]; // LEFT
            byteBufferStereo[index + 1] = byteBufferLeft[k + 1];
            index += 2;

            byteBufferStereo[index] = byteBufferRight[k]; // RIGHT
            byteBufferStereo[index + 1] = byteBufferRight[k + 1];
            index += 2;
        }

        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                mAudioTrack.write(byteBufferStereo, 0, byteBufferStereo.length);


                if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                    mAudioTrack.play();
                }
            }
        });
        thread.start();
    }

    public void setToneDuration(int toneDurationInSeconds){
        mToneDurationInSeconds = toneDurationInSeconds;
    }

    public void stop(){
        if(mAudioTrack == null)return;
        if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
            mAudioTrack.stop();
        }
    }

}
