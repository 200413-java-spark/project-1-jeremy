package com.github.jt.project1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.descriptor.web.ContextResource;

public class SimpleServer {
  private static final Logger logger = LogManager.getLogger(SimpleServer.class);
    
  public static void main(String[] args) {
    // get db credentials from classpath
    try (InputStream input = SimpleServer.class.getClassLoader().getResourceAsStream("app.properties")) {
      Properties prop = new Properties(System.getProperties());
      prop.load(input);
      System.setProperties(prop);
    } catch (IOException ex) {
      logger.error("Properties file error ", ex);
    }

    // create db service
    //NoteSQL db = new NoteSQL(NoteDataSource.getInstance());
  /*}

  public void init() {*/
    Tomcat tomcat = new Tomcat();
    tomcat.setPort(8888);
    tomcat.getConnector();
    tomcat.enableNaming();

    String webappDirLocation = "src/main/webapp/";
    StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
    System.out.println("configuring app with basedir: " + new File("./" + webappDirLocation).getAbsolutePath());

    // STUFF TO GET JNDI RUNNING BUT IT'S NOT WORKING
    /*ContextResource dataResource = new ContextResource();
    dataResource.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
    dataResource.setName(System.getProperty("db.name"));
    dataResource.setType(System.getProperty("db.type"));
    dataResource.setAuth("Container");
    dataResource.setProperty("driverClassName", System.getProperty("db.driver"));
    dataResource.setProperty("url", System.getProperty("db.url"));
    dataResource.setProperty("username", System.getProperty("db.user"));
    dataResource.setProperty("password", System.getProperty("db.password"));
    tomcat.getServer().getGlobalNamingResources().addResource(dataResource);

    // Declare an alternative location for your "WEB-INF/classes" dir
    // Servlet 3.0 annotation will work
    File additionWebInfClasses = new File("target/dependency");
    WebResourceRoot resources = new StandardRoot(ctx);
    resources.addPreResources(
        new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));

    ctx.setResources(resources);*/

    try {
      tomcat.start();
      tomcat.getServer().await();
    } catch (LifecycleException e) {
      e.printStackTrace();
    }
  }
}