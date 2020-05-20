package com.github.jt.project1.servlet;

import com.github.jt.project1.spark.SimpleAnalysis;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;


public class SimpleServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  DataSource ds;
  SimpleAnalysis analyze = new SimpleAnalysis("./Crimes2015.csv");

  public SimpleServlet() {
  }

  @Override
  public void init() throws ServletException {
    try {
      ds = (DataSource) InitialContext.doLookup("java:comp/env/jdbc/DS");
    } catch (NamingException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String query = req.getQueryString();
    if (query == null) {
      resp.getWriter().println("BLAAAAAARGH!");
      resp.getWriter().println(analyze.describe() + "\n");

      resp.getWriter().println("ASPECTS OF CRIME!");
      resp.getWriter().println(analyze.getHeader());
      // resp.getWriter().println(analyze.get2Count("Primary Type", "Arrest"));
    }
    /*
     * if (query != null) { if (query.contains("count")) { resp.getWriter().println("Count: " +
     * count); } } else { resp.getWriter().println(names); } { DataSource ds = (DataSource)
     * InitialContext.doLookup("java:comp/env/jdbc/DS"); Connection conn = ds.getConnection();
     * String productName = conn.getMetaData().getDatabaseProductName();
     * System.out.println(productName); resp.getWriter().println(productName); } catch
     * (NamingException|SQLException e) { e.printStackTrace(); }
     */
  }

}
