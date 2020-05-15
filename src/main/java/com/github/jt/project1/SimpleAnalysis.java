package com.github.jt.project1;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

public class SimpleAnalysis {

  private static JavaPairRDD<String, Map<String, String>> pairBy(JavaRDD<Map<String, String>> rdd,
      String key) {
    return rdd.mapToPair(
        m -> new Tuple2<>(m.get(key), m.entrySet().stream().filter(n -> !n.getKey().equals(key))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
  }

  private static LinkedHashMap<String, Long> sortMapByValue(Map<String, Long> counts) {
    return counts.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
            LinkedHashMap::new));
  }

  public static void main(String[] args) {
    SparkConf conf = new SparkConf().setAppName("Simple Spark App").setMaster("local[*]");
    JavaSparkContext sc = new JavaSparkContext(conf);
    sc.setLogLevel("WARN");

    JavaRDD<String> lines = sc.textFile(args[0]);

    // split csv into entries
    JavaRDD<List<String>> items =
        lines.map(x -> Arrays.stream(x.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1))
            .map(String::trim).collect(Collectors.toList()));

    // extract header
    List<String> header = items.first();

    System.out
        .println("Data set dimensions: " + header.size() + " columns x " + lines.count() + " rows");

    // remove header from data
    JavaRDD<List<String>> data = items.filter(x -> !x.get(0).equals(header.get(0)));

    JavaRDD<Map<String, String>> maps = data.map(line -> {
      LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
      for (int i = 0; i < line.size(); i++) {
        newMap.put(header.get(i), line.get(i));
      }
      return newMap;
    });

    // count and sort
    System.out.println("Rankings across select categories");

    String[] categories = {"Primary Type", "Location Description", "Arrest", "Domestic"};
    Map<String, LinkedHashMap<String, Long>> counts = new LinkedHashMap<>();
    for (String category : categories) {
      JavaPairRDD<String, Map<String, String>> crimesByCat = pairBy(maps, category);
      Map<String, Long> countsByCat = crimesByCat.countByKey();
      LinkedHashMap<String, Long> sortedCounts = sortMapByValue(countsByCat);
      counts.put(category, sortedCounts);

      System.out.println("\n" + category + "!");
      sortedCounts.forEach((k,v) -> System.out.println(k + ": " + v));
    }
    sc.stop();

    // write to db
    SimpleDb db = new SimpleDb(counts);
    db.writeToDb();
    
    //RUNS OUT OF MEMORY EVEN AFTER CLOSING SPARK
    //SimpleServer simpleserver = new SimpleServer();
    //simpleserver.init();

  }
}
