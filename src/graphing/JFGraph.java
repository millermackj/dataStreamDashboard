package graphing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
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
	private ArrayList<XYSeries> seriesList;
	private int numSeries = 5; // number of xy series sets 
	private XYSeriesCollection dataset;
	private XYItemRenderer lineRenderer;
	private XYItemRenderer markerRenderer;
	NumberAxis domain;
	NumberAxis range;
	private JFreeChart theChart;
	private XYPlot plot;
	private ChartPanel panel;
	private ArrayList<Color> plotColors;
	
	public void addTrace(final int seriesNum, final double[][] series){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try{
				for(int i = 0; i < series.length; i++)
					seriesList.get(seriesNum).add(series[i][0], series[i][1]);
				}
				catch (Exception e) {
				}
		  }
		});

		
	}
	
	public void addPair(final int seriesNum, final double x, final double y){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try{
				seriesList.get(seriesNum).add(x, y);
				}
				catch (Exception e) {
				}
		  }
		});
	} 
	
	public void addSeries(XYSeries newSeries){
		seriesList.add(newSeries);
	}
	
	public void clearData(final int seriesNum){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try{
					seriesList.get(seriesNum).clear();
				}
				catch (Exception e) {
				}
		  }
		});
	}
	
	public double[] getPlotLimits(){
		double limits[] = {getXmin(), getXmax(), getYmin(), getYmax()}; 
		return limits;
	}
	
	public double getXmin(){
		return plot.getDomainAxis().getRange().getLowerBound();
	}

	public double getXmax(){
		return plot.getDomainAxis().getRange().getUpperBound();
	}
	
	public double getYmin(){
		return plot.getRangeAxis().getRange().getLowerBound();
	}
	
	public double getYmax(){
		return plot.getRangeAxis().getRange().getUpperBound();
	}

	public void setXrange(final double xmin, final double xmax){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				plot.getDomainAxis().setRange(xmin, xmax);
		  }
		});
	}
	
	public void setYrange(final double ymin, final double ymax){
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				plot.getRangeAxis().setRange(ymin, ymax);
		  }
		});
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
	
	public void setShapesVisible(boolean shapes){
		if(shapes){
			plot.setRenderer(markerRenderer);
		}
		else
			plot.setRenderer(lineRenderer);
	}
	
	public ArrayList<Color> getColors(){
		return plotColors;
	}
	
	public JFGraph(String graphTitle, String xAxisLabel, String yAxisLabel, int numSeries){
		this.numSeries = numSeries;
		// list of series colors
		plotColors = new ArrayList<Color>(numSeries);
		plotColors.add(Color.red);
		plotColors.add(new Color(0x419106));
		plotColors.add(Color.blue);
		plotColors.add(Color.magenta);
		plotColors.add(new Color(0x806010));
		
		this.graphTitle = graphTitle;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		
		seriesList = new ArrayList<XYSeries>(numSeries);
		dataset = new XYSeriesCollection();
		
		lineRenderer = new XYLineAndShapeRenderer(true, false);
		markerRenderer = new XYLineAndShapeRenderer(true, true);
		
		// populate list of xy series
		for(int i = 0; i < numSeries; i++){
			seriesList.add(new XYSeries(i));
			dataset.addSeries(seriesList.get(i));
			lineRenderer.setSeriesPaint(i, plotColors.get(i));
			markerRenderer.setSeriesPaint(i, plotColors.get(i));
		}
		
		domain = new NumberAxis(xAxisLabel);
		range = new NumberAxis(yAxisLabel); 
		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
				
		plot = new XYPlot(dataset, domain, range, lineRenderer);
		
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);
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
