package graphing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import javax.swing.JPanel;
import java.util.LinkedList;
import java.lang.Integer;
import java.awt.geom.*;
import javax.swing.BorderFactory;

public class Graph extends JPanel{
	public enum AutoManEnum {
		AUTO, MANUAL;
	}
	
	AutoManEnum zoomMode; 
	AutoManEnum scaleMode;
	
	// relative lengths of tick marks
	private double minorTickRatio = 0.03125;
	private double majorTickRatio = 0.0625;
	
	private int majorTickFreq = 5;
	
	// the diameter of points on the graph
	int pointDiam = 4;
	
	private double xMin, xMax, yMin, yMax, xScale, yScale;
	private String xLabel, yLabel;
	
	
	
	private Graphics2D g2;

	LinkedList<double[]> points = new LinkedList<double[]>();

	boolean painted = false;
	
	public void setMajorTickFreq(int freq){
		majorTickFreq = freq;
	}
	
	/**use addPoint to enter a data point to be graphed **/
	public void addPoint(double x, double y){
		double[] newPoint = {x,y}; 
		points.add(newPoint);
		
		// redefine max/min if in auto zoom mode
		if (zoomMode == AutoManEnum.AUTO){
			if (x < xMin){
				xMin = x - 2;
			}
			else if (x > xMax){
				xMax = x + 2;
			}

			if (y < yMin){
				yMin = y - 2;
			}
			else if (y > yMax){
				yMax = y + 2;
			}
		}
		paintPointsAndPath((Graphics2D)this.getGraphics());
	}
	/** Set the axis labels **/
	public void setAxisLabels(String xLabel, String yLabel){
		this.xLabel = xLabel;
		this.yLabel = yLabel;
	}
	
	public Graph(){
		zoomMode = AutoManEnum.AUTO;
		
		setXMin(Integer.MAX_VALUE);
		setXMax(Integer.MIN_VALUE);

	}
	
	public Graph(double xMin, double xMax, double xScale, double yMin, 
			double yMax, double yScale){		
		setZoom(xMin, xMax, xScale, yMin, yMax, yScale);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
	private void setXMin(double minimumX){
		xMin = minimumX;
	}

	private void setYMin(double minimumY){
		yMin = minimumY;
	}
	
	private void setXMax(double maximumX){
		xMax = maximumX;
	}
	
	private void setYMax(double maximumY){
		yMax = maximumY;
	}
	
	private void setXScale(double scaleX){
		xScale = scaleX;
	}
	
	private void setYScale(double scaleY){
		yScale = scaleY;
	}
	
	public void setZoom(double xMin, double xMax, double xScale, double yMin, double yMax,
			double yScale){
		zoomMode = AutoManEnum.MANUAL;
		scaleMode = AutoManEnum.MANUAL;		
		setXMin(xMin);
		setXMax(xMax);
		setYMin(yMin);
		setYMax(yMax);
		setXScale(xScale);
		setYScale(yScale);
	}
	
	private int getX(double xIn){
		double diffX = xMax - xMin;
		return (int)((xIn - xMin)/ diffX * getWidth());
	}
	
	private int getY(double yIn){
		double diffY = yMax - yMin;
		return (int)(-(yIn - yMax)/ diffY * getHeight());
	}
	
	private int[] getCoords(double x, double y){
		double[] point = {x,y};
		return getCoords(point);
	}
	
	private int[] getCoords(double[] point){
		int[] coord = {getX(point[0]), getY(point[1])};
		return coord;
	}
	
	private LinkedList<int[]> getCoords(LinkedList<double[]> points){
		LinkedList<int[]> newCoords = new LinkedList<int[]>();		
		for(double[] point : points){
			newCoords.add(getCoords(point));
		}
		
		return newCoords;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g2 = (Graphics2D)g;

		int originX = getX(0);
		int originY = getY(0);
		
		// draw horizontal axis
		g2.drawLine(0, originY, getWidth(), originY);
		
		// draw vertical axis
		g2.drawLine(originX, 0, originX, getHeight());

		int majorTickLength, minorTickLength, tickCount;
		
		// draw graduation ticks on horizontal axis
		int heightOrWidth = (getHeight() > getWidth() ? getHeight() : getWidth());
		minorTickLength = (int)((minorTickRatio * heightOrWidth) / 2.0);
		majorTickLength = (int)((majorTickRatio * heightOrWidth) / 2.0);
		
		// to left of origin
		tickCount = 0;
		for (double ix = -xScale; ix > xMin; ix -= xScale){
			tickCount++;
			int tickLength = (tickCount % majorTickFreq == 0 ? majorTickLength 
					: minorTickLength);
			int[] coord = getCoords(ix, 0);
			g2.drawLine(coord[0], coord[1] - tickLength, coord[0], coord[1] + tickLength);
		}
		
		// to right of origin
		tickCount = 0;
		for (double ix = xScale; ix < xMax; ix += xScale){
			tickCount++;
			int tickLength = (tickCount % majorTickFreq == 0 ? majorTickLength 
					: minorTickLength);
			int[] coord = getCoords(ix, 0);
			g2.drawLine(coord[0], coord[1] - tickLength, coord[0], coord[1] + tickLength);
		}		
		// draw graduation ticks on vertical axis	
		
		// below origin
		tickCount = 0;
		for (double iy = -yScale; iy > yMin; iy -= yScale){
			tickCount++;
			int tickLength = (tickCount % majorTickFreq == 0 ? majorTickLength 
					: minorTickLength);
			int[] coord = getCoords(0, iy);
			g2.drawLine(coord[0] - tickLength, coord[1], coord[0] + tickLength,
					coord[1]);
		}
		
		// above origin
		tickCount = 0;
		for (double iy = yScale; iy < yMax; iy += yScale){
			tickCount++;
			int tickLength = (tickCount % majorTickFreq == 0 ? majorTickLength 
					: minorTickLength);
			int[] coord = getCoords(0, iy);
			g2.drawLine(coord[0] - tickLength, coord[1], coord[0] + tickLength,
					coord[1]);
		}		
		paintPointsAndPath(g2);

	}
	
	private void paintPoint(int[] point, Graphics2D g2){
		g2.fill(new Ellipse2D.Double(point[0] - pointDiam / 2, point[1] - pointDiam / 2, pointDiam, pointDiam));
	}

	private void paintPointsAndPath(Graphics2D g2){
		LinkedList<int[]> coords = getCoords(points);
		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, coords.size());

		if (!coords.isEmpty())
			path.moveTo(coords.getFirst()[0], coords.getFirst()[1]);
			
		for(int[] point: coords){
			paintPoint(point, g2);
			path.lineTo(point[0], point[1]);
		}
		
		g2.draw(path);		
	}
	public void clearData(){
		points.clear();
		this.paintComponent(this.getGraphics());
	}
}
