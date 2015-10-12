package net.seninp.jmotif.sax.tinker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.util.HeatChart;

public class MoviePrinter {

  private static final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
  private static DecimalFormat df = new DecimalFormat("0.00000", otherSymbols);

  private static EuclideanDistance ed = new EuclideanDistance();

  private static final String DAT_FNAME = "src/resources/dataset/depth/0890031.dat";

  private static final int SAX_WINDOW_SIZE = 10;
  private static int cPoint = SAX_WINDOW_SIZE;

  private static final int SAX_PAA_SIZE = 10;
  private static final int SAX_ALPHABET_SIZE = 4;
  private static final double SAX_NORM_THRESHOLD = 0.001;
  private static final NumerosityReductionStrategy SAX_NR_STRATEGY = NumerosityReductionStrategy.NONE;
  private static final int SHINGLE_SIZE = 3;

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(MoviePrinter.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  // ffmpeg -framerate 5 -i frame%04d.png -s:v 1280x720 -vcodec libx264 -profile:v high -crf 20
  // -pix_fmt yuv420p daimler_man.mp4

  public static void main(String[] args) throws Exception {

    SAXProcessor sp = new SAXProcessor();

    // data
    //
    double[] dat = TSProcessor.readFileColumn(DAT_FNAME, 1, 0);
    // TSProcessor tp = new TSProcessor();
    // double[] dat = tp.readTS("src/resources/dataset/asys40.txt", 0);
    // double[] dat = TSProcessor.readFileColumn(filename, columnIdx,
    // sizeLimit)FileColumn(DAT_FNAME, 1, 0);
    consoleLogger.info("read " + dat.length + " points from " + DAT_FNAME);

    String str = "win_width: " + cPoint + "; SAX: W " + SAX_WINDOW_SIZE + ", P " + SAX_PAA_SIZE
        + ", A " + SAX_ALPHABET_SIZE + ", STR " + SAX_NR_STRATEGY.toString();

    int frameCounter = 0;
    int startOffset = cPoint;
    while (cPoint < dat.length - startOffset - 1) {

      if (0 == cPoint % 2) {

        BufferedImage tsChart = getChart(dat, cPoint);

        // bitmap 1
        //
        double[] win1 = Arrays.copyOfRange(dat, cPoint - startOffset, cPoint);
        Map<String, Integer> shingledData1 = sp.ts2Shingles(win1, SAX_WINDOW_SIZE, SAX_PAA_SIZE,
            SAX_ALPHABET_SIZE, SAX_NR_STRATEGY, SAX_NORM_THRESHOLD, SHINGLE_SIZE);
        BufferedImage pam1 = getHeatMap(shingledData1, "pre-window");

        double[] win2 = Arrays.copyOfRange(dat, cPoint, cPoint + startOffset);
        Map<String, Integer> shingledData2 = sp.ts2Shingles(win2, SAX_WINDOW_SIZE, SAX_PAA_SIZE,
            SAX_ALPHABET_SIZE, SAX_NR_STRATEGY, SAX_NORM_THRESHOLD, SHINGLE_SIZE);
        BufferedImage pam2 = getHeatMap(shingledData2, "post-window");

        // the assemble
        //
        BufferedImage target = new BufferedImage(800, 530, BufferedImage.TYPE_INT_ARGB);
        Graphics targetGraphics = target.getGraphics();
        targetGraphics.setColor(Color.WHITE);
        targetGraphics.fillRect(0, 0, 799, 529);

        targetGraphics.drawImage(tsChart, 0, 0, null);

        targetGraphics.drawImage(pam1, 10, 410, null);// draws the first image onto it

        targetGraphics.drawImage(pam2, 120, 410, null);// draws the first image onto it

        targetGraphics.setColor(Color.RED);
        targetGraphics.setFont(new Font("monospaced", Font.PLAIN, 16));
        targetGraphics.drawString(str, 300, 420);

        targetGraphics.setColor(Color.BLUE);
        targetGraphics.setFont(new Font("monospaced", Font.PLAIN, 24));
        double dist = ed.distance(toVector(shingledData1), toVector(shingledData2));
        targetGraphics.drawString("ED=" + df.format(dist), 300, 480);

        // String fileName = new SimpleDateFormat("yyyyMMddhhmmssSS'.png'").format(new Date());
        File outputfile = new File("dframe" + String.format("%04d", frameCounter) + ".png");
        ImageIO.write(target, "png", outputfile);
        frameCounter++;
      }

      cPoint++;
    }
  }

  private static double[] toVector(Map<String, Integer> shingledData1) {
    TreeSet<String> keys = new TreeSet<String>(shingledData1.keySet());
    double[] res = new double[shingledData1.size()];
    int counter = 0;
    for (String shingle : keys) {
      Integer value = shingledData1.get(shingle);
      res[counter] = value;
      counter++;
    }
    return res;
  }

  private static BufferedImage getHeatMap(Map<String, Integer> shingledData1, String title) {
    TreeSet<String> keys = new TreeSet<String>(shingledData1.keySet());
    double[][] heatmapData = new double[8][8];
    int counter = 0;
    for (String shingle : keys) {
      Integer value = shingledData1.get(shingle);
      heatmapData[counter / 8][counter % 8] = value;
      counter++;
    }
    HeatChart chart = new HeatChart(heatmapData);
    chart.setAxisThickness(0);
    chart.setTitle(title);
    chart.setCellSize(new Dimension(10, 10));
    return (BufferedImage) chart.getChartImage();
  }

  private static BufferedImage getChart(double[] tsData, double redDot) {
    try {

      // making the data
      //
      XYSeries dataset = new XYSeries("Series");
      for (int i = 0; i < tsData.length; i++) {
        dataset.add(i, (float) tsData[i]);
      }
      XYSeriesCollection chartXYSeriesCollection = new XYSeriesCollection(dataset);

      XYSeries dot = new XYSeries("Dot");
      dot.add((float) redDot, 0.0f);
      chartXYSeriesCollection.addSeries(dot);

      // set the renderer
      //
      XYLineAndShapeRenderer xyRenderer = new XYLineAndShapeRenderer(true, false);
      xyRenderer.setBaseStroke(new BasicStroke(3));

      xyRenderer.setSeriesPaint(0, new Color(0, 0, 0));
      xyRenderer.setSeriesLinesVisible(0, true);
      xyRenderer.setSeriesShapesVisible(0, false);

      xyRenderer.setSeriesPaint(1, Color.RED);
      xyRenderer.setSeriesLinesVisible(1, false);
      xyRenderer.setSeriesShapesVisible(1, true);

      // X - the time axis
      //
      NumberAxis timeAxis = new NumberAxis();
      timeAxis.setLabel("Time");

      // Y axis
      //
      NumberAxis valueAxis = new NumberAxis("Values");
      valueAxis.setAutoRangeIncludesZero(false);
      valueAxis.setLabel("Values");

      // put these into collection of dots
      //
      XYPlot timeseriesPlot = new XYPlot(chartXYSeriesCollection, timeAxis, valueAxis, xyRenderer);

      // finally, create the chart
      JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, timeseriesPlot, false);

      BufferedImage objBufferedImage = chart.createBufferedImage(800, 400);
      ByteArrayOutputStream bas = new ByteArrayOutputStream();
      try {
        ImageIO.write(objBufferedImage, "png", bas);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      byte[] byteArray = bas.toByteArray();

      InputStream in = new ByteArrayInputStream(byteArray);
      BufferedImage image = ImageIO.read(in);

      return image;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
