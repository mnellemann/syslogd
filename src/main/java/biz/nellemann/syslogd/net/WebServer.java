package biz.nellemann.syslogd.net;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.stream.Collectors;
import ro.pippo.core.Application;
import biz.nellemann.syslogd.SyslogPrinter;
import biz.nellemann.syslogd.msg.SyslogMessage;

public class WebServer extends Application {

    ArrayDeque<SyslogMessage> deque;

    public void setDeque(ArrayDeque<SyslogMessage> d) {
        deque = d;
    }

    @Override
    protected void onInit() {

        GET("/", routeContext -> routeContext.render("index"));

        // send 'Hello World' as response
        GET("/test", routeContext -> routeContext.send("Hello World"));

        // send logs as reponse
        GET("/log", routeContext -> routeContext.text().negotiateContentType().send(
            deque.stream().sorted(Collections.reverseOrder()).map(SyslogPrinter::toHtml).collect(Collectors.joining())
        ));

    }



}
