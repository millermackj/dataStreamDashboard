import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.*;
import javax.swing.border.Border;

import graphing.*;
import org.jfree.data.xy.XYSeries;
import java.util.LinkedList;
import java.util.ArrayList;

public class DashboardGUI extends JFrame implements ActionListener,
		WindowListener {
	private static final long serialVersionUID = 1L;
	private String[] headerLabels;
	private String[] unitsLabels;
	private LinkedList<Float[]> dataList;
	private JScrollPane scrollPane;
	private JTextArea textArea;
	private JScrollPane msgScrollPane;
	private JTextArea messageArea = new JTextArea(10,20); // messages get printed here
	private JFrame messageFrame;
	private JTextField entryField;
	private JButton saveButton;
	private JButton startStopCollBtn;
	private JButton setGraphButton;
	private JButton resetConnectionBtn;
	private JTextArea headerDisplay;
	private ArrayBlockingQueue<String> outputBuffer;
	private SerialCommunicator serialComm;
	private JCheckBox chkAutoscroll;
	private JCheckBox msgAutoscroll;
	private JCheckBox chkGraphInstant;
	private JFGraph graph;
	private JComboBox xComboBox;
	private JComboBox yComboBox;
	
	private ArrayList<JComboBox> ledComboBoxes;
	
	private JPanel ledDisplayPanel = new JPanel();
	private ArrayList<LEDPanel> ledPanels;
	private int numLEDDisplays = 4;
	private int numLEDDigits = 4;
	private boolean settingsSaved = false;
	private int[] dropDownSettings;
	private boolean noHeader = true;
	private boolean autoColumnSelect = true;
	private File saveDirectory = new File(""); // directory where last file was saved
	
	// an enumeration of tags
	private enum Tag {
		HEADER("<h>"), UNITS("<u>"), ROW("<r>"), MESSAGE("<m>");

		private String tagString;

		Tag(String tagString) {
			this.tagString = tagString;
		}

		public String toString() {
			return tagString;
		}
	}

	private int graphXIndex = 0;
	private int graphYIndex = 0;

	private boolean stopCollecting = false;

	public DashboardGUI(int width, int height,
			ArrayBlockingQueue<String> outputBuffer,
			SerialCommunicator serialComm) throws IOException {
		this.outputBuffer = outputBuffer;
		dataList = new LinkedList<Float[]>();
		this.setSize(width, height);
		initialize();
		this.serialComm = serialComm;
	}

	/** set up the gui components 
	 * @throws IOException **/
	private void initialize() throws IOException {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// create upper, middle and lower panels
		Border paddedBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

		JPanel upperPanel = new JPanel();
		JPanel displayPanel = new JPanel();
		JPanel lowerPanel = new JPanel();
		JPanel messagePanel = new JPanel();
		JPanel graphPanel = new JPanel();
		JPanel graphSelectPanel = new JPanel();
		JPanel checkboxPanel = new JPanel();

		upperPanel.setBorder(paddedBorder);
		lowerPanel.setBorder(paddedBorder);
		displayPanel.setBorder(paddedBorder);
		graphSelectPanel.setBorder(paddedBorder);
		checkboxPanel.setBorder(paddedBorder);
		
		// give box layouts to the panels
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
		ledDisplayPanel.setLayout(new BoxLayout(ledDisplayPanel, BoxLayout.Y_AXIS));
		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
		graphSelectPanel.setLayout(new BoxLayout(graphSelectPanel, BoxLayout.X_AXIS));
		displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.X_AXIS));
		checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.X_AXIS));
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
		
		messagePanel.setLayout(new BorderLayout());

		// objects for upper panel, top row
		saveButton = new JButton("Save Data as CSV");
		saveButton.addActionListener(this);
		startStopCollBtn = new JButton("Stop collecting");
		startStopCollBtn.addActionListener(this);
		resetConnectionBtn = new JButton("Reset Connection");
		resetConnectionBtn.addActionListener(this);
		// for upper panel, below top row
		graph = new JFGraph("", "", "");
		// graph.useDataSeries(dataSeries);

		upperPanel.add(saveButton);
		upperPanel.add(startStopCollBtn);
		upperPanel.add(resetConnectionBtn);
		
		// objects in graph select panel
		yComboBox = new JComboBox();
		xComboBox = new JComboBox();
		JLabel xComboLabel = new JLabel("X-data:");
		JLabel yComboLabel = new JLabel("Y-data:");
		
		yComboBox.addActionListener(this);
		xComboBox.addActionListener(this);

		setGraphButton = new JButton("Set Graph");
		setGraphButton.addActionListener(this);
		
		graphSelectPanel.add(yComboLabel);
		graphSelectPanel.add(yComboBox);
		graphSelectPanel.add(setGraphButton);
		graphSelectPanel.add(xComboLabel);
		graphSelectPanel.add(xComboBox);

		
		graphPanel.add(graph);
		graphPanel.add(graphSelectPanel);
		
		// set up LED display panel

		initLEDPanel();

		// add graphPanel and LED displays to display panel
		displayPanel.add(graphPanel);
		displayPanel.add(ledDisplayPanel);

		// objects in lower panel
		chkAutoscroll = new JCheckBox("Autoscroll Text");
		chkAutoscroll.setSelected(true);
		
		chkGraphInstant = new JCheckBox("Graph only this instant");
		chkGraphInstant.addActionListener(this);
		
		textArea = new JTextArea(8, 50);
		textArea.setEditable(false);

		scrollPane = new JScrollPane(textArea);

		entryField = new JTextField();
		entryField.addActionListener(this);

		headerDisplay = new JTextArea(1, 50);

		checkboxPanel.add(chkGraphInstant);
		checkboxPanel.add(chkAutoscroll);
		
		lowerPanel.add(checkboxPanel);
		
		lowerPanel.add(headerDisplay);
		lowerPanel.add(scrollPane);

		this.add(upperPanel);
		this.add(displayPanel);
		this.add(graphSelectPanel);
		this.add(lowerPanel);

		this.addWindowListener(this); // so that we can have custom close event
		
		msgScrollPane = new JScrollPane(messageArea);
		msgAutoscroll = new JCheckBox("Autoscroll");
		msgAutoscroll.setSelected(true);
		messagePanel.add(msgAutoscroll, BorderLayout.NORTH);
		messagePanel.add(msgScrollPane, BorderLayout.CENTER);
		// put entry field in message area
		messagePanel.add(entryField, BorderLayout.PAGE_END);
		
		
		// set up message frame
		messageFrame = new JFrame("Messages");
		messageFrame.setSize(400, 300);
		messageFrame.setLocation(this.getSize().width + 20, 0);
		messageFrame.add(messagePanel);
		messageArea.setEditable(false);
		
		this.setTitle("Data Aquisition Dashboard");
		this.setVisible(true);
		messageFrame.setVisible(true);
	}

	private void initLEDPanel() throws IOException{
		ledDisplayPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		
		// LED Displays
		ledPanels = new ArrayList<LEDPanel>(numLEDDisplays);
		
		// combo boxes
		ledComboBoxes = new ArrayList<JComboBox>(numLEDDisplays);
		
		for(int i = 0; i < numLEDDisplays; i++){
			ledPanels.add(new LEDPanel(numLEDDigits));
			// add this LED panel to the display panel
			ledDisplayPanel.add(ledPanels.get(i));
			
			ledComboBoxes.add(new JComboBox());
			ledComboBoxes.get(i).addActionListener(this);
			// add combo box to display panel
			ledDisplayPanel.add(ledComboBoxes.get(i));
				
		}
		
	}
	
	@Override
	public synchronized void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(entryField)) {
			// this is run when a string is entered into the text entry field
			String text = entryField.getText();
			messageArea.append(text + "\n");
			entryField.setText("");
			try {
				serialComm.serialWrite(text + "\n");
			} catch (Exception e) {
				System.err.println(e);
			}
		} else if (event.getSource().equals(saveButton)) {
			saveInput();

		} else if (event.getSource().equals(setGraphButton)) {
			setGraph();
			autoColumnSelect = false;
		} else if (event.getSource().equals(startStopCollBtn)) {
			// toggle data collection
			stopCollecting = !stopCollecting;
			if (startStopCollBtn.getText().equals("Stop collecting")) {
				startStopCollBtn.setText("Start collecting");
			} else {
				startStopCollBtn.setText("Stop collecting");
			}
		} else if(event.getSource().equals(resetConnectionBtn)){
			// save drop-down selections from graph and all LED displays
			dropDownSettings = new int[ledComboBoxes.size() + 2];
			dropDownSettings[0] = yComboBox.getSelectedIndex();
			dropDownSettings[1] = xComboBox.getSelectedIndex();
			
			// loop through dropdowns and save selected indices
			for(int i = 2; i < dropDownSettings.length; i ++){
				dropDownSettings[i] = ledComboBoxes.get(i-2).getSelectedIndex();
			}
			
			settingsSaved = true;
			
			// reset graph
			graph.clearData();
			// delete data list
			dataList = new LinkedList<Float[]>();
			// clear text and message areas
			textArea.setText("");
			messageArea.setText("");
			//  arduino
			try{
				serialComm.reset();				
			}catch (Exception e) {
				System.err.println(e);
			}
		} 
		else if(event.getSource().equals(chkGraphInstant)){
			graph.setShapesVisible(chkGraphInstant.isSelected());
			if (chkGraphInstant.isSelected()){
				graph.clearData();
			}
		}
	}

	private void setGraph() {
		graphXIndex = xComboBox.getSelectedIndex();
		graphYIndex = yComboBox.getSelectedIndex();

		// set labels on graph to selected columns from combo box
		graph.clearData();
		graph.setAxisLabels(
				headerLabels.length >= graphXIndex ? headerLabels[graphXIndex]
						: "null",
				headerLabels.length >= graphYIndex ? headerLabels[graphYIndex]
						: "null");
		// XYSeries tempDataSeries = new XYSeries(0);

		for (Float[] dataRow : dataList) {
			if (graphXIndex < dataRow.length && graphYIndex <= dataRow.length)
				graph.addPair(dataRow[graphXIndex], dataRow[graphYIndex]);
		}

	}

	private void refreshHeaderDisplay(String headerString) {
		headerDisplay.setText(headerString);
	}

	private void refreshUnitsDisplay(String unitsString) {
		String topLine = headerDisplay.getText().split("\n")[0];
		headerDisplay.setText(topLine + "\n" + unitsString);
		for (int i = 0; i < headerLabels.length && i < unitsLabels.length; i++) {
			headerLabels[i] = headerLabels[i] + " (" + unitsLabels[i] + ")";
		}
		populateComboBox(yComboBox);
		if (settingsSaved && dropDownSettings[0] < yComboBox.getItemCount())
			yComboBox.setSelectedIndex(dropDownSettings[0]);
		else if(!settingsSaved && autoColumnSelect && yComboBox.getItemCount() > 1){
			yComboBox.setSelectedIndex(1);
		}
		populateComboBox(xComboBox);
		if (settingsSaved && dropDownSettings[1] < xComboBox.getItemCount())
			xComboBox.setSelectedIndex(dropDownSettings[1]);
		
		else if(!settingsSaved && autoColumnSelect && xComboBox.getItemCount() > 1){
		// set x axis to be first column by default
			xComboBox.setSelectedIndex(0);
		}		
		
		int cb = 2; // combobox settings counter
		for(JComboBox combobox : ledComboBoxes){
			populateComboBox(combobox);
			// check if saved selection is still valid
			if (settingsSaved && dropDownSettings[cb] < combobox.getItemCount()){
				// set the combobox to saved selection
				combobox.setSelectedIndex(dropDownSettings[cb]);
			}
			if (!settingsSaved && autoColumnSelect && 
					cb - 2 < combobox.getItemCount()){
				combobox.setSelectedIndex(cb-2); // auto select led displays
			}
			cb++;
		}
		
		if(autoColumnSelect)
			setGraph();
	}

	private void populateComboBox(JComboBox comboBox) {
		comboBox.removeAllItems();
		for (String header : headerLabels) {
			comboBox.addItem(header);
		}
	}
	
