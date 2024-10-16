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

import biz.nellemann.syslogd.LogForwardEvent;
import biz.nellemann.syslogd.LogForwardListener;
import biz.nellemann.syslogd.SyslogPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpClient implements LogForwardListener {

    private final static Logger log = LoggerFactory.getLogger(UdpClient.class);

    private final InetSocketAddress inetSocketAddress;
    private final DatagramSocket socket;

   public UdpClient(InetSocketAddress inetSocketAddress) throws SocketException {
        this.inetSocketAddress = inetSocketAddress;
        this.socket = new DatagramSocket();
    }

    public void send(String msg) {
        byte[] buf = msg.getBytes(StandardCharsets.US_ASCII);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, inetSocketAddress.getAddress(), inetSocketAddress.getPort());
        try {
            socket.send(packet);
        } catch (IOException e) {
            log.error("send() - Could not send packet: {}", e.getMessage());
        }
    }

    public void close() {
        socket.close();
    }


    @Override
    public void onForwardEvent(LogForwardEvent event) {
        try {
            send(SyslogPrinter.toRfc5424(event.getMessage()));
        } catch (Exception e) {
            log.warn("onForwardEvent() error", e);
        }
    }

}
