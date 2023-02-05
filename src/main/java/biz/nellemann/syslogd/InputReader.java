package biz.nellemann.syslogd;

import biz.nellemann.syslogd.msg.SyslogMessage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputReader extends Thread {

    private final Scanner input;
    private final String protocol;

    public InputReader(InputStream inputStream, String protocol) {
        input = new Scanner(inputStream);
        this.protocol = protocol;
    }

    public void run() {

        while(input.hasNextLine()) {
            SyslogMessage msg = new SyslogMessage(input.nextLine());
            msg.hostname = "localhost";
            msg.application = "syslogd";

            String payload;
            if(protocol.equalsIgnoreCase("GELF"))
                payload = SyslogPrinter.toGelf(msg);
            else if (protocol.equalsIgnoreCase("RFC-5424")) {
                payload = SyslogPrinter.toRfc5424(msg);
            } else {
                payload = SyslogPrinter.toRfc3164(msg);
            }

            sendEvent(payload);
        }
        input.close();
    }


    private synchronized void sendEvent(String text) {
        LogReceiveEvent event = new LogReceiveEvent( this, text);
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
