# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).



## [Planned]

* use javax.print for implementation


## [Unreleased]

### Changed

* beginning migration to Kotlin


## [0.6.0] - 2020-09-03

### Added

* CupsServer can act as proxy
* get-printers request is supported
* get-default opertation is supported
* all IPP requests are validated now
* replay feature in CupsClient
* HTTP support

### Changed

* configuration is internally handled by a Config object


## [0.5.0] - 2018-06-03

### Added

* op package with IPP operations
* server part provided which can act like a CUPS server or a proxy



## [0.3.0] - 2018-03-24

### Added

* IPP response to Get-Jobs and Get-Printer-Attributes request implemented

### Changed

* request and response are now Serializable.



## [0.2.1] - 2018-02-22

### Changed

* IPP response are pre-filled with attributes from IPP request



## [0.2.0] - 2018-02-18

### Added

* IPP response to Print-Job request implemented



## [0.1.0] - 2018-02-14

### Added

* successful reading of IPP requests,
* logging introduced (SLF4J)



## [0.0.2] - 2018-02-11

### Added

* tags and attributes added
* successful parsing of IPP request



## [0.0.1] - 2018-02-09

### Added

* created with https://issues.sonatype.org/browse/OSSRH-37762[OSSRH-37762]
  and protocol package (IppOperations, IppRequest and IppResponse)
