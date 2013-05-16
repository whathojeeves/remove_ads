package project576;

public class AdFrame {
	int start;
	int end;
	int duration;
	
	public AdFrame(int s, int e)
	{
		start = s;
		end = e;
		duration = end-start;
	}
}
