package biz.nellemann.syslogd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TcpServer {

    private int port;
    private ServerSocket serverSocket;

    TcpServer() {
        this(514);
    }

    TcpServer(int port) {
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

    protected List<LogListener> eventListeners = new ArrayList<>();

    public synchronized void addEventListener( LogListener l ) {
        eventListeners.add( l );
    }

    public synchronized void removeEventListener( LogListener l ) {
        eventListeners.remove( l );
    }



    private static class ClientHandler extends Thread {

        protected List<LogListener> eventListeners;

        private Socket clientSocket;
        private BufferedReader in;

        public ClientHandler(Socket socket, List<LogListener> eventListeners) {
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
            LogEvent event = new LogEvent( this, message );
            Iterator listeners = eventListeners.iterator();
            while( listeners.hasNext() ) {
                ( (LogListener) listeners.next() ).onLogEvent( event );
            }
        }

    }

}
