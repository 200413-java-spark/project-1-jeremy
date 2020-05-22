package com.github.jt.project1.servlet;

import com.github.jt.project1.spark.SimpleAnalysis;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;

public class SimpleServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  DataSource ds;
  SimpleAnalysis analyze = new SimpleAnalysis("./Crimes2015.csv");

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
      resp.getWriter().println(analyze.describe() + "\n");
      resp.getWriter().println("ASPECTS OF CRIME!");
      resp.getWriter().println(analyze.getHeader());
    }

  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String[] cat = req.getParameterValues("cat");
    String save = req.getParameter("save");

    if (cat != null) {
      if (cat.length == 2) {
        List<String> categories = Arrays.asList(cat);
        categories.sort(String::compareTo);
        LinkedHashMap<List<String>, Long> results = analyze.get2Count(cat[0], cat[1]);
        resp.getWriter().println("\n" + categories.get(0) + " x " + categories.get(1) + "!");
        results.forEach((k, v) -> {
          try {
            resp.getWriter().println(k + ": " + v);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      }

      if (cat.length == 1) {
        LinkedHashMap<String, Long> results = analyze.getCount(cat[0]);
        resp.getWriter().println("\n" + cat[0] + "!");
        results.forEach((k, v) -> {
          try {
            resp.getWriter().println(k + ": " + v);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      }
    }

    if (save != null) {
      if (save.equals("true")) {
        SimpleDb db = new SimpleDb(ds, analyze.counts, analyze.pairedCounts);
        try {
          int total = db.writeToDB();
          resp.getWriter().println(total + " records saved to db!");
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

  }

}
