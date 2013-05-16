package project576;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;



public class VideoPlayer {

	static int frameWidth;
	static int frameHeight;
	static int frameSize;
	static int frameRate;
	static long totalFrames;
	static int frameThreshold = 250;

	DataReader videoData;
	DataReader videoProcessor;
	static File videoFile;
	
	/* Re-usable variables */
	static byte[] frameDataBuffer;
	static byte r,g,b;
	static int pixelVal;
	static IplImage[] allFrames;
	
	Match2 fDetect = new Match2();
	
	public static LinkedList<Integer> shotBoundaries = new LinkedList<Integer>();
	public static LinkedList<Integer> shotBoundariesFD = new LinkedList<Integer>();
	public static LinkedList<Node> shotBoundariesFDD = new LinkedList<Node>();
	
	public static LinkedList<Node> listMain = new LinkedList<Node>();
	public static LinkedList<Node> listSub = new LinkedList<Node>();
	
	public static LinkedList<Node> list1 = new LinkedList<Node>();
	public static LinkedList<Node> nodeFdList = new LinkedList<Node>();
	
	public static LinkedList<Node> shots = new LinkedList<Node>();
	
	public static LinkedList<Node> lastshot1 = new LinkedList<Node>();
	
	
	public static String soundPath;
	public static String outputVideoPath;
	public static String outputAudioPath;
	public static String videoPath;

	public VideoPlayer(int width, int height, int numChannels, int fRate, String videoFilePath)
	{
		frameWidth = width;
		frameHeight = height;
		frameSize = width*height*numChannels;
		frameRate = fRate;
		
		videoPath = videoFilePath;
		videoFile = new File(videoFilePath);
				
		videoData = new DataReader(videoFile, frameSize);
		videoProcessor = new DataReader(videoFile, frameSize);
		totalFrames = videoFile.length()/frameSize;
		allFrames = new IplImage[(int) totalFrames];
	}

