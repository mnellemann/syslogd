package biz.nellemann.syslogd.net;

import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import biz.nellemann.syslogd.SyslogPrinter;
import biz.nellemann.syslogd.msg.SyslogMessage;
import ro.pippo.core.Application;
import ro.pippo.core.PippoSettings;

public class WebServer extends Application {

    private final LogSocketHandler logSocketHandler = new LogSocketHandler();
    private CircularFifoQueue<SyslogMessage> queue;

    public WebServer(PippoSettings settings) {
        super((settings));
    }

    public WebServer(PippoSettings settings, CircularFifoQueue<SyslogMessage> q) {
        super((settings));
        queue = q;
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


    public LogSocketHandler getLogSocketHandler() {
        return logSocketHandler;
    }


    public void setQueue(CircularFifoQueue<SyslogMessage> q) {
        this.queue = q;
    }

}
