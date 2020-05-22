package com.github.jt.project1;

import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;
import org.apache.catalina.webresources.DirResourceSet;
import java.util.Properties;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import hthurow.tomcatjndi.TomcatJNDI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class DbTest {

  private static final Logger logger = LogManager.getLogger(DbTest.class);
  private Connection conn;
  List<String> categoryNames = Arrays.asList("Arrest", "Primary Type");

  @Before
  @Test
  public void iniatePropAndDb() {
    try (InputStream props = this.getClass().getResourceAsStream("/rds.properties")) {
      Properties prop = new Properties(System.getProperties());
      prop.load(props);
      System.setProperties(prop);

      String url = System.getProperty("db.url");
      String user = System.getProperty("db.user");
      String password = System.getProperty("db.password");

      conn = DriverManager.getConnection(url, user, password);
      assertNotNull(conn);
    } catch (SQLException | IOException e) {
      logger.error("Some error", e);
    }
  }

  @Test
  public void postgresShouldConnect() {
    try {
      String productName = conn.getMetaData().getDatabaseProductName();
      logger.log(Level.valueOf("DEBUG"), productName);
      assertNotNull(productName);
    } catch (SQLException e) {
      logger.error("Postgres still can't connect!", e);
    }
  }

  @Test
  public void shouldConvertSpaces() {
    categoryNames.forEach(s -> s.replace(" ", "_"));
    String newName = categoryNames.stream().map(s -> s.replace(" ","_")).collect(Collectors.joining("_"));
    //String tableName = categoryNames.get(0) + "_" + categoryNames.get(1);
    System.out.println(newName);
    assertEquals(newName, "Arrest_Primary_Type");
  }

  /*
   * @Test public void tomcatShouldInitiate() { TomcatJNDI tomcatJNDI = new TomcatJNDI();
   * tomcatJNDI.start(); try { /* tomcatJNDI.processContextXml(new File("context.xml"));
   * tomcatJNDI.processWebXml(new File("web.xml")); // DataSource ds =
   * InitialContext.doLookup("java:comp/env/jdbc/ds"); System.out.println(System.getenv());
   * assertNotNull(ds); } catch (NamingException e) { logger.error("ERRORS", e); }
   * tomcatJNDI.tearDown(); }
   */

  /*
   * @Test public void tomcatShouldSetUpJNDI() { Tomcat tomcat = new Tomcat(); tomcat.setPort(8888);
   * tomcat.getConnector(); tomcat.enableNaming();
   * 
   * String webappDirLocation = "src/test/webapp/"; StandardContext ctx = (StandardContext)
   * tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
   * logger.log(Level.valueOf("DEBUG"), "configuring app with basedir: " + new File("./" +
   * webappDirLocation).getAbsolutePath());
   * 
   * ContextResource dataResource = new ContextResource(); dataResource.setProperty("factory",
   * "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
   * dataResource.setName(System.getProperty("db.name"));
   * dataResource.setType(System.getProperty("db.type")); dataResource.setAuth("Container"); //
   * dataResource.setProperty("driverClassName", System.getProperty("db.driver"));
   * dataResource.setProperty("url", System.getProperty("db.url"));
   * dataResource.setProperty("username", System.getProperty("db.user"));
   * dataResource.setProperty("password", System.getProperty("db.password"));
   * tomcat.getServer().getGlobalNamingResources().addResource(dataResource);
   * 
   * // Declare an alternative location for your "WEB-INF/classes" dir // Servlet 3.0 annotation
   * will work File additionWebInfClasses = new File("target/dependency"); WebResourceRoot resources
   * = new StandardRoot(ctx); resources.addPreResources(new DirResourceSet(resources,
   * "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
   * 
   * ctx.setResources(resources);
   * 
   * try { tomcat.start(); // tomcat.getServer().await(); DataSource ds =
   * InitialContext.doLookup("java:comp/env/jdbc/ds"); tomcat.stop(); } catch (Exception e) {
   * e.printStackTrace();
   * logger.error(Arrays.asList(e.getStackTrace()).stream().map(Object::toString)
   * .collect(Collectors.joining(", "))); } }
   */

}
