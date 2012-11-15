
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.*;
import java.util.Scanner;

public class InputReader implements Runnable {
	private ArrayBlockingQueue<String> sharedBuffer;
	private DashboardGUI gui;
	private boolean quit;
	private StringBuilder ourBuffer = new StringBuilder();
	private Pattern closeTagPattern = Pattern.compile("</[a-z]>");
	private String openTag;
	private String closeTag;

	public InputReader(ArrayBlockingQueue<String> buffer)	{
		sharedBuffer = buffer;
		quit = false;
	}
	
	public InputReader(ArrayBlockingQueue<String> buffer, DashboardGUI gui)	{
		sharedBuffer = buffer;
		this.gui = gui;
		quit = false;
	}
	
	public void setGUI(DashboardGUI gui){
		this.gui = gui;
	}
	
	
	public void quit(){
		quit = true;
	}
	
	public synchronized void filter_input(){
		try{
			// grab data from shared buffer and move it the local one
			ourBuffer.append(sharedBuffer.take());
			//System.out.print(ourBuffer);
			// create a matcher to find an open tag in the buffer			
			int endCloseTag;
			int startOpenTag;
			Matcher closeTagMatcher = closeTagPattern.matcher(ourBuffer);
			
			// look or a closing tag in our buffer
			if(closeTagMatcher.find()){
				endCloseTag = closeTagMatcher.end();
				openTag = "<" + ourBuffer.charAt(closeTagMatcher.start() + 2) + ">";
				// find corresponding opening tag
				startOpenTag = ourBuffer.indexOf(openTag);
				// send the input string to the GUI for processing
				if(startOpenTag >= 0 && startOpenTag < endCloseTag){
					gui.incoming(ourBuffer.substring(startOpenTag,endCloseTag));
					// clear our buffer
					ourBuffer.delete(startOpenTag, endCloseTag);
				}
			}
			}
		catch (Exception e){
			System.out.print(e + "\n");
			
		}
	}
	
	@Override
	public synchronized void run(){
		while(!quit){
//			filter_input();
				try{
					Thread.sleep(1); // poll at 1kHz
					// grab data from shared buffer and move it the local one
					ourBuffer.append(sharedBuffer.take());
					//System.out.print(ourBuffer);
					// create a matcher to find an open tag in the buffer			
					int endCloseTag;
					int startOpenTag;
					Matcher closeTagMatcher = closeTagPattern.matcher(ourBuffer);
					
					// look or a closing tag in our buffer
					if(closeTagMatcher.find()){
						endCloseTag = closeTagMatcher.end();
						openTag = "<" + ourBuffer.charAt(closeTagMatcher.start() + 2) + ">";
						// find corresponding opening tag
						startOpenTag = ourBuffer.indexOf(openTag);
						// send the input string to the GUI for processing
						if(startOpenTag >= 0 && startOpenTag < endCloseTag){
							gui.incoming(ourBuffer.substring(startOpenTag,endCloseTag));
							// clear our buffer
							ourBuffer.delete(startOpenTag, endCloseTag);
						}
						else{
							ourBuffer.delete(0, endCloseTag);						}
					}
					}
				catch (Exception e){
					System.out.print(e + "\n");
					
				}
		}
	}

}
