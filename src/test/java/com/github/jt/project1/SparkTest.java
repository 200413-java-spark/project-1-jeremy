package com.github.jt.project1;

import com.github.jt.project1.spark.SimpleContext;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;


public class SparkTest {

  private static final Logger logger = LogManager.getLogger(SparkTest.class);
  JavaSparkContext sc;

  @Before
  @Test
  public void ctxShouldInit() {
    sc = SimpleContext.getInstance().getContext();
    assertNotNull(sc);
  }

  private static JavaPairRDD<List<String>, Map<String, String>> pairBy2(
      JavaRDD<Map<String, String>> rdd, String key1, String key2) {
    return rdd.mapToPair(m -> new Tuple2<>(Arrays.asList(m.get(key1), m.get(key2)),
        m.entrySet().stream().filter(n -> !(n.getKey().equals(key1) || n.getKey().equals(key2)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
  }

  private static LinkedHashMap<List<String>, Long> sort2MapByValue(Map<List<String>, Long> counts) {
    return counts.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
            LinkedHashMap::new));
  }

  @Test
  public void shouldSort() {
    List<String> header = Arrays.asList("blah", "boo", "foor", "bar");
    List<String> line1 = Arrays.asList("1", "true", "2", "4");
    List<String> line2 = Arrays.asList("1", "true", "2", "4");
    List<String> line3 = Arrays.asList("1", "false", "2", "4");
    List<String> line4 = Arrays.asList("2", "false", "2", "4");
    List<String> line5 = Arrays.asList("2", "false", "2", "4");
    List<List<String>> lines = Arrays.asList(line1, line2, line3, line4, line5);
    JavaRDD<List<String>> rdd = sc.parallelize(lines);

    JavaRDD<Map<String, String>> maps = rdd.map(line -> {
      LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
      for (int i = 0; i < line.size(); i++) {
        newMap.put(header.get(i), line.get(i));
      }
      return newMap;
    });

    maps.take(5).forEach(System.out::println);

    JavaPairRDD<List<String>, Map<String, String>> crimesBy2Cat = pairBy2(maps, "blah", "boo");
    Map<List<String>, Long> countsBy2Cat = crimesBy2Cat.countByKey();
    LinkedHashMap<List<String>, Long> sorted2Counts = sort2MapByValue(countsBy2Cat);

    System.out.println("\n" + "blah" + " x " + "boo" + "!");
    sorted2Counts.forEach((k, v) -> System.out.println(k + ": " + v));

    assertNotNull(sorted2Counts);
  }

}
