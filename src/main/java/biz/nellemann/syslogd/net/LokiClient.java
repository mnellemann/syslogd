/*
   Copyright 2021 mark.nellemann@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package biz.nellemann.syslogd.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class LokiClient {

    private final static Logger log = LoggerFactory.getLogger(LokiClient.class);
    private final URL url;


    public LokiClient(URL url) {
        this.url = url;
    }


    public void send(String msg) throws MalformedURLException {

        URL pushUrl = new URL(url, "/loki/api/v1/push");
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection)pushUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(500);
            con.setReadTimeout(100);
            con.setDoOutput(true);

            byte[] input = msg.getBytes(StandardCharsets.UTF_8);
            try(OutputStream os = con.getOutputStream()) {
                os.write(input, 0, input.length);
            } catch (IOException e) {
                log.warn(e.getMessage());
            }

            int responseCode = con.getResponseCode();
            if(responseCode != 204) {
                log.warn("send() - response: " + responseCode);
                log.debug("send() - msg: " + msg);
            }


        } catch (IOException e) {
            log.error("send() - " + e.getMessage());
        } finally {
            if(con != null) {
                con.disconnect();
            }
        }

    }

}
