Time series discretization with SAX.
====

This library implements Symbolic Aggregate Approximation for time series in Java. The code and API are aiming at performance implementing a multi-threaded SAX discretization. 

The library is **[available through Maven](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jmotif-sax%22)**.

Here is the SAX transform in a nutshell, a time series of 128 points into the word of 8 letters:

![SAX in a nutshell](https://raw.githubusercontent.com/jMotif/SAX/master/src/resources/sax.png)

You can find more information about SAX at its authors pages: [(i) SAX overview by Jessica Lin](http://cs.gmu.edu/~jessica/sax.htm), [(ii) Eamonn Keogh's SAX page](http://www.cs.ucr.edu/~eamonn/SAX.htm), or at [sax-vsm wiki page](http://jmotif.github.io/sax-vsm_site/morea/algorithm/SAX.html).

The key publications introducing SAX:

[1] Lin, J., Keogh, E., Patel, P., and Lonardi, S., [*Finding Motifs in Time Series*](http://cs.gmu.edu/~jessica/Lin_motif.pdf), The 2nd Workshop onTemporal Data Mining, the 8th ACM Int'l Conferenceon KDD (2002)

[2] Patel, P., Keogh, E., Lin, J., Lonardi, S., [*Mining Motifs in Massive Time Series Databases*](http://www.cs.gmu.edu/~jessica/publications/motif_icdm02.pdf), In Proc. ICDM (2002)

If you are using this implementation for you academic work, please cite this paper (it may seems odd, but that work made me working on this lib):

[Citation] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [**GrammarViz 2.0: a tool for grammar-based pattern discovery in time series**](http://www2.hawaii.edu/~senin/assets/papers/grammarviz2.pdf), ECML/PKDD Conference, 2014.

_If you are interested in the time series motif and discord discovery, please check out our new tool called [GrammarViz 2.0](http://grammarviz2.github.io/grammarviz2_site/index.html) -- it is based on SAX and enables variable-length time series recurrent and anomalous patterns detection._

0.0 Javadocs
------------
[http://jmotif.github.io/SAX](http://jmotif.github.io/SAX)

1.0 Building
------------
The code is written in Java and I use maven to build it:
	
	$ mvn package -P single
	[INFO] Scanning for projects...
	[INFO] ------------------------------------------------------------------------
	[INFO] Building jmotif-sax
	[INFO]    task-segment: [package]
	...
	[INFO] Building jar: /media/Stock/git/jmotif-sax/target/jmotif-sax-0.1.1-SNAPSHOT-jar-with-dependencies.jar
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESSFUL

2.0 Time series to SAX conversion using CLI
------------
Built jar can be used to convert a time series (represented as a single-column text file) to SAX via sliding window in command line:

	$ java -jar target/jmotif-sax-0.1.1-SNAPSHOT-jar-with-dependencies.jar
	Command-line SAX conversion utility, the output printed to STDOUT 
	Expects 6 parameters:
 	[1] training dataset filename
 	[2] sliding window size
 	[3] PAA size
 	[4] Alphabet size
 	[5] numerosity reduction <NONE|EXACT|MINDIST>
 	[6] z-Normalization threshold value
 	[7] OPTIONAL: number of threads to use
	An execution example: $java -jar target/jmotif-sax-0.1.1-SNAPSHOT-jar-with-dependencies.jar  src/resources/test-data/ecg0606_1.csv 120 7 5 EXACT 0.001 2

When run, it prints the time series index and a corresponding word:

 	$ java -jar "target/jmotif-sax-0.1.1-SNAPSHOT-jar-with-dependencies.jar" src/resources/test-data/ecg0606_1.csv 120 7 5 EXACT 0.001 2 | head
 	0, aceccdc
 	4, adeccdc
 	6, addccdc
 	8, addccdd
 	9, adccccd
 	...

3.0 API usage
------------	
There two classes which implement sequential end-to-end workflow for SAX and a parallel implementation of the discretization. These are [TSProcessor](https://github.com/jMotif/SAX/blob/master/src/main/java/net/seninp/jmotif/sax/TSProcessor.java) and [SAXProcessor](https://github.com/jMotif/SAX/blob/master/src/main/java/net/seninp/jmotif/sax/SAXProcessor.java).

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
		System.out.println(idx + ", " + String.valueOf(res.getByIndex(idx).getPayload()));
	}

Parallel discretization:

	// instantiate needed classes
	NormalAlphabet na = new NormalAlphabet();
	SAXProcessor sp = new SAXProcessor();
  
	// read the input file
	double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);

	// perform the discretization using 8 threads
	ParallelSAXImplementation ps = new ParallelSAXImplementation();
	SAXRecords res = ps.process(ts, 8, slidingWindowSize, paaSize, alphabetSize, 
		nrStrategy, nThreshold);

	// print the output
	Set<Integer> index = res.getIndexes();
	for (Integer idx : index) {
		System.out.println(idx + ", " + String.valueOf(res.getByIndex(idx).getPayload()));
	}

The plot below shows the speedup achieved when using the parallelized SAX version on the dataset [`300_signal1.txt`](https://raw.githubusercontent.com/jMotif/SAX/master/src/resources/test-data/300_signal1.txt) of length 536,976 points. Parameters used in the experiment: sliding window size 200, PAA size 11, alphabet size 7, and three different NR strategies.

![Performance plot](https://raw.githubusercontent.com/jMotif/SAX/master/src/performance/profiling.png)
