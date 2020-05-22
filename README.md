# Project 1

## A simple job for Spark

This project presents a simple analysis of crimes in Chicago in 2015 using Apache Spark.
Original data set can be found at <https://data.cityofchicago.org/Public-Safety/Crimes-2015/vwwp-7yr9>.
It contains 264,384 rows and 22 columns.

### Build

```bash
mvn clean package
```

### Run

Spark Http Servlet (and Tomcat Server)

```bash
~$ java -cp target/project1.jar com.github.jt.project1.SimpleServer
```

#### HTTP methods

##### GET

```bash
~$ curl http://localhost:8888/spark
Data set dimensions: 22 columns x 264375 rows

ASPECTS OF CRIME!
ID, Case Number, Date, Block, IUCR, Primary Type, Description, Location Description,
Arrest, Domestic, Beat, District, Ward, Community Area, FBI Code, X Coordinate,
Y Coordinate, Year, Updated On, Latitude, Longitude, Location
```

##### POST

Use up to two parameter values of type "cat" to get a list of crime in descending
order by counts and grouped by the categories requested.  Set "save" to true to write to
PostgresQL.

```bash
~$ curl -d "cat=Primary Type&cat=Location Description&save=true" -X POST http://192.168.1.100:8888/spark

Location Description x Primary Type!
[STREET, THEFT]: 14451
[APARTMENT, BATTERY]: 12291
[STREET, CRIMINAL DAMAGE]: 10488
[RESIDENCE, BATTERY]: 9237
[SIDEWALK, BATTERY]: 7867
...
1506 records saved to db!
```
