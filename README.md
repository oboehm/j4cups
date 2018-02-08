[![Build Status](https://travis-ci.org/oboehm/j4cups.svg?branch=master)](https://travis-ci.org/oboehm/j4cups)
[![Coverage Status](https://coveralls.io/repos/github/oboehm/j4cups/badge.svg?branch=master)](https://coveralls.io/github/oboehm/j4cups)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# J4Cups

`j4cups` is a Java library for CUPS and IPP to provide data types defined in [RFC 2910](https://tools.ietf.org/html/rfc2910) and [RFC 8011](https://tools.ietf.org/html/rfc8011).
It is planned as add-on to [Cups4J](http://cups4j.org/) and requested for deployment to the Central Repository with [OSSRH-37762](https://issues.sonatype.org/browse/OSSRH-37762).
Whereas Cups4J is good for printing with Java (client part) the focus of J4Cups is more general - to support the data types and operations of the underlying protocol.

The goal of this library is to provide the different puzzle parts to write applications based on the IPP protocol.
One of this application is to provide a Java service which can be accessed like a CUPS printer.

---
February 2018,
Oli B.
