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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.nellemann.syslogd.LogForwardEvent;
import biz.nellemann.syslogd.LogForwardListener;
import biz.nellemann.syslogd.SyslogPrinter;

public class LokiClient implements LogForwardListener, Runnable {

    private final static Logger log = LoggerFactory.getLogger(LokiClient.class);

    private final ArrayBlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1024);
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
            con.setReadTimeout(150);
            con.setDoOutput(true);

            byte[] input = msg.getBytes(StandardCharsets.UTF_8);
            try(OutputStream os = con.getOutputStream()) {
                os.write(input, 0, input.length);
            } catch (IOException e) {
                log.warn(e.getMessage());
            }

            int responseCode = con.getResponseCode();
            try (InputStream ignored = con.getInputStream()) {
                if(responseCode != 204) {
                    log.warn("send() - response: " + responseCode);
                }
            }

        } catch (IOException e) {
            log.error("send() - error: " + e.getMessage());
        } finally {
            if(con != null) {
                con.disconnect();
            }
        }

    }


    @Override
    public void run() {

        while (true) {
            try {
                send(blockingQueue.take());
            } catch (MalformedURLException | InterruptedException e) {
                log.warn(e.getMessage());
            }
        }

    }


    @Override
    public void onForwardEvent(LogForwardEvent event) {
        blockingQueue.offer(SyslogPrinter.toLoki(event.getMessage()));
    }

}
