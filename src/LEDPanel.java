import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarFile;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class LEDPanel extends JPanel{
	static final int noDots = 0;
	static final int dots = 1;
	static ArrayList<ArrayList<ImageIcon>> digits;
	static ArrayList<ImageIcon> digitdots;
	static ImageIcon digitMinus;
	static ImageIcon digitBlank;
	static boolean loadedIcons = false;
	static boolean firstRun = true;
	
	ArrayList<JLabel> theNumbers;
	
	int numDigits;
	int decimalPlace;
	double currentNumber;
	// create image icons for each digit
	static void populateDigits() throws IOException{
		digits = new ArrayList<ArrayList<ImageIcon>>(2);
		digits.add(new ArrayList<ImageIcon>(10));
		digits.add(new ArrayList<ImageIcon>(10));
		for(int i = 0; i < 10; i++){
		
//		try{
//			digits.get(noDots).add(new ImageIcon(Toolkit.getDefaultToolkit().getImage(LEDPanel.class.getResource("/led_images/" + i + "LED.png"))));
//		}
//		catch (Exception e){
//		digits.get(noDots).add(new ImageIcon(new File(".").getCanonicalPath() + 
//				"/led_images/" + i + "LED.png"));
//		}
//		try{
//			digits.get(dots).add(new ImageIcon(Toolkit.getDefaultToolkit().getImage(LEDPanel.class.getResource("/led_images/" + i + "LED_DOT.png"))));			
//		}
//		catch(Exception e){
//			digits.get(dots).add(new ImageIcon(new File(".").getCanonicalPath() + 
//				"/led_images/" + i + "LED_DOT.png"));
//		}
//
			digits.get(noDots).add(new ImageIcon(new File(".").getCanonicalPath() + 
					"/led_images/" + i + "LED.png"));
			digits.get(dots).add(new ImageIcon(new File(".").getCanonicalPath() + 
					"/led_images/" + i + "LED_DOT.png"));
//		}
//		try{
//			digitMinus = new ImageIcon(Toolkit.getDefaultToolkit().getImage(LEDPanel.class.getResource("/led_images/LEDminus.png")));			
//		}
//		catch(Exception e){
//			digitMinus = new ImageIcon(new File(".").getCanonicalPath() + 
//				"/led_images/LEDminus.png");
//		}
//		try{
//			digitBlank = new ImageIcon(Toolkit.getDefaultToolkit().getImage(LEDPanel.class.getResource("/led_images/LEDblank.png")));
//		}
//		catch(Exception e){
//			digitBlank = new ImageIcon(new File(".").getCanonicalPath() + 
//					"/led_images/LEDblank.png");			
//		}
		digitMinus = new ImageIcon(new File(".").getCanonicalPath() + 
		"/led_images/LEDminus.png");
		
		digitBlank = new ImageIcon(new File(".").getCanonicalPath() + 
		"/led_images/LEDblank.png");
		}
	}
	
	public void setNumber(double number){
		if(number != currentNumber || firstRun){
			currentNumber = number;
//			if (number == 0.0){
//				for(JLabel label: theNumbers){
//					setDigit(label, digits.get(0).get(0));
//				}
//			}
//			else
			{
			// total number of places to start with
			int places = numDigits;
			int startingPlace = 0;
			int dp = startingPlace;
			int sigfigs;
			int magnitude;
			int value;
			int index;

			if(number < 0){ // number is negative
				places--; // first place (on left) will be reserved for the minus sign.
				startingPlace++;
				setDigit(theNumbers.get(0), digitMinus);
				number = - number; // make the number positive
			}

			// decide where decimal place goes
			magnitude = (int)(Math.log10(number)); // order of magnitude

			if(Math.abs(magnitude) >= places){
				for(JLabel label:theNumbers){
					label.setIcon(digitMinus);
				}
			}else{
				if(magnitude < 1){
					magnitude = 0;
				}
					dp = startingPlace + magnitude;
			
			// get the significant digits as a number
			number = number / (float)Math.pow(10, magnitude);
			
			sigfigs = (int)(number * (float)(Math.pow(10,places - 1)));

			for(int i = 0; i < places; i++){
				if(i + startingPlace == dp)
					index = 1;
				else
					index = 0;
				value = (sigfigs % (int)Math.pow(10, places-i)/ (int)Math.pow(10, places - i -1));
				setDigit(theNumbers.get(i+startingPlace), digits.get(index).get(value));
			}

		}}
		}
	}
	
	private void setDigit(JLabel label, ImageIcon icon){
		if (!label.getIcon().equals(icon)){
			label.setIcon(icon);
		}
	}
	
	public LEDPanel(int numDigs) throws IOException{
		// no need to load the icons if another instance took care of it already
		if (!loadedIcons){
			LEDPanel.populateDigits();
			loadedIcons = true;
		}
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		numDigits = numDigs;
		theNumbers = new ArrayList<JLabel>(numDigs);
		for(int i = 0; i < numDigs; i++){
			theNumbers.add(new JLabel(digitBlank));
			this.add(theNumbers.get(i));
		}
		
	}
}
