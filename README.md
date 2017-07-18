# README #


Simple experimental app to learn about PCM audio generation on mobile devices (Android). 
Can be used to tune musical instruments or to screen for hearing sensitivity to specific frequencies. 
Volume values are in dB in reference to the device clipping point, with a maximum value of 96dB (0 - 96dB) 
since 16-bit audio is used. Frequency and volume steps can be tuned as needed.
The sine wave samples are generated in stereo mode with the ability play L and R channel independently.

Some important references:

https://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
https://stackoverflow.com/questions/20461243/how-to-play-two-sine-wave-on-left-and-right-channel-separately-with-16-bit-forma