package com.github.jt.project1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class SimpleDb {

  // data source factory
  private static class SimpleDS {
    private static SimpleDS instance;
    private String url;
    private String user;
    private String password;

    private SimpleDS() {
      try (InputStream props = this.getClass().getResourceAsStream("/app.properties")) {
        Properties prop = new Properties(System.getProperties());
        prop.load(props);
        System.setProperties(prop);

        url = System.getProperty("db.url");
        user = System.getProperty("db.user");
        password = System.getProperty("db.password");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    protected static SimpleDS getInstance() {
      if (instance == null) {
        instance = new SimpleDS();
      }
      return instance;
    }

    protected Connection getConnection() {
      try {
        return DriverManager.getConnection(url, user, password);
      } catch (SQLException e) {
        throw new RuntimeException("Error connecting to db ", e);
      }
    }
  }

  private SimpleDS ds = SimpleDS.getInstance();
  private Map<String, LinkedHashMap<String, Long>> data;

  public SimpleDb(Map<String, LinkedHashMap<String, Long>> data) {
    this.data = data;
  }

  public void writeToDb() {
    data.forEach((k, v) -> {
      String category = k.replace(" ", "_");
      String dropSql = "DROP TABLE IF EXISTS " + category;
      String createSql = "CREATE TABLE IF NOT EXISTS " + category + " (" + category
          + "_CATEGORY VARCHAR, " + category + "_COUNT BIGINT)";
      try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
        conn.setAutoCommit(false);
        stmt.execute(dropSql);
        stmt.execute(createSql);
        conn.commit();
        String prepareSql = "INSERT INTO " + category + " (" + category + "_CATEGORY, " + category
            + "_COUNT) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(prepareSql)) {
          v.forEach((key, value) -> {
            String descriptor = key.replace(" ", "_");
            Long count = value;

            // WHY TRY WITH RESOURCES NOT WORKING?
            try {
              ps.setString(1, descriptor);
              ps.setLong(2, count);
              ps.addBatch();
            } catch (SQLException e) {
              e.printStackTrace();
            }
          });
          int n[] = ps.executeBatch();
          conn.commit();
          Integer total = Arrays.stream(n).sum();
          System.out.println(total + " records inserted into db @ " + System.getProperty("db.url"));
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    });
  }
}
