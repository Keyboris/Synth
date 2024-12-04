package synth;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class GUI {
	private final JFrame frame = new JFrame();
	
	private boolean shouldGenerate;
	private int wavePos;
	
	private final AudioThread audioThread = new AudioThread(() -> {
		if (!shouldGenerate) {
			return null;
			//return new short[AudioThread.BUFFER_SIZE];
		}
		
		short[] s = new short[AudioThread.BUFFER_SIZE];
		for (int i = 0; i < AudioThread.BUFFER_SIZE; i++) {
			s[i] = (short) (Short.MAX_VALUE * Math.sin(2 * Math.PI * 440 * wavePos / AudioInfo.SAMPLE_RATE));
			wavePos++;

		}
		return s; 
	});
	

	
	GUI(){
		
		frame.addKeyListener(new KeyAdapter() {
			@Override 
			public void keyPressed(KeyEvent e) {
				if (!audioThread.isRunning()) {
					shouldGenerate = true;
					audioThread.triggerPlayBack();
				}
				System.out.println("key pressed");
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				shouldGenerate = false;
				System.out.println("key released");
			}
		});
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				audioThread.close(); 
			}
		});
		
		frame.setTitle("GOVNO BOMZHA");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(600, 400);
		frame.setResizable(false);
		frame.setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public static class AudioInfo{
		public static final int SAMPLE_RATE = 44100;
	}
}
