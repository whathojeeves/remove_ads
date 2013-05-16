import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


public class Player {

	/**
	 * @param args
	 */
	
	public static String soundPath;
	static int frameWidth = 352;
	static int frameHeight = 288;
	static int frameSize = frameWidth*frameHeight*3;
	static int frameRate = 24;
	static long totalFrames;
	
	static DataReader videoData;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Player myVideoPlayer = new Player();
		
		FileInputStream soundReader = null;
		try {
			soundReader = new FileInputStream(args[1]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		soundPath = args[1];
		File vFile = new File(args[0]);
		
		videoData= new DataReader(vFile, frameSize);
		totalFrames = vFile.length()/frameSize;
		
		SoundPlayer playSound = new SoundPlayer(soundReader);
		myVideoPlayer.videoPlayer();
	}

	public void videoPlayer()
	{
		CanvasFrame videoCanvas = new CanvasFrame("MyVideo");
		
		BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_4BYTE_ABGR);
		
		long numFrame;
		long prev=0;
		long wait = 0;
		
		/* Test 
		JFrame frame = new JFrame();
	    JLabel label = new JLabel(new ImageIcon(currentFrame));
	    frame.getContentPane().add(label, BorderLayout.CENTER);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
	    
	    /* Test done*/
	    
	    for(numFrame=0; numFrame<totalFrames; numFrame++)
		{
	    	//System.out.println("Frame "+numFrame);
			videoData.readFrame(currentFrame, frameSize, frameWidth, frameHeight);
			
			/* Test To check how the buffered images look */
			//frame.repaint();
			/* Test done */
			
			IplImage currentFrameImg = IplImage.createFrom(currentFrame);
			
			if(numFrame == 0)
			{
				prev = System.currentTimeMillis();
			}
			long cur = System.currentTimeMillis();
			if(cur - prev < 35)
			{
				wait = 35 - (cur-prev);
				
				try {
					Thread.currentThread().sleep(wait);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			videoCanvas.showImage(currentFrameImg);
			prev = System.currentTimeMillis();
			
			/*KeyEvent key=null;
			try {
				key = videoCanvas.waitKey(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(key!=null)
				break;*/
			
			currentFrameImg.release();
		}
	
}
}
