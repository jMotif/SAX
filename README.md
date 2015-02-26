Time series discretization with SAX
====

This Java library implements Symbolic Aggregate Approximation for time series. The code and API are very simple aiming at performance; the library provides a multi-threaded SAX discretization too. 

You can find more information about SAX on [my old wiki page](https://code.google.com/p/jmotif/wiki/SAX) or in following papers:

[1] Lin, J., Keogh, E., Patel, P., and Lonardi, S., [*Finding Motifs in Time Series*](http://cs.gmu.edu/~jessica/Lin_motif.pdf), The 2nd Workshop onTemporal Data Mining, the 8th ACM Int'l Conferenceon KDD (2002)

[2] Patel, P., Keogh, E., Lin, J., Lonardi, S., [*Mining Motifs in Massive Time Series Databases*](http://www.cs.gmu.edu/~jessica/publications/motif_icdm02.pdf), In Proc. ICDM (2002)

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

2.0 CLI time series conversion
------------
Built jar can be used to convert a time series (represented as a single-column text file) to SAX via sliding window in command line:

	$ java -jar "jmotif-sax20.jar"
	Command-line SAX converson utility, the output printed to STDOUT 
	Expects 6 parameters:
 	[1] training dataset filename
 	[2] sliding window size
 	[3] PAA size
 	[4] Alphabet size
 	[5] numerosity reduction <NONE|EXACT|MINDIST>
 	[6] z-Normalization threshold value
 	[7] OPTIONAL: number of threads to use
	An execution example: $java -jar "jmotif-vsm-20.jar"  test/data/ecg0606_1.csv 120 7 5 EXACT 0.001 2

When run, it prints the time series index and a corresponding word:

 	$ java -jar "jmotif-sax20.jar"  test/data/ecg0606_1.csv 120 7 5 EXACT 0.001 3 | head
 	0, aceccdc
 	4, adeccdc
 	6, addccdc
 	8, addccdd
 	9, adccccd
 	...

3.0 API USAGE
------------	
There two classes which implement sequential end-to-end workflow for SAX and a parallel implementation of the discretization. These are [TSProcessor](https://github.com/jMotif/SAX/blob/master/src/edu/hawaii/jmotif/sax/TSProcessor.java) and [SAXProcessor](https://github.com/jMotif/SAX/blob/master/src/edu/hawaii/jmotif/sax/SAXProcessor.java).

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

The plot below shows the speedup achieved when using the parallelized SAX version on the dataset [`300_signal1.txt`](https://raw.githubusercontent.com/jMotif/SAX/master/test/data/300_signal1.txt) of length 536,976 points. Parameters used in the experiment: sliding window size 200, PAA size 11, alphabet size 7, and three different NR strategies.

![Performance plot](https://raw.githubusercontent.com/jMotif/SAX/master/test/performance/profiling.png)
