package ass.prochka6.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Kamil Prochazka
 */
public class GetUrl {

    public static void main(String[] args) throws IOException {
        HttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

        HttpResponse response = httpClient.execute(new HttpGet("https://www.random.org/strings/?num=10&len=10&digits=on&unique=on&format=html&rnd=new"));

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));) {
            String line = null;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

}
