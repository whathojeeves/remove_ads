package project576;
//Vasu:Check the //!----------------------------------!//
import static com.googlecode.javacv.cpp.opencv_core.CV_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_core.cvSplit;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_COMP_INTERSECT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HIST_ARRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCalcHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCompareHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCreateHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvNormalizeHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvReleaseHist;

import java.io.IOException;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_features2d.BFMatcher;
import com.googlecode.javacv.cpp.opencv_features2d.DMatchVectorVector;
import com.googlecode.javacv.cpp.opencv_features2d.DescriptorExtractor;
import com.googlecode.javacv.cpp.opencv_features2d.DescriptorMatcher;
import com.googlecode.javacv.cpp.opencv_features2d.FeatureDetector;
import com.googlecode.javacv.cpp.opencv_features2d.KeyPoint;
import com.googlecode.javacv.cpp.opencv_imgproc.CvHistogram;
import com.googlecode.javacv.cpp.opencv_nonfree.SIFT;

public class Match {
	
	int size[] = {50,50,50}; 
	float ranges[][] = {{0,50},{0,50},{0,50}};
		//----------------------------------------------------------------------------
	/* Hard coding width and height */
	IplImage channel1 = cvCreateImage(cvSize(352,288),IPL_DEPTH_8U,1);
	IplImage channel2 = cvCreateImage(cvSize(352,288),IPL_DEPTH_8U,1);
	IplImage channel3 = cvCreateImage(cvSize(352,288),IPL_DEPTH_8U,1);
	
	
	IplImage array[] = new IplImage[3];
	int dims=3;
	
	CvHistogram histro = cvCreateHist( dims, size, CV_HIST_ARRAY, ranges, 1 );
	CvHistogram histro_n = cvCreateHist( dims, size, CV_HIST_ARRAY, ranges, 1 );
	
	
	public CvMat FeatureDetect(IplImage image) {

		SIFT sift = new SIFT(); 
		FeatureDetector featureDetector  = sift.getFeatureDetector();
		KeyPoint kpt = new KeyPoint(); 
		featureDetector.detect(image, kpt, null); 
		 
		DescriptorExtractor extractor  = sift.getDescriptorExtractor(); 		
        CvMat dp = cvCreateMat(kpt.capacity(), extractor.descriptorSize(), CV_32F);         
        extractor.compute(image, kpt, dp); 
        
        if(kpt.isNull() || dp.isNull())
        {
        	return null;
        }
        return dp;
	}
	public DMatchVectorVector Init_Match(CvMat d1,CvMat d2){
		DescriptorMatcher matcher = new BFMatcher();
		DMatchVectorVector matches = new DMatchVectorVector();
		matcher.knnMatch(d1, d2, matches, 2, null, true);
		return matches;
	}
	public double RatioTest(DMatchVectorVector matches){
		int count = 0;
		long total =  matches.size();
		
		if(total < 1)
			return 0.0;
		else{
			//ratio_Test
			for(int i=0; i<total-1; i++) {
				if(matches.size(i) > 1) {
					if(matches.get(i, 0).distance()/matches.get(i, 1).distance() < 0.9)
						count++;
			}
		}
		return (double)count/(double)matches.size();
		}
	}
	//!----------------------------------!//
	// Feature detection, just use this method. 
	// The input parameters are two images, the types are IplImage.
	//return value is double
	public double SymmetricTest(IplImage d1, IplImage d2){
		double similarity = 0.0;
		double i1=0;
		double i2=0;
		i1 = RatioTest(Init_Match(FeatureDetect(d1),FeatureDetect(d2)));
		//System.out.println("1st similarity:"+i1);
		i2 = RatioTest(Init_Match(FeatureDetect(d2),FeatureDetect(d1)));
		//System.out.println("2nd similarity:"+i2);
		similarity = i1>i2? i1:i2;
		return similarity;
		
	}
	
	public CvHistogram cal_histro(IplImage image){
		
		//IplImage channel1 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		//IplImage channel2 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		//IplImage channel3 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		
		cvSplit(image, channel1,channel2,channel3,null);
		
		//int size[] = {180,256,256}; 
		//float ranges[][] = {{0,180},{0,255},{0,255}};
		
		CvHistogram histro = cvCreateHist( dims, size, CV_HIST_ARRAY, ranges, 1 );
		
		//IplImage array[] = {channel1,channel2,channel3};
		
		array[0] = channel1;
		array[1] = channel2;
		array[2] = channel3;
		
		cvCalcHist(array, histro, 1, null);
		cvNormalizeHist(histro,1.0);
		
		return histro;
	}
	
