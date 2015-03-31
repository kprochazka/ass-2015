package ass.prochka6.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author Kamil Prochazka
 */
public class SslGetUrl {

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
        HttpResponse response = httpClient.execute(new HttpGet("https://www.random.org/strings/?num=10&len=10&digits=on&unique=on&format=html&rnd=new"));
        System.out.println(EntityUtils.toString(response.getEntity(), EntityUtils.getContentCharSet(response.getEntity())));
        EntityUtils.consume(response.getEntity());
        httpClient.close();
    }

}
