
import java.util.concurrent.ArrayBlockingQueue;

public class OutputReader implements Runnable {
	private ArrayBlockingQueue<String> outBuffer;
	SerialCommunicator serialComm;
	private boolean quit;
	
	public OutputReader(ArrayBlockingQueue<String> buffer, SerialCommunicator comm)	{
		outBuffer = buffer;
		serialComm = comm;
		quit = false;
	}
	
	public void quit(){
		quit = true;
	}
	
	@Override
	public synchronized void run() {
		while (!quit){
			try{
				if(outBuffer.peek().startsWith("reset")){
					outBuffer.take();
					serialComm.reset();
				}else
				// send the next item in the output buffer to the serial port
				serialComm.serialWrite(outBuffer.take());
			}
			catch (Exception e){
				
			}
		}
	}
}
