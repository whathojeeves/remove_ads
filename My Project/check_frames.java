package project576;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_COMP_CHISQR;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCompareHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.util.ArrayList;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSplit;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;


import com.googlecode.javacv.cpp.opencv_core.IplImage;


public class check_frames {

static int total_frame_size;
	
	public static int width = 352;
	public static int height = 288;
	public static double feature_detection_threshold= 0.3;
	public static int merge_threshold=250;
	
	
	//static ArrayList<int[][]> image_rgb_values = new ArrayList<int[][]>() ;
	
	public static ArrayList<IplImage>complete_video =new ArrayList<IplImage>();
	
	public static void main88(String[] args) {
		// TODO Auto-generated method stub
		
		String fileName = "C:\\CS576\\Project576\\videos\\testVideo3_ironman.rgb";
			
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		    
		    
		    
		    IplImage iplimg = IplImage.create(width, height, IPL_DEPTH_8U, 3);
		    IplImage iplimg_temp = IplImage.create(width, height, IPL_DEPTH_8U, 3);
		    
		    
		    FileWriter fstream = null;
			   try {
					fstream = new FileWriter("complete_hist.txt");
			   } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	BufferedWriter out3 = new BufferedWriter(fstream);
		    
		    
		    int counter_access_frames=0;
			    
		    int offset = 0;
		    try {
			    File file = new File(fileName) ;
			    InputStream is = new FileInputStream(file) ;
			    System.out.println("Starting.....");
			    long len = file.length();
			    byte[] bytes = new byte[width * height * 3];
			    
			    int numRead = 0;
				
				
		        while (offset < file.length() ) {
		        	
		        	
		        	int ind = 0;
		        	numRead=is.read(bytes, 0,width*height*3);
		        	if(numRead < 0)
		        		break;
		        	
		    		for(int y = 0; y < height; y++){
		    	
		    			for(int x = 0; x < width; x++){
		    		 
		    				byte a = 0;
		    				byte r = bytes[ind+height*width*2];
		    				byte g = bytes[ind+height*width];
		    				byte b = bytes[ind]; 
		    				
		    				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
		    				img.setRGB(x,y,pix);
		    			
		    				ind++;
		    			}
		    		}
		    		
		    	
		    			
		    		iplimg=IplImage.createFrom(img);
		    		complete_video.add(iplimg);
		    		offset += numRead;
					counter_access_frames++;
					
		        }
		        is.close();
		    		
		    	
				
				
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    System.out.println("completed iputting...");
		    
		    
		    BufferedImage result = new BufferedImage(352,288, BufferedImage.TYPE_INT_RGB);
			JFrame frame = new JFrame();
		    JLabel label = new JLabel(new ImageIcon(result));
		    frame.getContentPane().add(label, BorderLayout.CENTER);
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.pack();
		    frame.setVisible(true);
		
    		BufferedImage result2 = new BufferedImage(352,288, BufferedImage.TYPE_INT_RGB);
			JFrame frame2 = new JFrame();
		    JLabel label2 = new JLabel(new ImageIcon(result2));
		    frame2.getContentPane().add(label2, BorderLayout.CENTER);
		    frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame2.pack();
		    frame2.setVisible(true);
		
		    
		    int counter_no=100;
		    int counter_no1=100;
		    Scanner scan = new Scanner(System.in);
		    
		     while(true)
			 {
		    	 
		    	 System.out.println("ENter the First frame no:");	    
				 counter_no1 = scan.nextInt(); 
		    	 
		     	 result.setData( (complete_video.get(counter_no1)).getBufferedImage().getRaster());
				 frame.repaint();
				 
				 System.out.println("ENter the Second frame no:");	    
				 counter_no = scan.nextInt();
				 
				 
				 result2.setData( (complete_video.get(counter_no)).getBufferedImage().getRaster());				 
				 frame2.repaint();
				 
				 Match histMatch1 = new Match();
							
				 double hist_compared_value=histMatch1.HisDiff_1(complete_video.get(counter_no1), complete_video.get(counter_no));
				
				
				CvMat cMat = Match2.featureDetect(complete_video.get(counter_no1));
				CvMat pMat = Match2.featureDetect(complete_video.get(counter_no));
				double res = Match2.match(cMat, pMat);
				System.out.println("Hist value is "+hist_compared_value+"\tFD value: "+res);
				
			}
		    
		    
		    
		    
		    
} 
	
	
}
