package synth;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import utils.Utils;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

import java.util.function.Supplier;

public class AudioThread extends Thread{
	
	static final int BUFFER_SIZE = 512;
	static final int BUFFER_COUNT = 8;
	
	private final Supplier<short[]> bufferSupplier;
	private final int[] buffers = new int[BUFFER_COUNT];
	private final long device = alcOpenDevice(alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER));
	private final long context = alcCreateContext(device, new int[1]);
	private final int source;
	
	private int bufferIndex = 0;
	private boolean closed;
	private boolean running;
	
	
	AudioThread(Supplier<short[]> bufferSupplier){
		
		this.bufferSupplier = bufferSupplier;
		alcMakeContextCurrent(context);
		AL.createCapabilities(ALC.createCapabilities(device));
		source = alGenSources();
		
		for (int i = 0; i < BUFFER_COUNT; i++) {
			bufferSamples(new short[0]);
		}
		
		alSourcePlay(source);
		catchExceptions();
		start();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	@Override
	public synchronized void run() {
		while (!closed) {
			while (!running) {
				Utils.invokeProcedure(this::wait, false);
			}
			int processedBuffers = alGetSourcei(source, AL_BUFFERS_PROCESSED);
			for (int i = 0; i < processedBuffers; i++) {
				
				short[] samples = bufferSupplier.get();
				if (samples == null) {
					running = false;
					break;
				}

				
				alDeleteBuffers(alSourceUnqueueBuffers(source));
				buffers[bufferIndex] = alGenBuffers();
				bufferSamples(samples);
				
			}
			
			if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
				alSourcePlay(source);
			}
			catchExceptions();
		}
		alDeleteSources(source);
		alDeleteBuffers(buffers);
		alcDestroyContext(context);
		alcCloseDevice(device);
		
	}
	
	private void bufferSamples(short[] samples) {
		int buffer = buffers[bufferIndex];
		alBufferData(buffer, AL_FORMAT_MONO16, samples, GUI.AudioInfo.SAMPLE_RATE);
		alSourceQueueBuffers(source, buffer);
		bufferIndex = (bufferIndex + 1) % BUFFER_COUNT;

	}
	
	synchronized void triggerPlayBack() {
		running = true;
		notify();
	}
	
	void close() {
		closed = true; //break out of the loop so we can call playback
		
		
	}
	
	private void catchExceptions() {
		int error = alcGetError(device);
		if (error != ALC_NO_ERROR) {
			throw new OpenALRuntimeException(error);
		}
	}
}
