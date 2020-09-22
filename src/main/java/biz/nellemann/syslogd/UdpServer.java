package biz.nellemann.syslogd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UdpServer extends Thread {

    protected DatagramSocket socket = null;
    protected boolean listen = true;

    public UdpServer() throws IOException {
        this(514);
    }

    public UdpServer(int port) throws IOException {
        super("SyslogServer");
        socket = new DatagramSocket(port);
    }

    public void run() {

        byte[] buf = new byte[10000];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (listen) {
            try {
                socket.receive(packet);
                String packetData = new String(packet.getData(), "UTF8");
                sendEvent(packetData);
            } catch (Exception e) {
                e.printStackTrace();
                listen = false;
            }
        }
        socket.close();
    }

    private synchronized void sendEvent(String message) {
        LogEvent event = new LogEvent( this, message );
        Iterator listeners = eventListeners.iterator();
        while( listeners.hasNext() ) {
            ( (LogListener) listeners.next() ).onLogEvent( event );
        }
    }


    /**
     * Event Listener Configuration
     */

    protected List<LogListener> eventListeners = new ArrayList<>();

    public synchronized void addEventListener( LogListener l ) {
        eventListeners.add( l );
    }

    public synchronized void removeEventListener( LogListener l ) {
        eventListeners.remove( l );
    }

}
