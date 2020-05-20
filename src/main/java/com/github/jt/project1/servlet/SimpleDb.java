package com.github.jt.project1.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class SimpleDb {
  private DataSource ds;
  private Map<String, LinkedHashMap<String, Long>> counts = new LinkedHashMap<>();
  private Map<List<String>, LinkedHashMap<List<String>, Long>> pairedCounts = new LinkedHashMap<>();

  public SimpleDb(DataSource ds, Map<String, LinkedHashMap<String, Long>> counts,
      Map<List<String>, LinkedHashMap<List<String>, Long>> pairedCounts) {
    this.ds = ds;
    this.counts = counts;
    this.pairedCounts = pairedCounts;
  }

  public void createTable(String categoryName) {
    String category = categoryName.replace(" ", "_");
    String dropSql = "DROP TABLE IF EXISTS " + category;
    String createSql = "CREATE TABLE IF NOT EXISTS " + category + " (" + category
        + "_category VARCHAR, " + category + "_count BIGINT)";
    try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement();) {
      conn.setAutoCommit(false);
      stmt.execute(dropSql);
      stmt.execute(createSql);
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // assuming only 2 categories compared
  public void createPairedTable(List<String> categoryNames) {
    categoryNames.forEach(s -> s.replace(" ", "_"));
    categoryNames.sort(String::compareTo);
    String tableName = categoryNames.get(0) + "_" + categoryNames.get(1);
    String dropSql = "DROP TABLE IF EXISTS " + tableName;
    String createSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableName
        + "_category VARCHAR, " + tableName + "_count BIGINT)";
    try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement();) {
      conn.setAutoCommit(false);
      stmt.execute(dropSql);
      stmt.execute(createSql);
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void writeCountToTable(String categoryName, LinkedHashMap<String, Long> categoryCounts) {
    String category = categoryName.replace(" ", "_");
    String prepareSql = "INSERT INTO " + category + " (" + category + "_category, " + category
        + "_count) VALUES (?,?)";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(prepareSql);) {
      conn.setAutoCommit(false);

      categoryCounts.forEach((key, value) -> {
        String catType = key.replace(" ", "_");
        Long catCount = value;

        // WHY DO I NEED THIS?
        try {
          ps.setString(1, catType);
          ps.setLong(2, catCount);
          ps.addBatch();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      });
      int n[] = ps.executeBatch();
      conn.commit();
      Integer total = Arrays.stream(n).sum();
      System.out.println(total + " records inserted into db @ " + System.getProperty("db.url"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void writePairedCountToTable(List<String> categoryNames,
      LinkedHashMap<List<String>, Long> categoryPairedCounts) {
    categoryNames.forEach(s -> s.replace(" ", "_"));
    categoryNames.sort(String::compareTo);
    String tableName = categoryNames.get(0) + "_" + categoryNames.get(1);
    String prepareSql = "INSERT INTO " + tableName + " (" + tableName + "_tableName, " + tableName
        + "_COUNT) VALUES (?,?)";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(prepareSql);) {
      conn.setAutoCommit(false);

      categoryPairedCounts.forEach((key, value) -> {
        key.forEach(s -> s.replace(" ", "_"));
        key.sort(String::compareTo);
        String catType = key.get(0) + "_" + categoryNames.get(1);
        Long catCount = value;

        // WHY DO I NEED THIS?
        try {
          ps.setString(1, catType);
          ps.setLong(2, catCount);
          ps.addBatch();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      });
      int n[] = ps.executeBatch();
      conn.commit();
      Integer total = Arrays.stream(n).sum();
      System.out.println(total + " records inserted into db @ " + System.getProperty("db.url"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void writeAllToDB() throws SQLException {
    counts.forEach((k, v) -> {
      createTable(k);
      writeCountToTable(k, v);
    });
    pairedCounts.forEach((k, v) -> {
      createPairedTable(k);
      writePairedCountToTable(k, v);
    });
  }
}
