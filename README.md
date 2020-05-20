# Project 1

## A simple job for Spark

This project presents a simple analysis of crimes in Chicago in 2015 using Apache Spark.  Original data set can be found at <https://data.cityofchicago.org/Public-Safety/Crimes-2015/vwwp-7yr9>.  It contains 264,384 rows and 22 columns.

### Build

>mvn clean package

### Run

Spark Http Servlet (and Tomcat Server)
>java -cp target/project1.jar com.github.jt.project1.SimpleServer