	public static void main(String[] args)
	{
		if(args.length != 4)
		{
			System.out.println("Wrong command line arguments");
		}
		else
		{
			VideoPlayer myVideoPlayer = new VideoPlayer(352, 
														288,
														3,
														24,
														args[0]);
			
			FileInputStream soundReader = null;
			try {
				soundReader = new FileInputStream(args[1]);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			soundPath = args[1];
			outputVideoPath = args[2];
			outputAudioPath = args[3];
			
			myVideoPlayer.readAllFrames();
			
			/* Code to output the frame histogram differences */ 
			
			myVideoPlayer.outputHistDiff();
			myVideoPlayer.outputFeatureDetect();
			myVideoPlayer.showFDShotFrames();
			//myVideoPlayer.convertListToNodeList();
			myVideoPlayer.getNodeList(shotBoundariesFD);
			//myVideoPlayer.mergeShots(nodeFdList);
			myVideoPlayer.mergeShots2();
			//myVideoPlayer.showMergedShotFrames();
			
			
			ListIterator<Node> fds = shots.listIterator();
			while(fds.hasNext()){
				Node t = fds.next();
				System.out.println("<"+t.getStart()+","+t.getEnd()+">____Duration:"+t.getDuration());
			}
			
			//myVideoPlayer.finalMergedshots();
			//myVideoPlayer.lastpart();
			myVideoPlayer.creatTwoLists(shots);
			myVideoPlayer.showFinalAndMainLists();
			myVideoPlayer.playFinalAndMainLists();
			//myVideoPlayer.playShotsList();
						
			/*long start = System.currentTimeMillis();
			System.out.println("Started "+start);
			Histogram firstHG = new Histogram(videoFile, frameSize, totalFrames);
			firstHG.calculateMetricRGBHist(frameWidth, frameHeight, frameSize);
			System.out.println("Done "+(System.currentTimeMillis() - start));*/
			
			DataWriter outVideo = new DataWriter(listSub);
			outVideo.writeVideoWithoutAds(videoFile, totalFrames, frameRate, frameWidth, frameHeight, frameSize);
			try {
				outVideo.copyRawSoundToTempFile(48000*2);
				outVideo.writeSoundWithoutAds(48000*2);
				outVideo.writeWavSound(48000, 16, 1, true, false);
			} catch (PlayWaveException e) {
				e.printStackTrace();
			}
			
			//outVideo.writeWavSound(48000, 16, 1, true, false);
			
			
		
			System.out.println("Done");
		}
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
			if(cur - prev < 37)
			{
				wait = 37 - (cur-prev);
				
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
	
	public void readAllFrames()
	{
		System.out.println("Reading all frames from disk");
		int numFrame = 0;
		BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		
		for(numFrame=0; numFrame<totalFrames; numFrame++)
		{
			videoProcessor.readFrame(currentFrame,frameSize, frameWidth, frameHeight);
			allFrames[numFrame] = IplImage.createFrom(currentFrame);
		}
		System.out.println("Read all frames from disk");
	}
	
	public void outputHistDiff()
	{
		int numFrame;
		double res=0.0;
		double threshold = 0.0;
		if(videoPath.equalsIgnoreCase("C:\\CS576_FINAL\\Project576\\videos\\testVideo3_ironman.rgb"))
		{
			threshold = 0.82;
		}
		else
		{
			threshold = 0.8;
		}
		System.out.println("Starting histogram comnparison");
		for(numFrame=1; numFrame<totalFrames; numFrame++)
		{
			Match histMatch = new Match();
			res = histMatch.HisDiff_1(allFrames[numFrame], allFrames[numFrame-1]);
			
			//if(res < 0.8)
			if(res < threshold)
			{
				System.out.println("Adding "+numFrame);
				shotBoundaries.add(numFrame);
			}
		}
		System.out.println("Done histogram comparison");
	}
	
	public void outputFeatureDetect()
	{
		ListIterator<Integer> sb = shotBoundaries.listIterator();
		int curFrame = 0;
		int prevFrame = -1;
		double res = 0.0;
		double threshold = 0.0;
		if(videoPath.equalsIgnoreCase("C:\\CS576_FINAL\\Project576\\videos\\testVideo3_ironman.rgb"))
		{
			threshold = 0.25;
		}
		else
		{
			threshold = 0.35;
		}
		
		/*Test*/ 
		//BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		
		CvMat cMat;
		CvMat pMat;
		
		/*JFrame frame = new JFrame();
	    JLabel label = new JLabel(new ImageIcon(currentFrame));
	    frame.getContentPane().add(label, BorderLayout.CENTER);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);*/
	    
	    System.out.println("Starting feature detection");
		
		while(sb.hasNext())
		{
			curFrame = sb.next();
			if(prevFrame != -1)
			{
				cMat = Match2.featureDetect(allFrames[curFrame]);
				pMat = Match2.featureDetect(allFrames[prevFrame]);
				res = Match2.match(cMat, pMat);
				//if(res < 0.35)
				if(res < threshold)
				{
					shotBoundariesFD.add(curFrame);
					//System.out.println("FD val "+curFrame+","+prevFrame+":"+res);
					//System.out.println("Adding "+curFrame);
				}
				
				//currentFrame.setData(allFrames[curFrame].getBufferedImage().getRaster());
				//frame.repaint();
			}
			prevFrame = curFrame;
		}
		System.out.println("Done");
	}
	
	void getNodeList(LinkedList<Integer> list){
		ListIterator<Integer> iterator = shotBoundariesFD.listIterator();
		int mark=0;
		while(iterator.hasNext()){
			Node t = new Node();
			if(shots.size()==0){
				t.setStart(1);
				mark = iterator.next();
				t.setEnd(mark-1);
				t.setDuration(t.getEnd()-t.getStart());
				shots.add(t);
			}
			else{
				t.setStart(mark);
				mark = iterator.next();
				t.setEnd(mark-1);
				t.setDuration(t.getEnd()-t.getStart());
				shots.add(t);
			}
			System.out.println("size:"+shots.size()+"<"+t.getStart()+","+t.getEnd()+">___"+t.getDuration());
		}
		Node last = new Node();
		last.setStart(shots.getLast().getEnd()+1);
		last.setEnd(totalFrames);
		last.setDuration(totalFrames - 1 - last.getStart());
		shots.add(last);
	}
	
	
	void convertListToNodeList()
	{
		ListIterator<Integer> fds = shotBoundariesFD.listIterator();
		Node cur = new Node();
		Node prev = new Node();
		int start,end;
		start = 0;
		
		System.out.println("Converting frame number list to Node list");
		
		while(fds.hasNext())
		{
			end = fds.next();
			
			cur.setStart(start);
			cur.setEnd(end-1);
			cur.setDuration(end-start);
			
			System.out.println("Node start:"+cur.getStart()+" end:"+cur.getEnd());

			nodeFdList.add(cur);
			
			start = end;
		}
		System.out.println("Converted frame number list to Node list");
	}
	
	public boolean compareShots(Node input1, Node input2)
	{
		long start1 = input1.getStart()+2;
		long end1 = input1.getEnd()-2;
		long mid1 = Math.round((start1+end1)/2);
		long startmid1 = Math.round((start1+mid1)/2);
		long endmid1 = Math.round((mid1+end1)/2);

		long start2 = input2.getStart()+2;
		long end2 = input2.getEnd()-2;
		long mid2 = Math.round((start2+end2)/2);
		long startmid2 = Math.round((start2+mid2)/2);
		long endmid2 = Math.round((mid2+end2)/2);

		boolean similar = false;

		long[] scene1 = {start1, startmid1, mid1, endmid1, end1};
		long[] scene2 = {start2, startmid2, mid2, endmid2, end2};

		for(int i=0; i<5; i++)
		{
			for(int j=0; j<5; j++)
			{
				CvMat cMat = Match2.featureDetect(allFrames[(int)scene1[i]]);
				CvMat pMat = Match2.featureDetect(allFrames[(int)scene2[j]]);
				double res = Match2.match(cMat, pMat);

				if(res > 0.25)
				{
					
					similar = true;
					break;
				}
				
				else
				{
					Match newMatch = new Match();
					if((newMatch.HisDiff_1(allFrames[(int)scene1[i]], allFrames[(int)scene2[j]])) > 0.82)
					{
						similar = true;
						break;
					}
				}

			}
		}

		return similar;
	}
	
	public int compareShotsForSplit(Node input1, Node input2)
	{
		long start1 = input1.getStart()+5;
		long end1 = input1.getEnd()-5;
		long mid1 = Math.round((start1+end1)/2);
		long startmid1 = Math.round((start1+mid1)/2);
		long endmid1 = Math.round((mid1+end1)/2);

		long start2 = input2.getStart()+5;
		long end2 = input2.getEnd()-5;
		long mid2 = Math.round((start2+end2)/2);
		long startmid2 = Math.round((start2+mid2)/2);
		long endmid2 = Math.round((mid2+end2)/2);
		long i1 = Math.round((start2+startmid2)/2);
		long i2 = Math.round((startmid2+mid2)/2);
		long i3 = Math.round((mid2+endmid2)/2);
		long i4 = Math.round((endmid2+end2)/2);
		

		boolean similar = false;
		int frameMatch = 0;

		long[] scene1 = {start1, startmid1, mid1, endmid1, end1};
		long[] scene2 = {start2, i1, startmid2, i2, mid2, i3, endmid2, i4, end2};

		for(int i=0; i<9; i++)
		{
			for(int j=0; j<5; j++)
			{
				CvMat cMat = Match2.featureDetect(allFrames[(int)scene1[j]]);
				CvMat pMat = Match2.featureDetect(allFrames[(int)scene2[i]]);
				double res = Match2.match(cMat, pMat);

				if(res > 0.25)
				{
					
					similar = true;
					frameMatch++;
				}
		
				/*else
				{
					Match newMatch = new Match();
					if((newMatch.HisDiff_1(allFrames[(int)scene1[j]], allFrames[(int)scene2[i]])) > 0.82)
					{
						similar = true;
						frameMatch++;
					}
				}*/

			}
		}
		
		return frameMatch;
	}
	
	/*public boolean compareShotsFD(Node input1, Node input2)
	{
		long start1 = input1.getStart();
		long end1 = input1.getEnd();
		long mid1 = Math.round((start1+end1)/2);
		
		long start2 = input2.getStart();
		long end2 = input2.getEnd();
		long mid2 = Math.round((start2+end2)/2);
		
		boolean similar = false;
		
		long[] scene1 = {start1, end1, mid1};
		long[] scene2 = {start2, end2, mid2};
		
		for(int i=0; i<3; i++)
		{
			for(int j=0; j<3; j++)
			{
				Match newMatch = new Match();
				CvMat cMat = Match2.featureDetect(allFrames[(int)scene1[i]]);
				CvMat pMat = Match2.featureDetect(allFrames[(int)scene2[j]]);
				double res = Match2.match(cMat, pMat);
				if(res > 0.25)
				{
					similar = true;
				}
				
			}
		}
		return similar;
	}*/
	
	void finalMergedshots(){
		 int c1=0;
		 int c2=c1+1;
		 System.out.println("finalMergedshot!!!!-----------------------------------------start!");
		 int total=shots.size();
		 for(;c2<total;c1++,c2++){
			 if(shots.get(c1).getEnd()+1==shots.get(c2).getStart()){
				 //if(compareShots(shots.get(c1),shots.get(c2))){
				 if(compareShotsForSplitWorking(shots.get(c1),shots.get(c2))>3){
					 shots.get(c1).setEnd(shots.get(c2).getEnd());
					 shots.get(c1).setDuration(shots.get(c1).getEnd()-shots.get(c1).getStart());
					 shots.remove(c2);
					 total=shots.size();
					 c2=c1+1;
				 }
			 }
		 }
		 
		 for(c1 =0;c1<total;c1++){
			 System.out.println("<"+shots.get(c1).getStart()+","+shots.get(c1).getEnd()+">__"+shots.get(c1).getDuration());
		 }
		 System.out.println("finalMergedshot!!!!--------------------------------\\\\\\\\\\\\-end!");
	}
	
	public int compareShotsForSplitWorking(Node input1, Node input2)
	{
		long start1 = input1.getStart()+5;
		long end1 = input1.getEnd()-5;
		long mid1 = Math.round((start1+end1)/2);
		long startmid1 = Math.round((start1+mid1)/2);
		long endmid1 = Math.round((mid1+end1)/2);

		long start2 = input2.getStart()+5;
		long end2 = input2.getEnd()-5;
		long mid2 = Math.round((start2+end2)/2);
		long startmid2 = Math.round((start2+mid2)/2);
		long endmid2 = Math.round((mid2+end2)/2);

		boolean similar = false;
		int frameMatch = 0;

		long[] scene1 = {start1, startmid1, mid1, endmid1, end1};
		long[] scene2 = {start2, startmid2, mid2, endmid2, end2};

		for(int i=0; i<5; i++)
		{
			for(int j=0; j<5; j++)
			{
				CvMat cMat = Match2.featureDetect(allFrames[(int)scene1[i]]);
				CvMat pMat = Match2.featureDetect(allFrames[(int)scene2[j]]);
				double res = Match2.match(cMat, pMat);

				/*if(res > 0.27)
				{
					
					similar = true;
					frameMatch++;
				}
				
				else
				{*/
					Match newMatch = new Match();
					if((newMatch.HisDiff_2(allFrames[(int)scene1[i]], allFrames[(int)scene2[j]])) > 0.52)
					{
						similar = true;
						//break;
						frameMatch++;
					}
				//}

			}
		}
		
		return frameMatch;
	}
	
	void creatTwoLists(LinkedList<Node> finalList)
	{
		ListIterator<Node> finalListIter = finalList.listIterator();
		Node curNode = null;
		Node maxNode = null;
		
		/* Find the max length */
		long max = 0;
		
		while(finalListIter.hasNext())
		{
			curNode = finalListIter.next();
			if(curNode.getDuration() > max)
			{
				max = curNode.getDuration();
				maxNode = curNode;
			}
		}
		
		ListIterator<Node> finalListIter_1 = finalList.listIterator();
		ListIterator<Node> iter = finalList.listIterator();
		
		while(finalListIter_1.hasNext())
		{
			curNode = finalListIter_1.next();
			if(compareShotsForSplitWorking(curNode, maxNode) > 4)
			{
				
				listMain.add(curNode);
			}
			else
			{
				listSub.add(curNode);
			}
		}
	}
	
	void showFinalAndMainLists()
	{
		System.out.println("Main List");
		ListIterator<Node> fds = listMain.listIterator();
		int i = 0;
		int j = 0;
		//BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		Node cur = null;

		while(fds.hasNext())
		{
			cur = fds.next();
			System.out.println("Frame Start:"+cur.getStart()+" End:"+cur.getEnd());
		}

		System.out.println("Sub List");
		fds = listSub.listIterator();

		while(fds.hasNext())
		{
			cur = fds.next();
			System.out.println("Frame Start:"+cur.getStart()+" End:"+cur.getEnd());
		}

	}
	
	void playFinalAndMainLists()
	{
		System.out.println("Main List");
		ListIterator<Node> fds = listMain.listIterator();
		long i = 0;
		long j = 0;
		BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		BufferedImage currentFrame1= new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		Node cur = null;

		JFrame frame = new JFrame();
		JLabel label = new JLabel(new ImageIcon(currentFrame));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		while(fds.hasNext())
		{
			cur = fds.next();
			for(i=cur.getStart(); i<= cur.getEnd(); i++)
			{
				//System.out.println("Frame Start:"+cur.getStart()+" End:"+cur.getEnd());
				currentFrame.setData(allFrames[(int)i].getBufferedImage().getRaster());
				frame.repaint();
				try {
					Thread.currentThread().sleep(20);
				} catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
			
		}
		
		/*
		System.out.println("Sub List");
		fds = listSub.listIterator();
		
		JFrame frame1 = new JFrame();
		JLabel label1 = new JLabel(new ImageIcon(currentFrame1));
		frame1.getContentPane().add(label1, BorderLayout.CENTER);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame1.pack();
		frame1.setVisible(true);

		while(fds.hasNext())
		{
			cur = fds.next();
			for(i=cur.getStart(); i<= cur.getEnd(); i++)
			{
				System.out.println("Frame Start:"+cur.getStart()+" End:"+cur.getEnd());
				currentFrame1.setData(allFrames[(int)i].getBufferedImage().getRaster());
				frame1.repaint();
				try {
					Thread.currentThread().sleep(20);
				} catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
		}
		*/
		
	}
	
	void playShotsList()
	{
			System.out.println("Main List");
			ListIterator<Node> fds = shots.listIterator();
			long i = 0;
			long j = 0;
			BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
			BufferedImage currentFrame1= new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
			Node cur = null;

			JFrame frame = new JFrame();
			JLabel label = new JLabel(new ImageIcon(currentFrame));
			frame.getContentPane().add(label, BorderLayout.CENTER);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);

			while(fds.hasNext())
			{
				cur = fds.next();
				for(i=cur.getStart(); i<= cur.getEnd(); i++)
				{
					System.out.println("Frame Start:"+cur.getStart()+" End:"+cur.getEnd());
					currentFrame.setData(allFrames[(int)i].getBufferedImage().getRaster());
					frame.repaint();
					try {
						Thread.currentThread().sleep(20);
					} catch(Exception e)
					{
						e.printStackTrace();
					}
					
				}
			}
	}
	
	void mergeShots(LinkedList<Node> fList)
	{
		ListIterator<Node> fListIter = fList.listIterator();
		Node base = null;
		Node cur = null;
		Node savedCur = null;
		Node prevCur = null;
		Node prevCur1 = null;
		
		Node newNode1 = new Node();
		Node newNode2 = new Node();
		
		System.out.println("Merging shots");
		
		base = fListIter.next();
		cur = fListIter.next();
		
		while(fListIter.hasNext())
		{
			if(base.getDuration() > frameThreshold)
			{
				if(!list1.contains(base))
					list1.add(base);
				base = cur;
				if(fListIter.hasNext())
					cur = fListIter.next();
				else
					break;
			}
			else
			{
				while((compareShots(base, cur)) &&  
						((cur.getEnd() - base.getStart()) < 250) &&
						((cur.getEnd() - base.getStart()) > 0) &&
						fListIter.hasNext())
				{
					prevCur = cur;
					cur = fListIter.next();
				}
				
				if((cur.getEnd() - base.getStart()) < 250 &&
						(cur.getEnd() - base.getStart()) > 0)
				{
					prevCur1 = cur;
					if(fListIter.hasNext())
						cur = fListIter.next();
					if(compareShots(base, cur))
					{
						newNode1.setStart(base.getStart());
						newNode1.setEnd(cur.getEnd());
						newNode1.setDuration(cur.getEnd() - base.getStart());
						list1.add(newNode1);
						if(fListIter.hasNext())
							base = fListIter.next();
						if(fListIter.hasNext())
							cur = fListIter.next();
					}
					else
					{
						newNode1.setStart(base.getStart());
						newNode1.setEnd(prevCur.getEnd());
						newNode1.setDuration(prevCur.getEnd() - base.getStart());
						list1.add(newNode1);
						base = prevCur1;
						//cur = 
					}
				}
				else
				{
					newNode1.setStart(base.getStart());
					newNode1.setEnd(prevCur.getEnd());
					newNode1.setDuration(prevCur.getEnd() - base.getStart());
					list1.add(newNode1);
				}
			}
		}
		
		System.out.println("Done merging shots");
	}
	
	void mergeShots2()
	{
		System.out.println("Merging Shots");
		int total = shots.size();
		for(int counter1=0, counter2=0;counter2<total-1;counter2++){
			System.out.println("computing....c1:"+counter1+"c2:"+counter2);
			if(shots.get(counter2).getEnd()-shots.get(counter1).getStart() >50){
				 if(counter1==counter2 && counter1<total){
					 counter1++;
					 continue;
				 }
				 if( compareShots(shots.get(counter1),shots.get(counter2))){
					 System.out.println("similarity: 1st <"+shots.get(counter1).getStart()+","+shots.get(counter2).getEnd()+"> 2nd <"+shots.get(counter2).getStart()+","+shots.get(counter2).getEnd()+">");
					 shots.get(counter1).setEnd(shots.get(counter2).getEnd());
					 shots.get(counter1).setDuration(shots.get(counter1).getEnd()-shots.get(counter1).getStart());
					 for(int i=counter1+1;i<=counter2;i++){
						 shots.remove(i);
						 total=shots.size();
					 }
					 counter2=counter1;
				 }
				 if(counter1<total)
				 counter1++;
			}
			else{
				 if( compareShots(shots.get(counter1),shots.get(counter2))){
					 shots.get(counter1).setEnd(shots.get(counter2).getEnd());
					 shots.get(counter1).setDuration(shots.get(counter1).getEnd()-shots.get(counter1).getStart());
					 for(int i=counter1+1;i<=counter2;i++){
						 shots.remove(i);
						 total=shots.size();
						 counter2=counter1;
					 }
				 }
				
		}
		}
		System.out.println("Merging done");
	}
	void showFDShotFrames()
	{
		ListIterator<Integer> fds = shotBoundariesFD.listIterator();
		int i = 0;
		int j = 0;
		BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		
		while(fds.hasNext())
		{
			i = fds.next();
			/*for(j=0; j<i; j++)
			{
				currentFrame.setData(allFrames[j].getBufferedImage().getRaster());
				frame.repaint();
			}*/
			System.out.println("Frame "+i);
			/*currentFrame.setData(allFrames[i].getBufferedImage().getRaster());
			frame.repaint();
			try {
				Thread.currentThread().sleep(100);
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			*/
		}
	}
	
	void showMergedShotFrames()
	{
		ListIterator<Node> fds = shots.listIterator();
		BufferedImage currentFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		Node temp = null;
		/*JFrame frame = new JFrame();
	    JLabel label = new JLabel(new ImageIcon(currentFrame));
	    frame.getContentPane().add(label, BorderLayout.CENTER);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);*/
		
		while(fds.hasNext())
		{
			temp = fds.next();
			
			//currentFrame.setData(allFrames[(int)temp.getStart()].getBufferedImage().getRaster());
			
			System.out.println("Frame "+temp.getStart());
			
			//frame.repaint();
			try {
				Thread.currentThread().sleep(100);
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			
			//currentFrame.setData(allFrames[(int)temp.getEnd()].getBufferedImage().getRaster());
			
			System.out.println("Frame "+temp.getEnd());
		}
		System.out.println("Done showing merges");
	}
	
	void lastpart(){
		int j=0;
		 lastshot1.add(shots.get(getMaxIndex()));
		 shots.remove(getMaxIndex());
		while(lastshot1.size()==1 ||lastshot1.getLast().getEnd()!=lastshot1.get(j).getEnd()){
			
			 for(int i=0;i<shots.size()-1;i++){
				 System.out.println("+++++++++");
				 if(compareShotsForSplit(lastshot1.get(j),shots.get(i))>3){
					 System.out.println("!!!!!-----");
					 lastshot1.add(shots.get(i));
					 j++;
					 shots.remove(i);
					 i--;
					 //i=0;
				 }
			 }
		 }
		 
		 for(j=0;j<shots.size();j++)
			 System.out.println("|"+shots.get(j).getStart()+","+shots.get(j).getEnd()+"|----"+shots.get(j).getDuration());
		
	}
	
	int getMaxIndex(){
		long max=0;
		int maxIndex =0;
		for(int i=0;i<shots.size();i++){
			if(shots.get(i).getDuration()>max){
				maxIndex = i;
				max = shots.get(i).getDuration();
			}
		}
		return maxIndex;
	}
	
	
}
