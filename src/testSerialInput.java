
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.Random;
import java.util.Date;

public class testSerialInput implements Runnable {
	private ArrayBlockingQueue<String> inputBuffer;
	private boolean quit;
	private Random rndGen = new Random();
	int time = 0;
	int counter = 0;
	private Thread inputReader;
	
	public testSerialInput(ArrayBlockingQueue<String> buffer)	{
		inputBuffer = buffer;
		quit = false;
	}
	public testSerialInput(ArrayBlockingQueue<String> buffer, Thread inputReader)	{
		inputBuffer = buffer;
		quit = false;
		this.inputReader = inputReader;
	}
	
	
	public void quit(){
		quit = true;
	}
	
	@Override
	public synchronized void run() {
		try{
			// print header
			inputBuffer.put("<h>sysTime\tBattV\tCurrent\tDutyCycle\tCadence\tGenSpeed</h>");
			inputBuffer.put("<u>s\tmillivolts\tmilliamps\tpercent\thz\thz</u>");
		}
		catch(Exception ex){
			
		}
		while (!quit){
			counter++;
			float battV = rndGen.nextFloat() * 3 + 12;
			float current = rndGen.nextFloat() * 3 + -1001;
			float dutyCycle = rndGen.nextFloat() * 50 + 50;
			float cadence = rndGen.nextFloat() * (float)(.2) -(float)(.1);
			float genSpeed = rndGen.nextFloat() * 250 + 10000; 
			try{
				Thread.sleep(100);
				inputBuffer.put(String.format("<r>%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t" +
						"%.3f</r>", (float)time/1000.0 , battV,
						current, dutyCycle, cadence, genSpeed));
				if(counter % 10 == 0)
					inputBuffer.put("<m>message at row " + counter + "</m>");
			}
			catch (Exception e){
				System.out.print("\nin testSerialInput\n");
			}
			time += 100;
		}
	}

}
