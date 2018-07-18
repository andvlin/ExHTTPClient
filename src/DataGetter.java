import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class DataGetter implements ResponseHandler<String> {

  public String getResponse(String url) throws ClientProtocolException, IOException {
      CloseableHttpClient client = HttpClients.createDefault();
    HttpGet getUrl = new HttpGet(url);
    String response = client.execute(getUrl, this);
    client.close();
    return response;
  }

  @Override
  public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
    int status = response.getStatusLine().getStatusCode();
    if (status >= 200 && status < 300) {
      HttpEntity entity = response.getEntity();
      return entity != null ? EntityUtils.toString(entity) : null;
    } else {
      throw new ClientProtocolException("Unexpected response status: " + status);
    }
  }
  
}
