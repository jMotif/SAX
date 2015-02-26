Time series discretization with SAX
====

This Java library implements Symbolic Aggregate Approximation for time series.  The code and API are very simple aiming at performance. It provides a multi-threaded SAX version as well. You can find more information about SAX in the following papers:

[1] Lin, J., Keogh, E., Patel, P., and Lonardi, S., [*Finding Motifs in Time Series*](http://cs.gmu.edu/~jessica/Lin_motif.pdf), The 2nd Workshop onTemporal Data Mining, the 8th ACM Int'l Conferenceon KDD (2002)

[2] Patel, P., Keogh, E., Lin, J., Lonardi, S., *Mining Motifs in Massive Time Series Databases*, In Proc. ICDM (2002)

1.0 BUILDING
------------
The code is written in Java and I use Ant to build it:
	
	$ ant -f jar.build.xml 
	Buildfile: /media/Stock/git/jmotif-sax/jar.build.xml
  ...
  [jar] Building jar: /media/Stock/git/jmotif-sax/jmotif-sax20.jar
  [delete] Deleting directory /media/Stock/git/jmotif-sax/tmp

  BUILD SUCCESSFUL
  Total time: 1 second
	
1.0 USAGE
------------	
There two classes which implement sequential end-to-end workflow for SAX and a parallel implementation of the discretization. 

Discretizing time-series via sliding window sequentially:

  // instantiate needed classes
  NormalAlphabet na = new NormalAlphabet();
  SAXProcessor sp = new SAXProcessor();
  
  // read the input file
  double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);

  // perform the discretization
  SAXRecords res = sp.ts2saxViaWindow(ts, slidingWindowSize, paaSize, 
      na.getCuts(alphabetSize), nrStrategy, nThreshold);

  // print the output
  Set<Integer> index = res.getIndexes();
  for (Integer idx : index) {
    System.out.println(idx + COMMA + String.valueOf(res.getByIndex(idx).getPayload()));
  }

Parallel discretization:

  // instantiate needed classes
  NormalAlphabet na = new NormalAlphabet();
  SAXProcessor sp = new SAXProcessor();
  
  // read the input file
  double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);

  // perform the discretization using 8 threads
  ParallelSAXImplementation ps = new ParallelSAXImplementation();
  SAXRecords res = ps.process(ts, threadsNum, slidingWindowSize, paaSize, alphabetSize, 
      nrStrategy, nThreshold);

  // print the output
  Set<Integer> index = res.getIndexes();
  for (Integer idx : index) {
    System.out.println(idx + COMMA + String.valueOf(res.getByIndex(idx).getPayload()));
  }
