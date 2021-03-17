package biz.nellemann.syslogd.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpClient {

    private final static Logger log = LoggerFactory.getLogger(UdpClient.class);

    private InetSocketAddress inetSocketAddress;
    private DatagramSocket socket;

   public UdpClient(InetSocketAddress inetSocketAddress) throws SocketException {
        this.inetSocketAddress = inetSocketAddress;
        this.socket = new DatagramSocket();
    }

    public void send(String msg) {
        byte[] buf = msg.getBytes(StandardCharsets.US_ASCII);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, inetSocketAddress.getAddress(), inetSocketAddress.getPort());
        if(this.socket != null) {
            try {
                socket.send(packet);
            } catch (IOException e) {
                log.error("send() - Could not send packet: " + e.getMessage());
            }
        }
    }

    public void close() {
        socket.close();
    }
}
