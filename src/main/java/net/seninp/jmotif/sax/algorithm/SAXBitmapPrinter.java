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
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.util.UCRUtils;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.override.ClassifierOverride;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

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
  private static final int DATASET_SIZE = 1800;

  private static final int WINDOW_SIZE = 50;
  private static final int SHINGLE_SIZE = 5;
  private static final int ALPHABET_SIZE = 3;
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
        
    // here we need to train the NN
    //
    List<String> outcomeTypes = new ArrayList<String>();
    double[][] outcomes = new double[DATASET_SIZE][3];

    INDArray data = Nd4j.ones(DATASET_SIZE, INSTANCE_SIZE);
    int sampleCounter = 0;
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

    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().momentum(0.5)
        .layer(new org.deeplearning4j.nn.conf.layers.RBM())
        .momentumAfter(Collections.singletonMap(5, 0.9))
        .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT).iterations(50)
        .weightInit(WeightInit.DISTRIBUTION).dist(new NormalDistribution(0, 1))
        .lossFunction(LossFunctions.LossFunction.RMSE_XENT).learningRate(1e-1f).nIn(INSTANCE_SIZE)
        .nOut(3).list(4).hiddenLayerSizes(new int[] { 128, 64, 32 })
        .override(new ClassifierOverride(3)).build();

    MultiLayerNetwork d = new MultiLayerNetwork(conf);
    d.init();
    d.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(1)));

    d.fit(completedData);

    Evaluation eval = new Evaluation();
    INDArray output = d.output(completedData.getFeatureMatrix());
    eval.eval(completedData.getLabels(), output);
    System.out.println("Score " + eval.stats());

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