	public double comp_histro(IplImage image, IplImage image_n){
		
		cvSplit(image, channel1,channel2,channel3,null);
		
		array[0] = channel1;
		array[1] = channel2;
		array[2] = channel3;
		
		double res = 0.0;
		
		cvCalcHist(array, histro, 1, null);
		cvNormalizeHist(histro,1.0);
		
		cvSplit(image_n, channel1,channel2,channel3,null);
		
		array[0] = channel1;
		array[1] = channel2;
		array[2] = channel3;
		
		cvCalcHist(array, histro_n, 1, null);
		cvNormalizeHist(histro_n,1.0);
		
		res = cvCompareHist(histro, histro_n, CV_COMP_INTERSECT);
		
		cvReleaseHist(histro);
		cvReleaseHist(histro_n);
		
		return res;
	}
	
	
	//!----------------------------------!//
	//Only for histrogram. 
	//The input are two images, the type of those are Iplimage.
	//return value is double.
	public double HisDiff(IplImage image1, IplImage image2){
		//return cvCompareHist(cal_histro(image1),cal_histro(image2), CV_COMP_INTERSECT);
		return comp_histro(image1, image2);
	}
	
	public CvHistogram calc_histro_1(IplImage image){

		int size[] = {50,50,50}; 
		float ranges[][] = {{0,50},{0,50},{0,50}};
		int dims=3;
		IplImage channel1 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		IplImage channel2 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		IplImage channel3 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		cvSplit(image, channel1,channel2,channel3,null);
		CvHistogram histro = cvCreateHist( dims, size, CV_HIST_ARRAY, ranges, 1 );
		IplImage array[] = {channel1,channel2,channel3};
		cvCalcHist(array, histro, 0, null);
		cvNormalizeHist(histro,1.0);
		cvReleaseImage(channel1);
		cvReleaseImage(channel2);
		cvReleaseImage(channel3);

		return histro;
	}

	public double HisDiff_1(IplImage image1, IplImage image2){
		CvHistogram c1 = calc_histro_1(image1);
		CvHistogram c2 = calc_histro_1(image2);
		double s =cvCompareHist(c1,c2, CV_COMP_INTERSECT);
		cvReleaseHist(c1);
		cvReleaseHist(c2);
		return s;
	}

	public CvHistogram calc_histro_2(IplImage image){

		int size[] = {100,100,100}; 
		float ranges[][] = {{0,100},{0,100},{0,100}};
		int dims=3;
		IplImage channel1 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		IplImage channel2 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		IplImage channel3 = cvCreateImage(cvSize(image.width(),image.height()),IPL_DEPTH_8U,1);
		cvSplit(image, channel1,channel2,channel3,null);
		CvHistogram histro = cvCreateHist( dims, size, CV_HIST_ARRAY, ranges, 1 );
		IplImage array[] = {channel1,channel2,channel3};
		cvCalcHist(array, histro, 0, null);
		cvNormalizeHist(histro,1.0);
		cvReleaseImage(channel1);
		cvReleaseImage(channel2);
		cvReleaseImage(channel3);

		return histro;
	}

	public double HisDiff_2(IplImage image1, IplImage image2){
		CvHistogram c1 = calc_histro_2(image1);
		CvHistogram c2 = calc_histro_2(image2);
		double s =cvCompareHist(c1,c2, CV_COMP_INTERSECT);
		cvReleaseHist(c1);
		cvReleaseHist(c2);
		return s;
	}
	
	//The main method is only for testing. 
	//can delete them.
	/*public static void main(String[] args) throws IOException{
		IplImage image1 = cvLoadImage("1.jpg");
		IplImage image2 = cvLoadImage("2.jpg");
		
		Match t = new Match();
		double hisDiff= t.HisDiff(image1,image2);
		System.out.println("feature connection"+t.SymmetricTest(image1,image2));
		System.out.println("The histrodifference:"+hisDiff);
	}	*/
}
