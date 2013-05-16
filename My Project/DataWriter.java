package project576;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DataWriter {

	LinkedList<Node> adList = new LinkedList<Node>();
	LinkedList<Long> adStartFrameList = new LinkedList<Long>();
	LinkedList<Long> adEndFrameList = new LinkedList<Long>();
	
	LinkedList<Long> adSoundStartFrameList = new LinkedList<Long>();
	LinkedList<Long> adEndSoundFrameList = new LinkedList<Long>();
	File outputFile; 
	File outputSoundFile;
	File outputSoundFile1;
	FileOutputStream fileOut;
	FileOutputStream soundFileOut;
	FileOutputStream soundFileOut1;
	BufferedOutputStream bufWriter;
	BufferedOutputStream soundBufWriter;
	BufferedOutputStream soundBufWriter1;
	BufferedImage currentFrame;

	public DataWriter(LinkedList<Node> list_sub)
	{
		outputFile = new File(VideoPlayer.outputVideoPath);
		outputSoundFile = new File("C:\\CS576_FINAL\\Project576\\videos\\noads\\sound_temp_raw");
		outputSoundFile1 = new File("C:\\CS576_FINAL\\Project576\\videos\\noads\\sound_temp_raw_1");
		if (!outputFile.exists()) {

			try {
				outputFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!outputSoundFile.exists()) {

			try {
				outputSoundFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fileOut = new FileOutputStream(outputFile, true);
			bufWriter = new BufferedOutputStream(fileOut);
			
			soundFileOut = new FileOutputStream(outputSoundFile, true);
			soundBufWriter = new BufferedOutputStream(soundFileOut);
			
			soundFileOut1 = new FileOutputStream(outputSoundFile1, true);
			soundBufWriter1 = new BufferedOutputStream(soundFileOut1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adList = list_sub;
		//test_addDataToLinkedList(0, 30);
		//test_addDataToLinkedList(120, 165);
		//test_addDataToLinkedList(296, 327);
		
	}

	public void writeVideoWithoutAds(File inputVideo, long totalFrames, int frameRate, int frameWidth, int frameHeight, int frameSize)
	{
		DataReader inputVideoData = new DataReader(inputVideo, frameSize);
		byte[] readVideo = new byte[frameSize];
				
		createAdFrameLists();
		long i=0;
		int idx;
		long end = 0;

		for(i=1; i<totalFrames; i++)
		{
			if(!adStartFrameList.isEmpty() && (idx=adStartFrameList.indexOf(i)) != -1)
			{
				System.out.println("Index found : "+idx);
				end = adEndFrameList.get(idx);
				inputVideoData.skipToFrameNumberBytes(end, frameSize, frameWidth, frameHeight, readVideo);
				i = end;
				if(i >= totalFrames)
					break;
				System.out.println("removing "+adStartFrameList.get(idx)+" and "+adEndFrameList.get(idx));
				System.out.println("Skipping to frame "+i);
				adStartFrameList.remove(idx);
				adEndFrameList.remove(idx);
			}
			else
			{
				inputVideoData.readFrameBytes(frameSize, frameWidth, frameHeight, readVideo);
			}
			try {
				
			bufWriter.write(readVideo);
			//System.out.println("Writing frame "+i);
 			} catch (IOException e) {
 				e.printStackTrace();
 				}
		}
		try {
		bufWriter.close(); 
		fileOut.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("Writing done");
	}
	
	public void copyRawSoundToTempFile(int soundFrameSize) throws PlayWaveException
	{
		System.out.println("writing raw data");
		FileInputStream waveStream = null;
		
		try {
			waveStream = new FileInputStream(VideoPlayer.soundPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		AudioInputStream audioInputStream = null;
		
		long curFrame = 0;
		
		try {
		    audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(waveStream));
		} catch (UnsupportedAudioFileException e1) {
		    throw new PlayWaveException(e1);
		} catch (IOException e1) {
		    throw new PlayWaveException(e1);
		}
		
		int readBytes = 0;
		byte[] audioBuffer = new byte[soundFrameSize];

		try {
		    while (readBytes != -1) {
		       	readBytes = audioInputStream.read(audioBuffer, 0,audioBuffer.length);
		    	//System.out.println("read "+readBytes+" bytes");
		    	
			if (readBytes >= 0){
				soundBufWriter.write(audioBuffer);
				//System.out.println("Writing second "+curFrame);
			}
			curFrame++;
		    }
		} catch (IOException e1) {
		    throw new PlayWaveException(e1);
		}
		
	}
	
	public void writeSoundWithoutAds(int soundFrameSize) throws PlayWaveException
	{
		//AudioInputStream audioInputStream = null;
		
		FileInputStream waveStream = null;
		
		try {
			waveStream = new FileInputStream("C:\\CS576_FINAL\\Project576\\videos\\noads\\sound_temp_raw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		createSoundAdFrameLists(48000);
		long curFrame = 0;
		int idx = 0;
		long end=0;
		
		int readBytes = 0;
		byte[] audioBuffer = new byte[soundFrameSize];

		try {
		    while (readBytes != -1) {
		    	
		    	if(!adSoundStartFrameList.isEmpty() && (idx=adSoundStartFrameList.indexOf(curFrame)) != -1)
				{
					System.out.println("Index found : "+idx);
					end = adEndSoundFrameList.get(idx);
					
					System.out.println("Sound Skipping "+waveStream.skip((end-curFrame)*(soundFrameSize))+" bytes");
					curFrame = end;
					
					System.out.println("sound removing "+adSoundStartFrameList.get(idx)+" and "+adEndSoundFrameList.get(idx));
					System.out.println("sound Skipping to frame "+curFrame);
					adSoundStartFrameList.remove(idx);
					adEndSoundFrameList.remove(idx);
					readBytes = waveStream.read(audioBuffer, 0,audioBuffer.length);
					continue;
				}
				
		    	readBytes = waveStream.read(audioBuffer, 0,audioBuffer.length);
		    	System.out.println("read "+readBytes+" bytes");
		    	
			if (readBytes >= 0){
				soundBufWriter1.write(audioBuffer);
			}
			curFrame++;
		    }
		} catch (IOException e1) {
		    throw new PlayWaveException(e1);
		}
	}
	
	public void writeWavSound(int sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian)
	{
		System.out.println("Writing wav file");
		AudioFormat writeFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		
		WaveFileWriter soundWriter = new WaveFileWriter();
		
		File rawSound = new File("C:\\CS576_FINAL\\Project576\\videos\\noads\\sound_temp_raw_1");
		
		FileInputStream rawSoundStream = null;
		try {
			rawSoundStream = new FileInputStream("C:\\CS576_FINAL\\Project576\\videos\\noads\\sound_temp_raw_1");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		AudioInputStream writeAudio = new AudioInputStream(rawSoundStream, writeFormat, (rawSound.length()/2));
		
		File outputFinalSoundFile = new File(VideoPlayer.outputAudioPath);
		if (!outputSoundFile.exists()) {

			try {
				outputFinalSoundFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			soundWriter.write(writeAudio, new AudioFileFormat.Type("WAVE", "wav") , outputFinalSoundFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*public void test_addDataToLinkedList(int start, int end)
	{
		adList.add(new AdFrame(start,end));
	}*/

	public void createAdFrameLists()
	{
		ListIterator<Node> adIter = adList.listIterator();
		Node cur;
		while(adIter.hasNext())
		{
			cur = adIter.next();
			adStartFrameList.add(cur.getStart());
			adEndFrameList.add(cur.getEnd());
			
			System.out.println("Added "+(cur.getStart()));
			System.out.println("Added "+(cur.getEnd()));
		}
		
	}

	public void createSoundAdFrameLists(int frameRate)
	{
		ListIterator<Node> adIter = adList.listIterator();
		Node cur;
		while(adIter.hasNext())
		{
			cur = adIter.next();
			adSoundStartFrameList.add((long)(cur.getStart()/24));
			adEndSoundFrameList.add((long)(cur.getEnd()/24));
			
			System.out.println("Added "+cur.getStart()/24);
			System.out.println("Added "+cur.getEnd()/24);
		}
		
	}
}
