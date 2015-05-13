package net.seninp.jmotif.sax.algorithm;

import java.util.Arrays;
import java.util.Collections;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
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
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by agibsonccc on 9/11/14.
 */
public class DBNExample {

  private static Logger log = LoggerFactory.getLogger(DBNExample.class);

  public static void main(String[] args) throws Exception {
    Nd4j.dtype = DataBuffer.Type.FLOAT;

    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().momentum(0.5)
        .layer(new org.deeplearning4j.nn.conf.layers.RBM())
        .momentumAfter(Collections.singletonMap(3, 0.9))
        .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT).iterations(5)
        .weightInit(WeightInit.DISTRIBUTION).dist(new NormalDistribution(0, 1))
        .lossFunction(LossFunctions.LossFunction.RMSE_XENT).learningRate(1e-1f).nIn(784).nOut(10)
        .list(4).hiddenLayerSizes(new int[] { 500, 250, 200 }).override(new ClassifierOverride(3))
        .build();

    MultiLayerNetwork d = new MultiLayerNetwork(conf);
    d.init();
    d.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(1)));
    DataSetIterator iter = new MnistDataSetIterator(100, 60000);
    while (iter.hasNext()) {
      DataSet next = iter.next();
      d.fit(next);

    }

    iter.reset();

    Evaluation eval = new Evaluation();

    while (iter.hasNext()) {

      DataSet d2 = iter.next();
      INDArray predict2 = d.output(d2.getFeatureMatrix());

      eval.eval(d2.getLabels(), predict2);

    }

    log.info(eval.stats());

  }

}
