# gmsj-cli

This is a minimal CLI front-end for the [`google-maps-services-java`](https://github.com/googlemaps/google-maps-services-java) library. I wrote it to use while developing and debugging `google-maps-services-java` itself. It's not intended to be actually useful, or for use in any production scenario.

**THIS PROGRAM IS COMPLETELY UNSUPPORTED.**

## Requirements

* Unix
* google-maps-services-java
* Java 1.7
* A Google Maps API key
* Maven
* Other Java libraries, as specified in `pom.xml`
** This includes all Java libraries required by `google-maps-services-java`, and maybe some more required just by `gmsj-cli`.

In particular, this program will not work on Windows. It's written for use on Unix-like systems, and macOS in particular.

You must set the environment variable `GOOGLE_MAPS_API_KEY` to contain your Google Maps API key before running `gmsj-cli`. That's how it picks up your credentials.

This is not an end-user application, so no distinction is made between build-time and run-time requirements.

## Usage Scenarios

`gmsj-cli` is intended to exercise certain API calls on google-maps-services-java to make it easier to reproduce and debug its behavior. The main use case is making concise, reproducible bug reports to the main `google-maps-services-java` repository.

This project assumes that you are also building `google-maps-services-java` and are publishing it to a local Maven repository. If it complains about a missing `google-maps-services-java` library, `cd` over to your local working copy of it, and do a `./gradlew install` and see if that fixes things.

## License

`gmsj-cli` is licensed under the Apache 2.0 license, which is the same license that `google-maps-services-java` uses. If you can use `google-maps-services-java` in your scenario, you can use `gmsj-cli`.

## Versioning

The version of `gmsj-cli` tracks the version of `google-maps-services-java` that it exercises. Development only happens on the head of `master`; any previous "releases" are of historical interest only, and are unmaintained.

## Support and Contributing

This is a one-off side project, and no particular level of support is guaranteed or even suggested. But if you find this project useful, I welcome bug reports and feature requests.

## Interface

The idea is that `gmsj-cli` will have several subcommands which exercise the various parts of `google-maps-services-java`. As of now, the only thing implemented is a trivial `geocode` subcommand, which exercises the Geocoding API.

## Example Usage


###  Build the application first!

(This is a prerequisite for all the other usages.)

First, go to your `google-maps-services-java` working copy, and do this to build and install it.

```
./gradlew install
```

Then, go to your `gmsj-cli` working copy, and do this.

```
mvn clean
mvn package
```

Then you can run the `./bin/gmsj-cli` command from that working copy.

Each time you change the `google-maps-services-java` library, you must re-do all of these steps, in order!

####  Geocode a human-supplied location

```
./bin/gmsj-cli geocode "200 5th Ave, New York, NY"
./bin/gmsj-cli geocode "Sears Tower"
```
