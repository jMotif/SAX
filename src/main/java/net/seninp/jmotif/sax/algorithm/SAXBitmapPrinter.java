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
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.datastructures.SaxRecord;
import net.seninp.util.UCRUtils;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.override.ClassifierOverride;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.nn.conf.layers.RBM;

public class SAXBitmapPrinter {

  private static final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
  private static DecimalFormat df = new DecimalFormat("0.000000", otherSymbols);

  private static final String[] ALPHABET = { "a", "b", "c", "d", "e", "f" };
  private static final int SHINGLE_SIZE = 6;

  private static final Object COMMA = ", ";
  private static final Object CR = "\n";

  public static void main(String[] args) throws SAXException, IOException {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    // read the training data
    //
    Map<String, List<double[]>> train = UCRUtils.readUCRData("src/resources/dataset/CBF/CBF_TRAIN");
    Map<String, List<double[]>> shingledData = new HashMap<String, List<double[]>>();

    // build all shingles
    //
    String[] allStrings = getAllLists(ALPHABET, SHINGLE_SIZE);
    //
    // and make an index table
    int len = allStrings.length;
    HashMap<String, Integer> indexTable = new HashMap<String, Integer>();
    for (int i = 0; i < allStrings.length; i++) {
      indexTable.put(allStrings[i], i);
    }

    // some info printout
    System.out.println("Using words: " + Arrays.toString(allStrings).replace(", ", "\", \""));

    // iterate ofer all training series
    //
    int sampleCounter = 0;
    for (Entry<String, List<double[]>> e : train.entrySet()) {
      System.out.println(e.getKey());
      for (double[] series : e.getValue()) {

        // discretize the timeseries
        SAXRecords saxData = sp.ts2saxViaWindow(series, 60, SHINGLE_SIZE,
            na.getCuts(ALPHABET.length), NumerosityReductionStrategy.EXACT, 0.001);

        // allocate the weights array corresponding to the timeseries
        double[] weights = new double[len];

        // fill in the counts
        for (SaxRecord sr : saxData) {
          String word = String.valueOf(sr.getPayload());
          Integer idx = indexTable.get(word);
          if (null == idx) {
            System.out.println(word);
          }
          weights[idx] = sr.getIndexes().size();
        }

        // get max value
        double max = Double.MIN_VALUE;
        for (int i = 0; i < len; i++) {
          if (weights[i] > max) {
            max = weights[i];
          }
        }

        // normalize
        for (int i = 0; i < len; i++) {
          weights[i] = weights[i] / max;
        }

        // save that
        if (!shingledData.containsKey(e.getKey())) {
          shingledData.put(e.getKey(), new ArrayList<double[]>());
        }
        shingledData.get(e.getKey()).add(weights);

        // printout weights
        StringBuffer sb = new StringBuffer(len * len * 8);
        for (int i = 0; i < len; i++) {
          if (i < len - 1) {
            sb.append(df.format(weights[i])).append(COMMA);
          }
          else {
            sb.append(df.format(weights[i]));
          }
        }
        sb.append(CR);
        System.out.print(sb.toString());

        sampleCounter++;
      }
    }

    // here we need to train the NN
    //
    List<String> outcomeTypes = new ArrayList<String>();
    double[][] outcomes = new double[sampleCounter][3];

    INDArray data = Nd4j.ones(sampleCounter, len);
    sampleCounter = 0;
    for (Entry<String, List<double[]>> e : shingledData.entrySet()) {
      if (!outcomeTypes.contains(e.getKey()))
        outcomeTypes.add(e.getKey());

      for (double[] series : e.getValue()) {
        data.putRow(sampleCounter, Nd4j.create(series));

        double[] rowOutcome = new double[3];
        rowOutcome[outcomeTypes.indexOf(e.getKey())] = 1;
        outcomes[sampleCounter] = rowOutcome;

        sampleCounter++;
      }
    }

    DataSet completedData = new DataSet(data, Nd4j.create(outcomes));

    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().momentum(0.9)
        .layer(new org.deeplearning4j.nn.conf.layers.RBM())
        .momentumAfter(Collections.singletonMap(3, 0.9))
        .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT).iterations(50)
        .weightInit(WeightInit.DISTRIBUTION).dist(new NormalDistribution(0, 1))
        .lossFunction(LossFunctions.LossFunction.RMSE_XENT).learningRate(0.01).nIn(len).nOut(3)
        .list(4).hiddenLayerSizes(new int[] { 500, 400, 100 }).override(new ClassifierOverride(3))
        .build();

    MultiLayerNetwork d = new MultiLayerNetwork(conf);
    d.init();
    d.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(1)));

    d.fit(completedData);

    Evaluation eval = new Evaluation();
    INDArray output = d.output(completedData.getFeatureMatrix());
    eval.eval(completedData.getLabels(), output);
    System.out.println("Score " + eval.stats());

  }

  public static String[] getAllLists(String[] elements, int lengthOfList) {
    // initialize our returned list with the number of elements calculated above
    String[] allLists = new String[(int) Math.pow(elements.length, lengthOfList)];

    // lists of length 1 are just the original elements
    if (lengthOfList == 1)
      return elements;
    else {
      // the recursion--get all lists of length 3, length 2, all the way up to 1
      String[] allSublists = getAllLists(elements, lengthOfList - 1);

      // append the sublists to each element
      int arrayIndex = 0;

      for (int i = 0; i < elements.length; i++) {
        for (int j = 0; j < allSublists.length; j++) {
          // add the newly appended combination to the list
          allLists[arrayIndex] = elements[i] + allSublists[j];
          arrayIndex++;
        }
      }

      return allLists;
    }
  }
}
