package net.seninp.jmotif.sax.tinker;

import java.io.IOException;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class JmotifRSAXTestHelper {

  public static void main(String[] args) throws IOException, SAXException {

    double[] ts = { 0, 0, 0, 0, 0, -0.270340178359072, -0.367828308500142, 0.666980581124872,
        1.87088147328446, 2.14548907684624, -0.480859313143032, -0.72911654245842,
        -0.490308602315934, -0.66152028906509, -0.221049033806403, 0.367003418871239,
        0.631073992586373, 0.0487728723414486, 0.762655178750436, 0.78574757843331,
        0.338239686422963, 0.784206454089066, -2.14265084073625, 2.11325193044223,
        0.186018356196443, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.519132472499234, -2.604783141655,
        -0.244519550114012, -1.6570790528784, 3.34184602886343, 2.10361226260999, 1.9796808733979,
        -0.822247322003058, 1.06850578033292, -0.678811824405992, 0.804225748913681,
        0.57363964388698, 0.437113583759113, 0.437208643628268, 0.989892093383503, 1.76545983424176,
        0.119483882364649, -0.222311941138971, -0.74669456611669, -0.0663660879732063, 0, 0, 0, 0,
        0 };

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    SAXRecords saxExact = sp.ts2saxViaWindow(ts, 30, 3, na.getCuts(3),
        NumerosityReductionStrategy.EXACT, 0.01);
    saxExact.buildIndex();
    for (int i : saxExact.getAllIndices()) {
      System.out.println(i + " -> " + saxExact.getByIndex(i));
    }

    System.out.println(" ===================== ");

    SAXRecords saxMindist = sp.ts2saxViaWindow(ts, 30, 6, na.getCuts(5),
        NumerosityReductionStrategy.MINDIST, 0.01);
    saxExact.buildIndex();
    for (int i : saxMindist.getAllIndices()) {
      System.out.println(i + " -> " + saxMindist.getByIndex(i));
    }

  }

}
