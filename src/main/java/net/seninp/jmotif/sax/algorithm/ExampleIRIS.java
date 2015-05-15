package net.seninp.jmotif.sax.algorithm;

import java.io.IOException;
import java.util.Arrays;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.conf.override.ClassifierOverride;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by agibsonccc on 9/12/14.
 */
public class ExampleIRIS {

  private static Logger log = LoggerFactory.getLogger(ExampleIRIS.class);

  public static void main(String[] args) throws IOException {
    Nd4j.MAX_SLICES_TO_PRINT = -1;
    Nd4j.MAX_ELEMENTS_PER_SLICE = -1;
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().iterations(100)
        .layer(new RBM()).weightInit(WeightInit.DISTRIBUTION).dist(new UniformDistribution(0, 1))
        .activationFunction("tanh").momentum(0.9).optimizationAlgo(OptimizationAlgorithm.LBFGS)
        .constrainGradientToUnitNorm(true).k(1).regularization(true).l2(2e-4)
        .visibleUnit(RBM.VisibleUnit.GAUSSIAN).hiddenUnit(RBM.HiddenUnit.RECTIFIED)
        .lossFunction(LossFunctions.LossFunction.RMSE_XENT).learningRate(1e-1f).nIn(4).nOut(3)
        .list(2).hiddenLayerSizes(new int[] { 3 }).override(new ClassifierOverride(1)).build();

    MultiLayerNetwork d = new MultiLayerNetwork(conf);
    d.init();
    d.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(1)));

    DataSetIterator iter = new IrisDataSetIterator(150, 150);

    DataSet next = iter.next();

    Nd4j.writeTxt(next.getFeatureMatrix(), "iris.txt", "\t");

    next.normalizeZeroMeanZeroUnitVariance();

    SplitTestAndTrain testAndTrain = next.splitTestAndTrain(110);
    DataSet train = testAndTrain.getTrain();

    d.fit(train);

    DataSet test = testAndTrain.getTest();

    Evaluation eval = new Evaluation();
    INDArray output = d.output(test.getFeatureMatrix());
    eval.eval(test.getLabels(), output);
    log.info("Score " + eval.stats());
  }
}