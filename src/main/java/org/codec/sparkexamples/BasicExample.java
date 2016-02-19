package org.codec.sparkexamples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.biojava.nbio.structure.rcsb.GetRepresentatives;
import org.codec.filters.IdFilter;
import org.codec.mappers.PdbIdToBioJavaStruct;

public class BasicExample {

	private static int NUM_THREADS = 24;
	public static void main(String[] args )
	{

		// This is the default 2 line structure for Spark applications
		SparkConf conf = new SparkConf().setMaster("local[" + NUM_THREADS + "]")
				.setAppName(SparkRead.class.getSimpleName());
		// Set the config
		JavaSparkContext sc = new JavaSparkContext(conf);

		// Get all the PDB IDs
		SortedSet<String> thisSet = GetRepresentatives.getAll();
		List<String> pdbCodeList = new ArrayList<String>(thisSet);
		
		
		// Now read this list in
		JavaRDD<Integer> distData =
				sc.parallelize(pdbCodeList)
				.filter(new IdFilter())
				.mapToPair(new PdbIdToBioJavaStruct())
				.flatMapToPair(new StructToChains())
				.map(t -> t._2.getAtomLigands().size())
				.sample(true, 0.01)
				.cache();


		long min = distData.min(Comparator.naturalOrder());
		long max= distData.max(Comparator.naturalOrder());
		long total = distData.reduce((a,b) -> a + b);
		System.out.println("MIN: "+min +" MAX: "+ max +" TOTAL: "+ total);
		//
		sc.close();
	}
}


	
