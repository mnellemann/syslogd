/*
   Copyright 2020 mark.nellemann@gmail.com

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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.nellemann.syslogd.LogReceiveEvent;
import biz.nellemann.syslogd.LogReceiveListener;

public class UdpServer extends Thread {

    private final static Logger log = LoggerFactory.getLogger(UdpServer.class);

    protected DatagramSocket socket;
    protected boolean listen = true;

    public UdpServer(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {

        byte[] buf = new byte[8192];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (listen) {
            try {
                socket.receive(packet);
                //String packetData = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                sendEvent(packet);
            } catch (IOException e) {
                log.error("run() - error: {}", e.getMessage());
                listen = false;
            }
        }
        socket.close();
    }

    /*
    private synchronized void sendEvent(String message) {
        LogReceiveEvent event = new LogReceiveEvent( this, message);
        for (LogReceiveListener eventListener : eventListeners) {
            eventListener.onLogEvent(event);
        }
    }
     */

    private synchronized void sendEvent(DatagramPacket packet) {
        LogReceiveEvent event = new LogReceiveEvent( this, packet);
        for (LogReceiveListener eventListener : eventListeners) {
            eventListener.onLogEvent(event);
        }
    }

    /**
     * Event Listener Configuration
     */

    protected List<LogReceiveListener> eventListeners = new ArrayList<>();

    public synchronized void addEventListener(LogReceiveListener listener ) {
        eventListeners.add( listener );
    }

    public synchronized void addEventListener(List<LogReceiveListener> listeners ) {
        eventListeners.addAll(listeners);
    }

    public synchronized void removeEventListener( LogReceiveListener l ) {
        eventListeners.remove( l );
    }

}
