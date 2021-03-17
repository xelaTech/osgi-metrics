# OSGi Metrics App

![CI Build](https://github.com/xelaTech/osgi-metrics/workflows/CI%20Build/badge.svg)

OSGi bundles providing OS, JVM and application level metrics.

# Modules

* esa.egscc.metrics.api - Metrics interface (striped out from org.eclipse.microprofile.metrics)
* esa.egscc.metrics.impl - Implementation bundle
* esa.egscc.metrics.adapter - HTTP end point adapter providing pull and push strategies
* esa.egscc.metrics.app - Example application

# Build & Run from Shell

./gradlew clean

./gradlew build

java -jar esa.egscc.metrics.app/generated/metrics-app.bndrun.jar

The application is providing Gogo shell. To see the metrics go to browser and open [localhost:8080/metrics]

# Import to Eclipse

Download Eclipse IDE and install Bndtools plugin. Start Eclipse and before importing the project set the JRE/JDK to comply with Java version 1.8. Next import the project using Git importer.
