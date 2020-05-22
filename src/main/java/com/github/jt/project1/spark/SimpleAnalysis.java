package com.github.jt.project1.spark;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

public class SimpleAnalysis {
  private JavaSparkContext sc;
  public String stringified; // header = new ArrayList<>();
  public long[] dimensions = {0, 0};
  public JavaRDD<Map<String, String>> maps;

  public Map<String, LinkedHashMap<String, Long>> counts = new LinkedHashMap<>();
  public Map<List<String>, LinkedHashMap<List<String>, Long>> pairedCounts = new LinkedHashMap<>();

  private static final Logger logger = LogManager.getLogger(SimpleAnalysis.class);

  public SimpleAnalysis(String csvfile) {
    try {
      sc = SimpleContext.getInstance().getContext();
      JavaRDD<String> lines = sc.textFile(csvfile);

      // split csv into entries
      JavaRDD<List<String>> items =
          lines.map(x -> Arrays.stream(x.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1))
              .map(String::trim).collect(Collectors.toList()));

      // extract header
      List<String> header = items.first();
      stringified = header.stream().collect(Collectors.joining(", "));


      // remove header from data
      JavaRDD<List<String>> data = items.filter(x -> !x.get(0).equals(header.get(0)));

      // map each column to corrensponding row entry
      maps = data.map(line -> {
        LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
        for (int i = 0; i < line.size(); i++) {
          newMap.put(header.get(i), line.get(i));
        }
        return newMap;
      });
      dimensions[0] = (long) header.size();
      dimensions[1] = maps.count();
    } catch (Exception e) {
      logger.error("ERRORS", e);
    }
  }

  public String getHeader() {
    return stringified;
  }

  public String describe() {
    return "Data set dimensions: " + dimensions[0] + " columns x " + dimensions[1] + " rows";
  }

  private static JavaPairRDD<String, Map<String, String>> pairBy(JavaRDD<Map<String, String>> rdd,
      String key) {
    return rdd.mapToPair(
        m -> new Tuple2<>(m.get(key), m.entrySet().stream().filter(n -> !n.getKey().equals(key))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
  }

  private static JavaPairRDD<List<String>, Map<String, String>> pairBy2(
      JavaRDD<Map<String, String>> rdd, String key1, String key2) {
    return rdd.mapToPair(m -> new Tuple2<>(Arrays.asList(m.get(key1), m.get(key2)),
        m.entrySet().stream().filter(n -> !(n.getKey().equals(key1) || n.getKey().equals(key2)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
  }

  private static LinkedHashMap<String, Long> sortMapByValue(Map<String, Long> counts) {
    return counts.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
            LinkedHashMap::new));
  }

  private static LinkedHashMap<List<String>, Long> sort2MapByValue(Map<List<String>, Long> counts) {
    return counts.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
            LinkedHashMap::new));
  }

  public LinkedHashMap<String, Long> getCount(String category) {
    JavaPairRDD<String, Map<String, String>> crimesByCat = pairBy(maps, category);
    Map<String, Long> countsByCat = crimesByCat.countByKey();
    LinkedHashMap<String, Long> sortedCounts = sortMapByValue(countsByCat);
    counts.put(category, sortedCounts);
    return sortedCounts;
  }

  public LinkedHashMap<List<String>, Long> get2Count(String category1, String category2) {
    // sort category names before putting them into map as a pair
    List<String> categories = Arrays.asList(category1, category2);
    categories.sort(String::compareTo);
    JavaPairRDD<List<String>, Map<String, String>> crimesBy2Cat =
        pairBy2(maps, categories.get(0), categories.get(1));
    Map<List<String>, Long> countsBy2Cat = crimesBy2Cat.countByKey();
    LinkedHashMap<List<String>, Long> sorted2Counts = sort2MapByValue(countsBy2Cat);
    pairedCounts.put(categories, sorted2Counts);
    return sorted2Counts;
  }
}
