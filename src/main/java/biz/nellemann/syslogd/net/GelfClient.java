package biz.nellemann.syslogd.net;

import java.net.InetSocketAddress;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.nellemann.syslogd.LogForwardEvent;
import biz.nellemann.syslogd.SyslogPrinter;

public class GelfClient extends UdpClient {

    private final static Logger log = LoggerFactory.getLogger(GelfClient.class);

    public GelfClient(InetSocketAddress inetSocketAddress) throws SocketException {
        super(inetSocketAddress);
    }

    @Override
    public void onForwardEvent(LogForwardEvent event) {
        try {
            send(SyslogPrinter.toGelf(event.getMessage()));
        } catch (Exception e) {
            log.warn("onForwardEvent() error", e);
        }

    }
}
