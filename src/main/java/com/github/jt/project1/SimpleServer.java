package com.github.jt.project1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.descriptor.web.ContextResource;

public class SimpleServer {
  private static final Logger logger = LogManager.getLogger(SimpleServer.class);

  public static void main(String[] args) {
    // get db credentials from classpath
    try (InputStream input =
        SimpleServer.class.getClassLoader().getResourceAsStream("app.properties")) {
      Properties prop = new Properties(System.getProperties());
      prop.load(input);
      System.setProperties(prop);
    } catch (IOException ex) {
      logger.error("Properties file error ", ex);
    }

    Tomcat tomcat = new Tomcat();
    tomcat.enableNaming();
    tomcat.setPort(8888);
    tomcat.getConnector();

    String baseDirLocation = "target/tomcat/";
    tomcat.setBaseDir(new File(baseDirLocation).getAbsolutePath());

    String webappDirLocation = "src/main/webapp/";
    tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());

    // JNDI working!!
    ContextResource dataResource = new ContextResource();
    dataResource.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
    dataResource.setName(System.getProperty("db.name"));
    dataResource.setType(System.getProperty("db.type"));
    dataResource.setAuth("Container");
    dataResource.setProperty("driverClassName", System.getProperty("db.driver"));
    dataResource.setProperty("url", System.getProperty("db.url"));
    dataResource.setProperty("username", System.getProperty("db.user"));
    dataResource.setProperty("password", System.getProperty("db.password"));
    tomcat.getServer().getGlobalNamingResources().addResource(dataResource);

    try {
      tomcat.start();
      tomcat.getServer().await();
    } catch (LifecycleException e) {
      e.printStackTrace();
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          tomcat.stop();
        } catch (LifecycleException e) {
          e.printStackTrace();
        }
      }
    });
  }
}

