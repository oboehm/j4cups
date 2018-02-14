[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=de.javatux%3Aj4cups&metric=coverage)](https://sonarcloud.io/dashboard?id=de.javatux%3Aj4cups)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.javatux/j4cups/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.javatux/j4cups)

# J4Cups

`j4cups` is a Java library for CUPS and IPP to provide data types defined in [RFC 2910](https://tools.ietf.org/html/rfc2910) and [RFC 8011](https://tools.ietf.org/html/rfc8011).
It is planned as add-on to [Cups4J](http://cups4j.org/).
Whereas Cups4J is good for printing with Java (client part) the focus of J4Cups is more general - to support the data types and operations of the underlying protocol.

The goal of this library is to provide the different puzzle parts to write applications based on the IPP protocol.
One of this application is to provide a Java service which can be accessed like a CUPS printer.

## More Infos

* Release Notes: [src/main/doc/RELEASES](src/main/doc/RELEASES.adoc)
* Javadoc: [http://javatux.de/j4cups/](http://javatux.de/j4cups/)

---
February 2018,
Oli B.
