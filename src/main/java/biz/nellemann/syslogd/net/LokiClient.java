package biz.nellemann.syslogd.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class LokiClient {

    private final static Logger log = LoggerFactory.getLogger(LokiClient.class);

    private final URL url;


    public LokiClient(String url) throws MalformedURLException {
        this.url = new URL(url);
    }


    public void send(String msg) throws MalformedURLException {

        URL pushUrl = new URL(url, "/loki/api/v1/push");
        log.warn("send() - URL: " + pushUrl.toString());

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection)pushUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            //con.setRequestProperty("Content-Type", "application/json; utf-8");
            //con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            byte[] input = msg.getBytes(StandardCharsets.US_ASCII);
            try(OutputStream os = con.getOutputStream()) {
                os.write(input, 0, input.length);
                log.warn("send() - Data: " + msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringBuilder content;

            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }

            System.out.println(content.toString());

        } catch (IOException e) {
            log.warn("send() - " + e.getMessage());
        } finally {
            if(con != null) {
                con.disconnect();
            }
        }

    }

}
