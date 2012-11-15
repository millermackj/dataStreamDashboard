import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DashboardMain {

	/**
	 * @param args
	 */
	
	private LinkedList <String> data;
	private ArrayBlockingQueue <String> inputBuffer, outputBuffer;
	private static final int bufferSize = 10; // size of buffers
	private DashboardGUI theGUI;
	private SerialCommunicator serialComm;
	private InputReader inputReader;

	ExecutorService executor;

	
	public static void main(String[] args) throws IOException {
		DashboardMain application = new DashboardMain(); 
		application.run();

	}
	
	public void run() throws IOException{
		// set up the input/output buffers
		inputBuffer = new ArrayBlockingQueue<String>(bufferSize);
		outputBuffer = new ArrayBlockingQueue<String>(bufferSize);
		
		executor = Executors.newCachedThreadPool();
		
		
		/**	set up serial communicator -- it will write to the input buffer
			as serial data comes in.
		**/
		
		// create an input reader for parsing serial inputs
		
		
		serialComm = new SerialCommunicator(inputBuffer, 115200);
		theGUI = new DashboardGUI(1025, 1000, outputBuffer, serialComm);
		inputReader  = new InputReader(inputBuffer, theGUI);
		executor.execute(inputReader);
		
		//executor.execute(new OutputReader(outputBuffer, serialComm));
		
		if (!serialComm.gotPort()){
      try {
        Thread.sleep(2000);
      } catch (InterruptedException ex) {
        Logger.getLogger(DashboardMain.class.getName()).log(Level.SEVERE, null, ex);
      }
      executor.execute(new testSerialInput(inputBuffer));
    }
		

	}
	

}
