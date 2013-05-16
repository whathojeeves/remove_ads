package project576;

import java.io.IOException;

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

public class Match2 {
	public static CvMat featureDetect(IplImage image) {

		SIFT sift = new SIFT(); 
		FeatureDetector featureDetector  = sift.getFeatureDetector();
		KeyPoint keypoints = new KeyPoint(); 
		featureDetector.detect(image, keypoints, null); 
		 
		DescriptorExtractor extractor  = sift.getDescriptorExtractor(); 		
        CvMat descriptors = cvCreateMat(keypoints.capacity(), extractor.descriptorSize(), CV_32F);         
        extractor.compute(image, keypoints, descriptors); 
        
        if(keypoints.isNull() || descriptors.isNull()) {
        	//System.out.println("feature is Null");
        	return null;
        }      
        
        //System.out.println("Keypoints found: "+ keypoints.capacity()); 
       // System.out.println("Descriptors calculated: "+descriptors.rows()); 
        return descriptors;
		
	}
	public static double match(CvMat d1,CvMat d2){
		double i1 = Ratio_match(d1,d2);
		double i2 = Ratio_match(d2,d1);
		return i1>i2?i1:i2;
	}
	public static double Ratio_match(CvMat d1,CvMat d2){
		DescriptorMatcher matcher = new BFMatcher();
		DMatchVectorVector matches = new DMatchVectorVector();
		if(d2 != null && d1 != null) {
			matcher.knnMatch(d1, d2, matches, 2, null, true);	
			//matches.size()
			int count = 0;
			for(int i=0; i<matches.size()-1; i++) {
				if(matches.size(i) > 1) {
					if(matches.get(i, 0).distance()/matches.get(i, 1).distance() < 0.9)
						count++;
				}
			}
			return (double)count/(double)matches.size();
		}
		
		return 1.0;
	}
	
}
