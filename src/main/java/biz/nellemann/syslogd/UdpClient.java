package biz.nellemann.syslogd;

import java.io.IOException;
import java.net.*;

public class UdpClient {

    private DatagramSocket socket;
    private InetAddress address;
    private Integer port;

    private byte[] buf;

    public UdpClient(String host, Integer port) throws UnknownHostException, SocketException {
        socket = new DatagramSocket();
        address = InetAddress.getByName(host);
        this.port = port;
    }

    public void send(String msg) throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet
            = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    public void close() {
        socket.close();
    }
}
