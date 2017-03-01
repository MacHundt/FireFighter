package view.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

import model.datatypes.ColorMap;
import model.datatypes.Hose;
import model.datatypes.HoseLine;
import model.datatypes.HosePoint;
import model.datatypes.Pump;
import model.datatypes.XYLineAndShapeRendererOwn;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

public class Chart extends Component{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private XYDataset dataset;
	private HoseLine lines;
	private List<Pump> pumps;
	
	private double minEle = Double.MAX_VALUE;
	private double maxEle = Double.MIN_VALUE;
	
	public Chart() {
		
	}
	
	public ChartPanel createChart(HoseLine line) {
		lines = line;
		dataset = createDataset(); 
		JFreeChart chart = ChartFactory.createXYLineChart(
	            "Elevation Profile",      // chart title
	            null,                      // x axis label
	            null,                      // y axis label
	            dataset,                  // data
	            PlotOrientation.VERTICAL,
	            false,                     // include legend
	            false,                     // tooltips
	            false                     // urls
	        );

	        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
	        chart.setBackgroundPaint(Color.white);

	        // get a reference to the plot for further customisation...
	        final XYPlot plot = chart.getXYPlot();
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setDomainGridlinePaint(Color.white);
	        plot.setRangeGridlinePaint(Color.white);
	        if (!(lines == null)) {
	        	ColorMap colormap = lines.getColorMap();
		        
	        	//schlaeuche
		        final XYLineAndShapeRendererOwn renderer = new XYLineAndShapeRendererOwn();
		        for (int i = 0; i < lines.getHoses().size(); i++) {
		        	renderer.setSeriesLinesVisible(i, true);
		 	        renderer.setSeriesShapesVisible(i, false);
		 	        renderer.setSeriesPaint(i, colormap.getColor(lines.getHoses().get(i).getRise()));
		 	        renderer.setSeriesStroke(i, new BasicStroke(3.5f));
		        };
		        
		        //pumpen
		        for (int i = lines.getHoses().size(); i < dataset.getSeriesCount(); i++) {
		        	renderer.setSeriesLinesVisible(i, false);
		        	Shape cross = ShapeUtilities.createDiagonalCross(3, 1);
		        	renderer.setSeriesShape(i, cross);
		        	renderer.setSeriesShapesVisible(i, true);
		        }
		        
		        //incident
		        renderer.setSeriesLinesVisible(dataset.getSeriesCount()-1, false);
	        	Shape cross = new Rectangle2D.Double(5.0,5.0,5.0,5.0);
	        	renderer.setSeriesShape(dataset.getSeriesCount()-1, cross);
	        	renderer.setSeriesShapesVisible(dataset.getSeriesCount()-1, true);
		    
		        plot.setRenderer(renderer);
		        
		        ValueAxis rangeAxis = plot.getRangeAxis();
		        rangeAxis.setRange(minEle-10, maxEle+10);
		        
		        ValueAxis domainAxis = plot.getDomainAxis();
		        domainAxis.setRange(-50, lines.getLength()+70);
	        }
		return new ChartPanel(chart);
	}

	private XYDataset createDataset() {
		if (lines == null) {
			return null;
		}
		
		pumps = lines.getPumps();
		
		final XYSeriesCollection dataset = new XYSeriesCollection();
		
		for (Hose h : lines.getHoses()) {
			minEle = Math.min(minEle, h.getStart().getElevation());
			minEle = Math.min(minEle, h.getEnd().getElevation());
			maxEle = Math.max(maxEle, h.getStart().getElevation());
			maxEle = Math.max(maxEle, h.getEnd().getElevation());
			final XYSeries series = new XYSeries("Hose:"+h.getStart().getPosition());
			series.add(h.getStart().getPosition(), h.getStart().getElevation());
			series.add(h.getEnd().getPosition(), h.getEnd().getElevation());
			dataset.addSeries(series);
		}
		
		for (Pump p: pumps) {
			final XYSeries series = new XYSeries("Pump:"+p.getPosition());
			series.add(p.getPosition(), p.getElevation());
			dataset.addSeries(series);
		}
        
		HosePoint p = lines.getHoses().get(lines.getHoses().size()-1).getEnd();
		final XYSeries series = new XYSeries("Incident:"+p.getPosition());
		series.add(p.getPosition(), p.getElevation());
		dataset.addSeries(series);
		
        return dataset;
	}

}
