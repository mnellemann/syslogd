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

import biz.nellemann.syslogd.LogReceiveEvent;
import biz.nellemann.syslogd.LogReceiveListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

public class TcpServer {

    private final int port;
    private ServerSocket serverSocket;

    public TcpServer() {
        this(514);
    }

    public TcpServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        while (true)
            new ClientHandler(serverSocket.accept(), eventListeners).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }


    /**
     * Event Listener Configuration
     */

    protected final List<LogReceiveListener> eventListeners = new ArrayList<>();

    public synchronized void addEventListener(LogReceiveListener listener ) {
        eventListeners.add( listener );
    }

    public synchronized void addEventListener(List<LogReceiveListener> listeners ) {
        eventListeners.addAll(listeners);
    }

    public synchronized void removeEventListener( LogReceiveListener l ) {
        eventListeners.remove( l );
    }



    private static class ClientHandler extends Thread {

        protected final List<LogReceiveListener> eventListeners;

        private final Socket clientSocket;
        private BufferedReader in;

        public ClientHandler(Socket socket, List<LogReceiveListener> eventListeners) {
            this.clientSocket = socket;
            this.eventListeners = eventListeners;
        }

        public void run() {

            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sendEvent(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                in.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        private synchronized void sendEvent(String message) {
            LogReceiveEvent event = new LogReceiveEvent( this, message );
            for (LogReceiveListener eventListener : eventListeners) {
                eventListener.onLogEvent(event);
            }
        }

    }

}
