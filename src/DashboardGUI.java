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
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.*;
import javax.swing.border.Border;

import graphing.*;

import org.jfree.base.log.PadMessage;
import org.jfree.data.xy.XYSeries;
import java.util.LinkedList;
import java.util.ArrayList;

public class DashboardGUI extends JFrame implements ActionListener,
		WindowListener {
	private static final long serialVersionUID = 1L;
	private String[] headerLabels;
	private String[] unitsLabels;
	private LinkedBlockingDeque<double[]> dataList;
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
	
	private ButtonGroup grpLimitsRadio = new ButtonGroup();
	private JRadioButton radRoll = new JRadioButton("Rolling X");
	private JRadioButton radManual = new JRadioButton("Manual Limits");
	
	private JTextField txtRollTime;
	
	private JLabel lblXmin = new JLabel("X min:");
	private JTextField txtXmin;
	private JLabel lblXmax = new JLabel("X max:");
	private JTextField txtXmax;
	private JLabel lblYmin = new JLabel("Y min:");
	private JTextField txtYmin;
	private JLabel lblYmax = new JLabel("Y max:");
	private JTextField txtYmax;
	
	private ArrayList<JComponent> manualList = new ArrayList<JComponent>();
	private ArrayList<JComponent> rollList = new ArrayList<JComponent>();
	
	private JLabel lblRollTime = new JLabel("Roll X: ");
	private JFGraph graph;
	private JComboBox xComboBox;
	private JComboBox yComboBox;
	
	private ArrayList<JComboBox> ledComboBoxes;
	private ArrayList<JCheckBox> ledCheckBoxes;
	
	private JPanel ledDisplayPanel = new JPanel();
	private ArrayList<LEDPanel> ledPanels;
	private int numLEDDisplays = 4;
	private int numLEDDigits = 4;
	private int numSeriesPlots = numLEDDisplays + 1; // max concurrent plots
	private boolean settingsSaved = false;
	private int[] dropDownSettings;
	private boolean noHeader = true;
	private boolean autoColumnSelect = true;
	private File saveDirectory = new File(""); // directory where last file was saved
	private boolean ignoreDropdownEvents = true;
	private boolean checkedSeries[] = new boolean[numLEDDisplays];
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
	private int graphYIndex[] = new int[numSeriesPlots];

	private double rolltime = 10;
	
	private boolean stopCollecting = false;

	public DashboardGUI(int width, int height,
			ArrayBlockingQueue<String> outputBuffer,
			SerialCommunicator serialComm) throws IOException {
		this.outputBuffer = outputBuffer;
		dataList = new LinkedBlockingDeque<double[]>();
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
		graph = new JFGraph("", "", "" ,5); // 5 series plot
		// graph.useDataSeries(dataSeries);

		upperPanel.add(saveButton);
		upperPanel.add(startStopCollBtn);
		upperPanel.add(resetConnectionBtn);
		
		// objects in graph select panel
		xComboBox = new JComboBox();
		yComboBox = new JComboBox();
		JLabel xComboLabel = new JLabel("X-data:");
		JLabel yComboLabel = new JLabel("Y-data:");
		
		xComboBox.addActionListener(this);
		yComboBox.addActionListener(this);
		
		setGraphButton = new JButton("Set Graph");
		setGraphButton.addActionListener(this);

		Dimension txtDim = new Dimension(200, 20);
		
		txtXmin = new JTextField(4);
		txtXmin.setMaximumSize(txtDim);
		txtXmin.setMinimumSize(txtDim);
		txtXmin.setPreferredSize(txtDim);
		
		txtXmin.addActionListener(this);

		txtXmax = new JTextField(4);
		txtXmax.setMaximumSize(txtDim);
		txtXmax.setMinimumSize(txtDim);
		txtXmax.setPreferredSize(txtDim);
		
		txtXmax.addActionListener(this);
		
		txtYmin = new JTextField(4);
		txtYmin.setMaximumSize(txtDim);
		txtYmin.setMinimumSize(txtDim);
		txtYmin.setPreferredSize(txtDim);
		txtYmin.addActionListener(this);

		
		txtYmax = new JTextField(4);
		txtYmax.setMaximumSize(txtDim);
		txtYmax.setMinimumSize(txtDim);
		txtYmax.setPreferredSize(txtDim);
		txtYmax.addActionListener(this);
		
		grpLimitsRadio.add(radManual);
		
		
		txtRollTime = new JTextField("10", 4); // default 10 seconds
		txtRollTime.setMaximumSize(txtDim);
		txtRollTime.setMinimumSize(txtDim);
		txtRollTime.setPreferredSize(txtDim);
		txtRollTime.addActionListener(this);
		
		grpLimitsRadio.add(radRoll);

		radManual.addActionListener(this);
		radRoll.addActionListener(this);
		
		radRoll.setSelected(true);

		graphSelectPanel.add(yComboLabel);
		graphSelectPanel.add(yComboBox);

		//graphSelectPanel.add(setGraphButton);
		
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
		
		chkGraphInstant = new JCheckBox("Plot instant only");
		chkGraphInstant.addActionListener(this);
		
		textArea = new JTextArea(8, 50);
		textArea.setEditable(false);

		scrollPane = new JScrollPane(textArea);

		entryField = new JTextField();
		entryField.addActionListener(this);

		headerDisplay = new JTextArea(1, 50);
				
		rollList.add(lblRollTime);
		rollList.add(txtRollTime);
			
		manualList.add(lblYmin);
		manualList.add(txtYmin);
		manualList.add(lblYmax);
		manualList.add(txtYmax);
				
		manualList.add(lblXmin);
		manualList.add(txtXmin);
		manualList.add(lblXmax);
		manualList.add(txtXmax);

		checkboxPanel.add(chkGraphInstant);
		checkboxPanel.add(chkAutoscroll);
		
		checkboxPanel.add(radManual);
		checkboxPanel.add(radRoll);
		
		
		JSeparator vertSeparator = new JSeparator(JSeparator.VERTICAL);
		vertSeparator.setMaximumSize(new Dimension(15,Integer.MAX_VALUE));
		checkboxPanel.add(vertSeparator);		
		
		for(JComponent component: manualList){
			checkboxPanel.add(component);
			component.setVisible(false);
		}
		checkboxPanel.add(lblRollTime);
		checkboxPanel.add(txtRollTime);
		checkboxPanel.add(Box.createHorizontalGlue());
	
		//checkboxParent.add(checkboxPanelLeft, BorderLayout.WEST);
		//checkboxParent.add(checkboxPanelRight, BorderLayout.EAST);
		
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
		ledCheckBoxes = new ArrayList<JCheckBox>(numLEDDisplays);
		ArrayList<JPanel> panelRows = new ArrayList<JPanel>(numLEDDisplays);
		
		for(int i = 0; i < numLEDDisplays; i++){
			ledPanels.add(new LEDPanel(numLEDDigits));
			// add this LED panel to the display panel
			ledDisplayPanel.add(ledPanels.get(i));
			
			ledComboBoxes.add(new JComboBox());
			ledComboBoxes.get(i).addActionListener(this);
			ledCheckBoxes.add(new JCheckBox());
			ledCheckBoxes.get(i).addActionListener(this);
			
			// add combo box to display panel
			panelRows.add(new JPanel());
			panelRows.get(i).setLayout(new BoxLayout(panelRows.get(i), BoxLayout.X_AXIS));
			panelRows.get(i).add(ledCheckBoxes.get(i));
			panelRows.get(i).add(ledComboBoxes.get(i));
			ledDisplayPanel.add(panelRows.get(i));
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
		} 
		else if(event.getSource().equals(xComboBox) && !ignoreDropdownEvents){
			//System.out.println("xComboBox: " + event.getActionCommand());
			setGraph();
			autoColumnSelect = false;
		}
		else if(event.getSource().equals(yComboBox) && !ignoreDropdownEvents){
			//System.out.println("yComboBox: " + event.getActionCommand());
			setGraph();
			autoColumnSelect = false;			
		}
		else if (event.getSource().equals(startStopCollBtn)) {
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
			
			// reset graphs
			for(int i = 0; i < numSeriesPlots; i++)
				graph.clearData(i);
			
			// delete data list
			dataList = new LinkedBlockingDeque<double[]>();
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
//			if (chkGraphInstant.isSelected()){
//				radManual.setSelected(true);
//				for(JComponent component: manualList)
//					component.setVisible(true);
//				for(JComponent component: rollList)
//					component.setVisible(false);
//				txtXmin.setText(String.format("%.3f", graph.getXmin()));
//				txtXmax.setText(String.format("%.3f", graph.getXmax()));
//				txtYmin.setText(String.format("%.3f", graph.getYmin()));
//				txtYmax.setText(String.format("%.3f", graph.getYmax()));				
//			}
			
		}
		else if(event.getSource().equals(txtRollTime)){
			try{
				rolltime = Double.parseDouble(txtRollTime.getText());
			}
			catch (NumberFormatException e){
				
			}
		}
		else if(event.getSource().equals(radRoll) || event.getSource().equals(radManual)){
			boolean rollSelected = radRoll.isSelected();
			for(JComponent component: manualList)
				component.setVisible(!rollSelected);
			for(JComponent component: rollList)
				component.setVisible(rollSelected);
			
			if(!rollSelected){ // if manual limits are selected
					txtXmin.setText(String.format("%.3f", graph.getXmin()));
					txtXmax.setText(String.format("%.3f", graph.getXmax()));
					txtYmin.setText(String.format("%.3f", graph.getYmin()));
					txtYmax.setText(String.format("%.3f", graph.getYmax()));				
					setPlotRange();
			}			
		}
		
		else if(event.getSource().equals(txtXmin) 
				|| event.getSource().equals(txtXmax) 
				|| event.getSource().equals(txtYmin)
				|| event.getSource().equals(txtYmax)){
			setPlotRange();
		}
		else if(event.getSource().getClass().equals(ledCheckBoxes.get(0).getClass())){
			for(int i = 0; i < ledCheckBoxes.size(); i++){
				// if the combo box selection is valid, and either the checkbox is 
				// selected but wasn't before, or the trace selection has changed				
				if( ledComboBoxes.get(i).getSelectedIndex() > -1 &&
						((ledCheckBoxes.get(i).isSelected() && !checkedSeries[i])
						|| (graphYIndex[i+1] != ledComboBoxes.get(i).getSelectedIndex()))){

					graphYIndex[i+1] = ledComboBoxes.get(i).getSelectedIndex();
					graph.clearData(i);

					// copy old data into series
					for(double[] row : dataList){
						graph.addPair(i+1, row[graphXIndex], row[graphYIndex[i+1]]);
					}

				}
				// if checkbox is unselected or combo box selection is invalid
				else if(!ledCheckBoxes.get(i).isSelected() 
						|| ledComboBoxes.get(i).getSelectedIndex() == -1 
						|| ledComboBoxes.get(i).getSelectedIndex() >= numSeriesPlots){ 
					graph.clearData(i+1);
				}
			}
		}
		
	}
	
private void setPlotRange(){
	double xmin, xmax, ymin, ymax;			
	
	try{
		xmin = Double.parseDouble(txtXmin.getText());
	}
	catch(NumberFormatException e){
		txtXmin.setText(Double.toString(xmin = graph.getXmin()));
	}
	
	try{
		xmax = Double.parseDouble(txtXmax.getText());
	}
	catch(NumberFormatException e){
		txtXmax.setText(Double.toString(xmax = graph.getXmax()));
	}
	
	try{
		ymin = Double.parseDouble(txtYmin.getText());
	}
	catch(NumberFormatException e){
		txtYmin.setText(Double.toString(ymin = graph.getYmin()));
	}
	
	try{
		ymax = Double.parseDouble(txtYmax.getText());		
	}
	catch(NumberFormatException e){
		txtYmax.setText(Double.toString(ymax = graph.getYmax()));
	}
	
	graph.setXrange(xmin, xmax);
	graph.setYrange(ymin, ymax);
	
}
	
	private void setGraph() {
		ignoreDropdownEvents = true;
		// set primary series
		graphXIndex = xComboBox.getSelectedIndex();
		graphYIndex[0] = yComboBox.getSelectedIndex();

		// set labels on graph to selected columns from combo box
		for(int i = 0; i < numSeriesPlots; i++)
			graph.clearData(i);
		
		graph.setAxisLabels(
				headerLabels.length >= graphXIndex && graphXIndex > -1? headerLabels[graphXIndex]
						: "null",
				headerLabels.length >= graphYIndex[0]  && graphYIndex[0] > -1? headerLabels[graphYIndex[0]]
						: "null");
//		XYSeries tempDataSeries = new XYSeries(0);

		for (double[] dataRow : dataList) {
			if (graphXIndex > -1 && graphXIndex < dataRow.length){
				// plot primary series
				if (graphYIndex[0] > -1  && graphYIndex[0] <= dataRow.length)
					graph.addPair(0, dataRow[graphXIndex], dataRow[graphYIndex[0]]);

				// plot rest of series
				for(int i = 1; i < numSeriesPlots; i++){
					if (ledCheckBoxes.get(i-1).isSelected() && graphYIndex[i] > -1 
							&& graphYIndex[i] <= dataRow.length)
						graph.addPair(i, dataRow[graphXIndex], dataRow[graphYIndex[i]]);
				}
			}
		}

		ignoreDropdownEvents = false;
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
		
		double[] newData = new double[inputArray.length];
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
			for(int i = 0; i < numSeriesPlots; i++)
				graph.clearData(i);
		}
		if(radRoll.isSelected() &&  graphXIndex > -1)
			graph.setXrange((double)dataList.getLast()[graphXIndex] - rolltime, 
					(double)dataList.getLast()[graphXIndex] + 0.01 * rolltime);

		
		// send primary series values
		if (graphXIndex > -1 && graphXIndex < newData.length){
			// plot primary series
			if (graphYIndex[0] > -1  && graphYIndex[0] <= newData.length)
				graph.addPair(0, newData[graphXIndex], newData[graphYIndex[0]]);

			// send rest of series values
			for(int i = 1; i < numSeriesPlots; i++){
				if (ledCheckBoxes.get(i-1).isSelected() && graphYIndex[i] > -1 
						&& graphYIndex[i] <= newData.length)
					graph.addPair(i, newData[graphXIndex], newData[graphYIndex[i]]);
			}
		}

			//post values to LED displays according to their selected combo box entries.
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
	for(int i = 0; i < numSeriesPlots; i++)
		graph.clearData(i);
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
			for (double[] row : dataList) {
				strBuilder = new StringBuilder();
				for (double column : row) {
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
