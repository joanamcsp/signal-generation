package android.projects.jp.wavegenerator;

import android.projects.jp.wavegenerator.wave_generators.ControlsConfiguration;
import android.projects.jp.wavegenerator.wave_generators.SineWaveGenerator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    SineWaveGenerator sineGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sineGenerator = new SineWaveGenerator();
        bindControls();
    }

    private void bindControls(){

        final TextView currentFrequency = (TextView) findViewById(R.id.frequency);
        currentFrequency.setText(String.valueOf(sineGenerator.getFrequency()));

        Button buttonFrequencyIncrease = (Button) findViewById(R.id.buttonFrequencyIncrease);
        buttonFrequencyIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(currentFrequency.getText().toString());
                if(frequency + sineGenerator.getFrequencyStep() <= sineGenerator.getMaxFrequency()){
                    frequency += sineGenerator.getFrequencyStep();
                    sineGenerator.setFrequency(frequency);
                    currentFrequency.setText(String.valueOf(frequency));
                }
            }
        });

        Button buttonFrequencyDecrease = (Button) findViewById(R.id.buttonFrequencyDecrease);
        buttonFrequencyDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(currentFrequency.getText().toString());
                if(frequency - sineGenerator.getFrequencyStep() >= 0){
                    frequency  -= sineGenerator.getFrequencyStep();
                    sineGenerator.setFrequency(frequency);
                    currentFrequency.setText(String.valueOf(frequency));
                }
            }
        });

        int volumeLeft = sineGenerator.getLeftVolume();
        final TextView currentVolumeLeft = (TextView) findViewById(R.id.currentVolumeLeft);
        currentVolumeLeft.setText(String.valueOf(volumeLeft));

        final SeekBar volumeLeftSlider = (SeekBar) findViewById(R.id.volumeLeftSlider);
        volumeLeftSlider.setMax(sineGenerator.getMaxVolume());
        volumeLeftSlider.setProgress(volumeLeft);
        volumeLeftSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                currentVolumeLeft.setText(String.valueOf(progress));
                sineGenerator.setLeftVolume(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int volumeRight = sineGenerator.getRightVolume();
        final TextView currentVolumeRight = (TextView) findViewById(R.id.currentVolumeRight);
        currentVolumeRight.setText(String.valueOf(volumeRight));

        final SeekBar volumeRightSlider = (SeekBar) findViewById(R.id.volumeRightSlider);
        volumeRightSlider.setMax(sineGenerator.getMaxVolume());
        volumeRightSlider.setProgress(volumeRight);
        volumeRightSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                currentVolumeRight.setText(String.valueOf(progress));
                sineGenerator.setRightVolume(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final Button buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(sineGenerator.isPlaying()){
                    sineGenerator.stop();
                    buttonPlay.setText("Play");
                } else {
                    sineGenerator.playTone();
                    buttonPlay.setText("Stop");
                }
            }
        });

    }

}
