package project576;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImageArray;
import com.googlecode.javacv.cpp.opencv_imgproc.CvHistogram;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

public class Histogram 
{
	static DataReader videoDataReader;
	static long totalFrames;
	
	public Histogram(File videoFile, int frameSize, long totFrames)
	{
		videoDataReader = new DataReader(videoFile, frameSize);
		totalFrames = totFrames;
	}
	public void calculateMetricRGBHist(int frameWidth, int frameHeight, int frameSize)
	{
		BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_4BYTE_ABGR);
		BufferedImage nextFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_4BYTE_ABGR);
		
		CvHistogram[] currentHist = new CvHistogram[3];
		CvHistogram[] nextHist = new CvHistogram[3];
		
		double[] histDiff = new double[3];
		double histDiffTest;
		
		/* File to write data into */
		 File file = new File("./video_rgb_comp.txt");
	 
		 // if file doesn't exists, then create it
		 FileWriter fw;
		 BufferedWriter bw = null;
		 if (!file.exists()) {

			 try {
				 file.createNewFile();
			 } catch (IOException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
		 }
		 try {
			 fw = new FileWriter(file.getAbsoluteFile(), true);
			 bw = new BufferedWriter(fw);
		 } catch (IOException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 
		int second = 0;
		double sqDiff = 0.0;
		double avgSqDiff = 0.0;
		for(long i=0; i<totalFrames-1; i++)
		{
			videoDataReader.readFrame(currentFrame, frameSize, frameWidth, frameHeight);
			videoDataReader.readFrame(nextFrame, frameSize, frameWidth, frameHeight);
			
			currentHist = getRGBHistogram(IplImage.createFrom(currentFrame));
			nextHist = getRGBHistogram(IplImage.createFrom(nextFrame));
			
			sqDiff = 0.0;
			for(int j=0; j<3; j++)
			{
				histDiff[j]=cvCompareHist(currentHist[j],nextHist[j] , CV_COMP_CHISQR );
				sqDiff += (histDiff[j]*histDiff[j]);
			}
			avgSqDiff += Math.sqrt(sqDiff);
			try {
				bw.write("Frame "+i+","+(i+1)+" : "+Math.sqrt(sqDiff));
				bw.newLine();
				
				if(i%23 == 1)
				{
					bw.write("-----------------------------Second "+(second++)+" : "+(avgSqDiff/24)+"-------------------");
					bw.newLine();
					avgSqDiff = 0.0;
				}
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		try {
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static CvHistogram[] getRGBHistogram(IplImage currentFrame) 
	{
		// Split the 3 channels into 3 images
		IplImageArray hsvChannels = splitChannels(currentFrame);

		int numBins = 255;
		float minVal = 0f;
		float maxVal = 180f;

		int dims = 1;
		int[] sizes = new int[] { numBins };
		int histogramType = CV_HIST_ARRAY;
		float[] minMax = new float[] { minVal, maxVal };
		float[][] ranges = new float[][] { minMax };
		CvHistogram[] hist = new CvHistogram[3];

		IplImage mask = null;
		for(int i=0; i<3; i++)
		{
			hist[i] = cvCreateHist(dims, sizes, histogramType, ranges, 1);
			cvCalcHist(hsvChannels.position(i), hist[i], 1, null);
			cvNormalizeHist(hist[i], 1);
		}
		return hist;
	}

	public static IplImageArray splitChannels(IplImage hsvImage) {
		CvSize size = hsvImage.cvSize();
		int depth = hsvImage.depth();
		IplImage channel0 = cvCreateImage(size, depth, 1);
		IplImage channel1 = cvCreateImage(size, depth, 1);
		IplImage channel2 = cvCreateImage(size, depth, 1);
		cvSplit(hsvImage, channel0, channel1, channel2, null);
		return new IplImageArray(channel0, channel1, channel2);
	}

}
