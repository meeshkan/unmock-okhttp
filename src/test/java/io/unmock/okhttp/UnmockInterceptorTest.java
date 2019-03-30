package io.unmock.okhttp;

import io.unmock.core.UnmockOptions;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class UnmockInterceptorTest {
  @Test
  public void testInterceptor() throws IOException {
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
    // smoke test, will fail if ID not present
    projects.getJSONObject(0).getInt("id");
    response.body().close();
  }

  static private @NotNull UnmockInterceptor requestOne(UnmockOptions options) throws IOException {
    UnmockInterceptor interceptor = new UnmockInterceptor(options);
    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build();

    Request request = new Request.Builder()
            .url("https://www.behance.net/v2/projects/3541")
            .build();
    client.newCall(request).execute();
    return interceptor;
  }

  static private @NotNull UnmockInterceptor requestTwo(UnmockOptions options) throws IOException {
    UnmockInterceptor interceptor = new UnmockInterceptor(options);
    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build();

    Request request = new Request.Builder()
            .url("https://www.behance.net/v2/projects/3541")
            .header("Foo", "Bar")
            .build();
    client.newCall(request).execute();
    return interceptor;
  }

  @Test
  public void sameRequestsYieldSameHash() throws IOException {
    UnmockOptions options = new UnmockOptions.Builder().build();
    Assert.assertEquals("Same requests have same hash", requestOne(options).getStories().get(0), requestOne(options).getStories().get(0));
  }

  @Test
  public void differentRequestsYieldDifferentHash() throws IOException {
    UnmockOptions options = new UnmockOptions.Builder().build();
    Assert.assertNotEquals("Different requests yield different hash", requestOne(options).getStories().get(0), requestTwo(options).getStories().get(0));
  }

  @Test
  public void differentRequestsYieldSameHashWithIgnore() throws IOException {
    UnmockOptions options = new UnmockOptions.Builder()
            .ignore("{\"headers\": \"Foo|foo\"}") // this OR is because okhttp makes all headers lowercase, so Foo becomes foo... feature or bug?
            .build();
    Assert.assertEquals("Different requests yield same hash with ignore", requestOne(options).getStories().get(0), requestTwo(options).getStories().get(0));
  }

}
