package com.github.jt.project1.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class SimpleContext {
  private JavaSparkContext sc;
  private static SimpleContext instance;

  private SimpleContext() {
  }

  private void confContext() {
    if (sc == null) {
      SparkConf conf = new SparkConf().setAppName("Simple Spark App").setMaster("local[*]");
      sc = new JavaSparkContext(conf);
      sc.setLogLevel("WARN");
    }
  }

  public static SimpleContext getInstance() {
    if (instance == null) {
      instance = new SimpleContext();
      instance.confContext();
    }
    return instance;
  }

  public JavaSparkContext getContext() {
    return sc;
  }
}
