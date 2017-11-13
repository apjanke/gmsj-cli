# gmsj-cli

This is a minimal CLI front-end for the [`google-maps-services-java`](https://github.com/googlemaps/google-maps-services-java) library. I wrote it to use for developing and debugging google-maps-services-java itself. It's not intended to be actually useful, or for use in any production scenario.

THIS PROGRAM IS COMPLETELY UNSUPPORTED.

## Requirements

* Unix
* google-maps-services-java
* Java 1.7
* A Google Maps API key (maybe)

In particular, this program will not work on Windows. It's written for use on Unix-like systems, and macOS in particular.

gmsj-cli is written to work without a Google Maps API key in the general case, as long as you're doing low-volume testing.

## Usage Scenarios

gmsj-cli is intended to exercise certain API calls on google-maps-services-java to make it easier to reproduce and debug its behavior. The main use case is making concise, reproducible bug reports to the main google-maps-services-java repository.

## License

gmsj-cli is licensed under the Apache 2.0 license, which is the same license that google-maps-services-java uses. If you can use google-maps-services-java in your scenario, you can use gmsj-cli.

## Versioning

The version of gmsj-cli tracks the version of google-maps-services-java that it exercises. Development only happens on the head of `master`; any previous "releases" are of historical interest only, and are unmaintained.

## Support and Contributing

This is a one-off side project, and no particular level of support is guaranteed or even suggested. But if you find this project useful, I welcome bug reports and feature requests.