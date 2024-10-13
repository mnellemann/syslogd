package biz.nellemann.syslogd.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.PippoSettings;

public class WebServer extends Application {

    private final static Logger log = LoggerFactory.getLogger(WebServer.class);
    private final LogSocketHandler logSocketHandler = new LogSocketHandler();

    public LogSocketHandler getLogSocketHandler() {
        return logSocketHandler;
    }

    public WebServer(PippoSettings settings) {
        super((settings));
    }

    @Override
    protected void onInit() {

        GET("/", routeContext -> routeContext.render("index"));

        GET("/ping", routeContext -> routeContext.send("pong"));

        /*
        GET("/log", routeContext -> routeContext.text().negotiateContentType().send(
            queue.stream().sorted(Collections.reverseOrder()).map(SyslogPrinter::toHtml).collect(Collectors.joining())
        ));*/

        addWebSocket("/ws/log", logSocketHandler);

    }


}
