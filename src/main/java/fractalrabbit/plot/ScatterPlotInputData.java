package fractalrabbit.plot;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.style.Styler.LegendPosition;

import java.util.Arrays;
import java.util.Random;

/**
 * Scatter plot of a fractal point cloud.
 *
 * @author rwdarli
 * @see fractalrabbit.simulation.AgoraphobicPoints
 */
public class ScatterPlotInputData implements ExampleChart<XYChart> {
    private final double[] x;
    private final double[] y;
    private final String legend;

    public ScatterPlotInputData(String title, double[] xData, double[] yData) {
        x = xData;
        y = yData;
        legend = title;
    }

    @Override
    public XYChart getChart() {
        XYChart chart = new XYChartBuilder().width(800).height(600).build();
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        chart.getStyler().setChartTitleVisible(true);
        chart.getStyler().setLegendPosition(LegendPosition.InsideSW);
        chart.getStyler().setMarkerSize(4);
        chart.addSeries(legend, x, y);
        return chart;
    }

    @Override
    public String getExampleChartName() {
        return "Scatter Plot";
    }

    /**
     * {@code args[0]} is the number of uniform points to plot.
     */
    static void main(String[] args) {
        Random random = new Random();
        int count = Integer.parseInt(args[0]);
        double[] xData = new double[count];
        double[] yData = new double[count];
        Arrays.setAll(xData, index -> random.nextDouble());
        Arrays.setAll(yData, index -> random.nextDouble());
        XYChart chart = new ScatterPlotInputData("test", xData, yData).getChart();
        new SwingWrapper<>(chart).displayChart();
    }
}
