package project576;
 
public class Node{
	private long start;
	private long end;
	private long duration;
	public void setStart(long s){
		start = s;
	}
	public void setEnd(long e){
		end = e;
	}
	public void setDuration(long d){
		duration = d;
	}
	public long getStart(){
		return start;
	}
	public long getEnd(){
		return end;
	}
	public long getDuration(){
		return duration;
	}
	
}