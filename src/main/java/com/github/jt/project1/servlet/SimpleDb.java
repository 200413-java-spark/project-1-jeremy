package com.github.jt.project1.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

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
    String tableName =
        categoryNames.stream().map(s -> s.replace(" ", "_")).collect(Collectors.joining("_"));
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

  public Integer writeCountToTable(String categoryName,
      LinkedHashMap<String, Long> categoryCounts) {
    Integer total = 0;
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
      total = Arrays.stream(n).sum();
      System.out.println(total + " records inserted into db @ " + System.getProperty("db.url"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return total;
  }

  public Integer writePairedCountToTable(List<String> categoryNames,
      LinkedHashMap<List<String>, Long> categoryPairedCounts) {
    Integer total = 0;
    String tableName =
        categoryNames.stream().map(s -> s.replace(" ", "_")).collect(Collectors.joining("_"));
    String prepareSql = "INSERT INTO " + tableName + " (" + tableName + "_category, " + tableName
        + "_COUNT) VALUES (?,?)";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(prepareSql);) {
      conn.setAutoCommit(false);

      categoryPairedCounts.forEach((key, value) -> {
        String catType =
            key.stream().map(s -> s.replace(" ", "_")).collect(Collectors.joining("_"));
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
      total = Arrays.stream(n).sum();
      System.out.println(total + " records inserted into db @ " + System.getProperty("db.url"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return total;
  }

  public Integer writeToDB() throws SQLException {
    int[] total = {0};
    if (!counts.isEmpty())
      counts.forEach((k, v) -> {
        createTable(k);
        total[0] += writeCountToTable(k, v).intValue();
      });
    if (!pairedCounts.isEmpty())
      pairedCounts.forEach((k, v) -> {
        createPairedTable(k);
        total[0] += writePairedCountToTable(k, v).intValue();
      });
    return total[0];
  }
}

