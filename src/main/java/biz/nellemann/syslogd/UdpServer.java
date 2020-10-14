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
package biz.nellemann.syslogd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UdpServer extends Thread {

    protected DatagramSocket socket;
    protected boolean listen = true;

    public UdpServer() throws IOException {
        this(514);
    }

    public UdpServer(int port) throws IOException {
        super("SyslogServer");
        socket = new DatagramSocket(port);
    }

    public void run() {

        byte[] buf = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (listen) {
            try {
                socket.receive(packet);
                String packetData = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                sendEvent(packetData);
            } catch (Exception e) {
                e.printStackTrace();
                listen = false;
            }
        }
        socket.close();
    }

    private synchronized void sendEvent(String message) {
        LogEvent event = new LogEvent( this, message);
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
