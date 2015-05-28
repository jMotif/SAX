package net.seninp.jmotif.sax.algorithm;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.seninp.jmotif.cbf.CBFGenerator;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.util.UCRUtils;

public class SAXBitmapPrinter {

  // formatting parameters
  //
  private static final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
  private static DecimalFormat df = new DecimalFormat("0.000000", otherSymbols);
  // and some constants
  private static final String COMMA = ", ";
  private static final String CR = "\n";

  // classes needed for the workflow
  //
  private static final TSProcessor tsp = new TSProcessor();
  private static final SAXProcessor sp = new SAXProcessor();
  private static final NormalAlphabet na = new NormalAlphabet();

  // discretization parameters
  //
  private static int INSTANCE_SIZE = 128;
  private static final int DATASET_SIZE = 90;

  private static final int WINDOW_SIZE = 40;
  private static final int SHINGLE_SIZE = 5;
  private static final int ALPHABET_SIZE = 4;
  private static final NumerosityReductionStrategy STRATEGY = NumerosityReductionStrategy.NONE;
  private static final double THRESHOLD = 0.001;

  public static void main(String[] args) throws SAXException, IOException {

    // read the training data
    //
    // Map<String, List<double[]>> train =
    // UCRUtils.readUCRData("src/resources/dataset/CBF/CBF_TRAIN");
    Map<String, ArrayList<double[]>> train = makeSet(DATASET_SIZE / 3);
    UCRUtils.saveData(train, "currentCBF.csv");

    // the shingled datastructure
    //
    Map<String, List<double[]>> shingledData = sp.toShingles(train, WINDOW_SIZE, SHINGLE_SIZE,
        ALPHABET_SIZE, STRATEGY, THRESHOLD);

    INSTANCE_SIZE = shingledData.get("1").iterator().next().length;
    System.out.println("Shingles table size: " + INSTANCE_SIZE);

    // here we need to train the NN
    //

  }

  private static Map<String, ArrayList<double[]>> makeSet(int num) {

    // ticks - i.e. time
    int[] t = new int[128];
    for (int i = 0; i < 128; i++) {
      t[i] = i;
    }

    Map<String, ArrayList<double[]>> set = new HashMap<String, ArrayList<double[]>>();

    ArrayList<double[]> c = new ArrayList<double[]>();
    for (int i = 0; i < num; i++) {
      c.add(CBFGenerator.cylinder(t));
    }

    ArrayList<double[]> b = new ArrayList<double[]>();
    for (int i = 0; i < num; i++) {
      b.add(CBFGenerator.bell(t));
    }

    ArrayList<double[]> f = new ArrayList<double[]>();
    for (int i = 0; i < num; i++) {
      f.add(CBFGenerator.funnel(t));
    }

    set.put("1", c);
    set.put("2", b);
    set.put("3", f);

    return set;
  }
}
