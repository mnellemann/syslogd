package biz.nellemann.syslogd.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpClient {

    private final static Logger log = LoggerFactory.getLogger(UdpClient.class);

    private DatagramSocket socket;
    private InetAddress address;
    private final Integer port;

   public UdpClient(String host, Integer port) {

        try {
            this.address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            log.error("UdpClient() - UnknownHostException: " + e.getMessage());
        }

        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            log.error("UdpClient() - Could not instantiate DatagramSocket: " + e.getMessage());
        }

        this.port = port;
    }

    public void send(String msg) {
        byte[] buf = msg.getBytes(StandardCharsets.US_ASCII);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
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
