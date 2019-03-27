package io.unmock.okhttp;

import io.unmock.core.PersistableData;
import io.unmock.core.Token;
import io.unmock.core.UnmockOptions;
import io.unmock.core.Util;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class UnmockInterceptor implements Interceptor {

  final private @NotNull UnmockOptions unmockOptions;
  final private @NotNull List<String> story = new ArrayList<>();
  final private @NotNull boolean xy;
  final private @Nullable String accessToken;

  public UnmockInterceptor(UnmockOptions unmockOptions) throws IOException {
    this.unmockOptions = unmockOptions;
    this.accessToken = Token.getAccessToken(unmockOptions.persistence, unmockOptions.unmockHost, unmockOptions.unmockPort);
    this.xy = this.accessToken != null;
  }

  @Override public Response intercept(Interceptor.Chain chain) throws IOException {

    final Request request = chain.request();
    final HttpUrl url = request.url();
    final Headers headers = request.headers();
    final RequestBody body = request.body();
    final String method = request.method();
    boolean selfCall = false;

    if (request.url().host().equals(unmockOptions.unmockHost)) {
      selfCall = true;
    }

    Collection<String> whitelist = new ArrayList<>();
    if (unmockOptions.whitelist != null) {
      whitelist.addAll(unmockOptions.whitelist);
    }
    whitelist.add(unmockOptions.unmockHost);

    if (Util.hostIsWhitelisted(whitelist, request.url().host())) {
      return chain.proceed(request);
    }

    final Map<String, String> headerz = new HashMap<>();

    for (Map.Entry<String, List<String>> entry : headers.toMultimap().entrySet()) {
      headerz.put(entry.getKey(), Arrays.stream(entry.getValue().toArray(new String[] {})).collect(Collectors.joining(",")));
    }

    final String path = Util.buildPath(
      headerz,
      url.host(),
      unmockOptions.ignore,
      method,
      url.encodedPath(), // TODO: should this be encoded?
      unmockOptions.signature,
      story,
      unmockOptions.unmockHost,
      this.xy
    );

    Request.Builder requestBuilder = request.newBuilder()
      .url("https://" + unmockOptions.unmockHost + path).method(method, body);

    for (Map.Entry<String, String> entry : headerz.entrySet()) {
      if (entry.getKey().equals("Authorization")) {
        // do nothing
      } else {
        requestBuilder = requestBuilder.header(entry.getKey(), entry.getValue());
      }
    }
    if (accessToken != null) {
      requestBuilder = requestBuilder.header("Authorization", "Bearer " + accessToken);
    }

    Response response = chain.proceed(requestBuilder.build());

    final Map<String, String> outputHeaders = new HashMap<>();

    for (Map.Entry<String, List<String>> entry : response.headers().toMultimap().entrySet()) {
      outputHeaders.put(entry.getKey(), Arrays.stream(entry.getValue().toArray(new String[] {})).collect(Collectors.joining(",")));
    }

    Util.endReporter(
      body != null ? body.toString() : null,
      null, // we purposefully make this null so that we do not read the stream before it's necessary. fix?
      outputHeaders,
      url.host(),
      unmockOptions.logger,
      method,
      url.encodedPath(),
      unmockOptions.persistence,
      unmockOptions.save,
      selfCall,
      this.story,
      this.xy,
      new PersistableData(headerz, url.host(), method, url.encodedPath())
    );

    return response;
  }
}