private void parseString(String inputString, Tag tag) {

	String[] inputArray;
	
	if (tag != Tag.MESSAGE){
		// create an array of the tab separated data elements in the string
		inputArray = inputString.split("\t");
	}
	else{
		inputArray = new String[1]; // dummy variable
	}
	
	switch (tag) {
	// do a different thing depending on the tag of the input
	case HEADER:
		headerLabels = inputArray;
		refreshHeaderDisplay(inputString);
		noHeader = false;
		break;
	
	case UNITS:
		unitsLabels = inputArray;
		refreshUnitsDisplay(inputString);
		noHeader = false;
		break;			

	case ROW:
		if(noHeader){
			// request header info from other party
			try {
				//Thread.sleep(20);
				//serialComm.serialWrite("print header\n");
			} catch (Exception e) {
				System.err.println(e + ": in serialWrite");
			}
			String[] fakeHeader = new String[inputArray.length];
			String[] fakeUnits = new String[inputArray.length];
			StringBuilder headerString = new StringBuilder();
			StringBuilder unitsString = new StringBuilder();

			for(int i = 0; i < inputArray.length; i++){
				headerString.append("var"+i+(i < inputArray.length -1 ? "\t" : ""));
				unitsString.append("unknown"+(i < inputArray.length -1 ? "\t" : ""));
			}

			fakeHeader = headerString.toString().split("\t");
			fakeUnits = unitsString.toString().split("\t");
			
			headerLabels = fakeHeader;
			unitsLabels = fakeUnits;
			
			refreshHeaderDisplay(headerString.toString());
			refreshUnitsDisplay(unitsString.toString());
			
		}
		
		Float[] newData = new Float[inputArray.length];
		for (int i = 0; i < inputArray.length; i++) {
			try {
				// convert the strings into floats
				newData[i] = Float.parseFloat(inputArray[i]);
			} catch (NumberFormatException ex) {
				System.err.println("Number format exception caught: "
						+ inputArray[i] + " is not a valid number");
			}
		}
		dataList.add(newData.clone()); // add the new data set to the big
										// list

		// graph selected values
		
		if(chkGraphInstant.isSelected()){
			graph.clearData();
		}
		
		graph.addPair(dataList.getLast()[graphXIndex],
				dataList.getLast()[graphYIndex]);

			//post values to LED displays according to the selected combo box entry
		for(int i = 0; i < ledPanels.size(); i++){
			ledPanels.get(i).setNumber(newData[ledComboBoxes.get(i).getSelectedIndex()]);
		}
		
		textArea.append(inputString + "\n"); // post row to text area
		if(chkAutoscroll.isSelected())
			textArea.setCaretPosition(textArea.getText().length());
		break;
	
	case MESSAGE: // post message to message window
		messageArea.append(inputString + "\n");
		if(msgAutoscroll.isSelected())
			messageArea.setCaretPosition(messageArea.getText().length());
		break;
	default:
		
		break;
	}

}

