package graphing;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleInsets;

public class JFGraph extends JPanel{

	String graphTitle, xAxisLabel, yAxisLabel;
	private XYSeries theData = new XYSeries(0);
	private XYDataset dataset = new XYSeriesCollection(theData);
	
	NumberAxis domain;
	NumberAxis range;
	
	private JFreeChart theChart;
	
	private ChartPanel panel;
		
	public void addPair(double x, double y){
		theData.add(x, y);
	}
	
	public void clearData(){
		theData.clear();
	}
	
	public void setAxisLabels(String xlabel, String ylabel){
		setXAxisLabel(xlabel);
		setYAxisLabel(ylabel);
	}
	
	public void setXAxisLabel(String xlabel){
		xAxisLabel = xlabel;
		domain.setLabel(xAxisLabel);
	}
	
	public void setYAxisLabel(String ylabel){
		yAxisLabel = ylabel;
		range.setLabel(yAxisLabel);
	}
	
	public void useDataSeries(XYSeries dataSeries){
		try {
			theData = dataSeries.createCopy(0, dataSeries.getItemCount() - 1);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JFGraph(String graphTitle, String xAxisLabel, String yAxisLabel){
		this.graphTitle = graphTitle;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		
		domain = new NumberAxis(xAxisLabel);
		range = new NumberAxis(yAxisLabel); 
		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setSeriesPaint(0, Color.red); 

		XYPlot plot = new XYPlot(dataset, domain, range, renderer); 
		plot.setBackgroundPaint(Color.lightGray); 
		plot.setDomainGridlinePaint(Color.white); 
		plot.setRangeGridlinePaint(Color.white); 
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		domain.setAutoRange(true); 
		domain.setLowerMargin(0.0); 
		domain.setUpperMargin(0.0); 
		domain.setTickLabelsVisible(true);
		range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
//		theChart  = ChartFactory.createXYLineChart(
//				graphTitle, 
//				xAxisLabel, 
//				yAxisLabel, 
//				dataset, 
//				org.jfree.chart.plot.PlotOrientation.VERTICAL, // orientation
//				false,  // legend 
//				false,  // tooltips 
//				false // urls
//		);
		theChart = new JFreeChart(graphTitle, new Font("SansSerif", Font.BOLD, 24), plot, false);
		theChart.setBackgroundPaint(Color.white); 
		panel = new ChartPanel(theChart); 
		panel.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createEmptyBorder(4, 4, 4, 4),
		BorderFactory.createLineBorder(Color.black))); add(panel);
		this.add(panel);
	}

	

}
