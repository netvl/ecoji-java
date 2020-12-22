# Ecoji [![Maven][maven]](https://search.maven.org/artifact/io.github.netvl.ecoji/ecoji) [![Build][actions]](https://github.com/netvl/ecoji-java/actions?query=workflow%3ACI) [![Javadocs][docs]](https://www.javadoc.io/doc/io.github.netvl.ecoji/ecoji/latest/index.html)

  [maven]: https://img.shields.io/maven-central/v/io.github.netvl.ecoji/ecoji?style=flat-square
  [actions]: https://img.shields.io/github/workflow/status/netvl/ecoji-java/CI/master?style=flat-square
  [docs]: https://img.shields.io/badge/documentation-javadoc-green.svg?style=flat-square

A Java implementation of the [Ecoji](https://github.com/keith-turner/ecoji) encoding standard.

Provides an API for encoding and decoding data as a base-1024 sequence of emojis.

Visit [ecoji.io](https://ecoji.io) to try Ecoji in your browser.

## Usage

This library is published to Maven Central, so you can add it to your dependencies configuration in your build tool of choice.

Maven:

```xml
<dependency>
    <groupId>io.github.netvl.ecoji</groupId>
    <artifactId>ecoji</artifactId>
    <version>${versions.ecoji}</version>
</dependency>
```

Gradle:

```groovy
implementation group: 'io.github.netvl.ecoji', name: 'ecoji', version: versions.ecoji
```

SBT:

```scala
libraryDependencies += "io.github.netvl.ecoji" % "ecoji" % versions.ecoji
```

Use the latest available version, which you can find in [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.netvl.ecoji%22%20a%3A%22ecoji%22) or on the badge at the top of this file.

Afterwards, import the [`io.github.netvl.ecoji.Ecoji`](https://netvl.github.io/ecoji-java/api/io/github/netvl/ecoji/Ecoji.html) class and use its [`getEncoder()`](https://netvl.github.io/ecoji-java/api/io/github/netvl/ecoji/Ecoji.html#getEncoder--) and [`getDecoder()`](https://netvl.github.io/ecoji-java/api/io/github/netvl/ecoji/Ecoji.html#getDecoder--) static methods to obtain instances of the [`Ecoji.Encoder`](https://netvl.github.io/ecoji-java/api/io/github/netvl/ecoji/Ecoji.Encoder.html) and [`Ecoji.Decoder`](https://netvl.github.io/ecoji-java/api/io/github/netvl/ecoji/Ecoji.Decoder.html) classes. These can then be used to describe encoding/decoding sources and targets. For example:

```java
import io.github.netvl.ecoji.Ecoji;

String encoded = Ecoji.getEncoder().readFrom("hello world").writeToString();
String decoded = Ecoji.getDecoder().readFrom(encoded).writeToString();
assert decoded.equals("hello world");
```

See [javadocs](https://www.javadoc.io/doc/io.github.netvl.ecoji/ecoji/latest/index.html) for more information.

## License

This program is licensed under Apache License, Version 2.0, ([LICENSE](LICENSE) or http://www.apache.org/licenses/LICENSE-2.0).
