# [Unmock](https://www.unmock.io/) (OkHttp)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.unmock/okhttp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.unmock/okhttp)
[![CircleCI](https://circleci.com/gh/unmock/unmock-okhttp.svg?style=svg)](https://circleci.com/gh/unmock/unmock-okhttp)
[![Known Vulnerabilities](https://snyk.io/test/github/unmock/unmock-okhttp/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/unmock/unmock-okhttp?targetFile=build.gradle)
[![Chat on Gitter](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/unmock/community)

Public API mocking for Java, Scala, Kotlin, Clojure, Groovy and friends.

Unmock can be used to test modules that perform requests to third-party APIs like Hubspot, SendGrid, Behance, and hundreds of other public APIs.

`unmock-okhttp` is a high-level interceptor for `okhttp3`.

The ultimate goal of unmock is to provide a semantically and functionally adequate mock of the internet.

## How does it work?

The `unmock-okhttp` package uses the `okhttp` interceptor pattern to intercept requests and run them through unmock.  For example, the following test intercepts a call to the Behance API and automatically returns mock data.

```java
  @Test
  public void testCallingBehanceAPI() throws IOException {
    UnmockOptions options = new UnmockOptions.Builder().build();
    UnmockInterceptor interceptor = new UnmockInterceptor(options);
    OkHttpClient client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build();

    Request request = new Request.Builder()
      .url("https://www.behance.net/v2/projects")
      .build();

    Response response = client.newCall(request).execute();
    JSONObject json = new JSONObject(response.body().string());
    JSONArray projects = json.getJSONArray("projects");
    Assert.assertTrue(projects.getJSONObject(0).getInt("id") >= 0);
    response.body().close();
  }
```

## Install

```gradle
dependencies {
  compile group: 'io.unmock', name: 'okhttp', version: '0.0.0';
  compile group: 'io.unmock', name: 'core', version: '0.0.0';
}
```

### Java version support

Unmock is written in Java 1.8.  It is currently untested for older versions of Java but may work for them as well.

## unmock.io

The URLs printed to the command line are hosted by [unmock.io](https://www.unmock.io).  You can consult the documentation about that service [here](https://www.unmock.io/docs).

## Contributing

Thanks for wanting to contribute! Take a look at our [Contributing Guide](CONTRIBUTING.md) for notes on our commit message conventions and how to run tests.

Please note that this project is governed by the [Unmock Community Code of Conduct](https://github.com/unmock/code-of-conduct). By participating in this project, you agree to abide by its terms.

## License

[MIT](LICENSE)

Copyright (c) 2018‚ 2019 [Meeshkan](http://meeshkan.com) and other [contributors](https://github.com/unmock/unmock-js/graphs/contributors).