public void clearData() {
	dataList.clear();
	graph.clearData();
}

public void saveInput() {

	JFileChooser chooser = new JFileChooser();
	if(saveDirectory.exists())
		chooser.setCurrentDirectory(saveDirectory);
	if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
		saveDirectory = chooser.getCurrentDirectory();
		try {
			FileWriter out = new FileWriter(chooser.getSelectedFile());
			StringBuilder strBuilder = new StringBuilder();
			// write the header info to file
			for (String columnLabel : headerLabels) {
				strBuilder.append(columnLabel);
				strBuilder.append(",");
			}
			out.write(strBuilder.substring(0, strBuilder.length() - 1)
					+ "\n");

			// write all data to the file
			for (Float[] row : dataList) {
				strBuilder = new StringBuilder();
				for (Float column : row) {
					strBuilder.append(String.valueOf(column));
					strBuilder.append(",");
				}
				out.write(strBuilder.substring(0, strBuilder.length() - 1)
						+ "\n");
			}
			out.close();
		} catch (IOException ex) {
			System.err.println("IO error: cannot write to file.");
		}

		Object[] options = {"Yes", "No"};
		int n = JOptionPane.showOptionDialog(this,
				"Would you like to save the message history?",
				"Save Messages?",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
		if(n == 0){
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				saveDirectory = chooser.getCurrentDirectory();
				try {
					FileWriter out = new FileWriter(chooser.getSelectedFile());
					messageArea.write(out);
					out.close();
				} catch (IOException ex) {
					System.err.println("IO error: cannot write to file.");
				}

			}
		}
	}
}

public void incoming(String inputString) {
	if (!stopCollecting){		
		for (Tag tag : Tag.values()) {
			if (inputString.startsWith(tag.toString())) {
				// strip tags and send input to parser
				parseString(cleanString(inputString), tag);
				break;
			}
		}
	} 
}

private String cleanString(String dirtyString) {
	String cleanString;
	int begin = dirtyString.indexOf(">") + 1;
	int end = dirtyString.indexOf("</", begin);
	if (end < begin)
		end = dirtyString.length();
	if (begin <= dirtyString.length() - 1 && end <= dirtyString.length()) {
		cleanString = dirtyString.substring(begin, end);
	} else {
		cleanString = dirtyString;
	}
	return cleanString;
}

@Override
public void windowClosing(WindowEvent e) {
	serialComm.close(); // close the serial port when the gui closes
}

	// window events that we don't bother responding to
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {

		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}
