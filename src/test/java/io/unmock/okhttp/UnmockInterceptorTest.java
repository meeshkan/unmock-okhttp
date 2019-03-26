package io.unmock.okhttp;

import com.google.gson.Gson;
import io.unmock.core.UnmockOptions;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
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
}
