import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DataReader {
	
	File videoFile;
	InputStream fileReader;
	int currentFrameNumber;
	
	/* Re-usable variables */
	byte[] frameDataBuffer;
	byte r,g,b;
	int pixelVal;
	
	public DataReader(File videoDataFile, int bufferSize)
	{
		videoFile = videoDataFile;
		try {
			fileReader = new FileInputStream(videoFile);
			fileReader.mark(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		frameDataBuffer = new byte[bufferSize];
	}
	
	public void readFrame(BufferedImage currentFrame, int frameSize, int frameWidth, int frameHeight)
	{
		int x=0,y=0,iter=0;
		try {
			
			fileReader.read(frameDataBuffer, 0, frameDataBuffer.length);
			
			for(y=0; y<frameHeight; y++)
			{
				for(x=0; x<frameWidth; x++)
				{
					r = frameDataBuffer[iter + (frameSize/3)*2];
					g = frameDataBuffer[iter + (frameSize/3)];
					b = frameDataBuffer[iter];
					
					pixelVal = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					currentFrame.setRGB(x, y, pixelVal);
					iter++;
				}
			}
			currentFrameNumber++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void readFrameBytes(int frameSize, int frameWidth, int frameHeight, byte[] readData)
	{
		try {
			
			System.out.println("Normal Reading "+fileReader.read(readData, 0, readData.length)+" bytes");
			
			currentFrameNumber++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void skipToFrameNumberBytes(long frameNum, int frameSize, int frameWidth, int frameHeight, byte[] readData)
	{
		try {
			
			System.out.println("Current frame :"+currentFrameNumber+" to skip till "+frameNum);
			System.out.println("Skipping "+fileReader.skip((frameNum-currentFrameNumber)*(frameSize)) + " bytes");
			
			System.out.println("Skipped Reading "+fileReader.read(readData, 0, readData.length)+" bytes");;
			currentFrameNumber += frameNum-currentFrameNumber;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
