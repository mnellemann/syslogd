package biz.nellemann.syslogd.net;

import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import biz.nellemann.syslogd.SyslogPrinter;
import biz.nellemann.syslogd.msg.SyslogMessage;

public class WebServer extends Application {

    private final static Logger log = LoggerFactory.getLogger(WebServer.class);
    private final LogSocketHandler logSocketHandler = new LogSocketHandler();
    private CircularFifoQueue<SyslogMessage> queue;


    public void setQueue(CircularFifoQueue<SyslogMessage> q) {
        queue = q;
    }

    public LogSocketHandler getLogSocketHandler() {
        return logSocketHandler;
    }


    @Override
    protected void onInit() {

        GET("/", routeContext -> routeContext.render("index"));

        GET("/ping", routeContext -> routeContext.send("pong"));

        GET("/log", routeContext -> routeContext.text().negotiateContentType().send(
            queue.stream().sorted(Collections.reverseOrder()).map(SyslogPrinter::toHtml).collect(Collectors.joining())
        ));

        addWebSocket("/ws/log", logSocketHandler);

    }


}
