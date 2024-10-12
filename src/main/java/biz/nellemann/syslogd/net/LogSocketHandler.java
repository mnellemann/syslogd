package biz.nellemann.syslogd.net;

import biz.nellemann.syslogd.LogForwardEvent;
import biz.nellemann.syslogd.LogForwardListener;
import biz.nellemann.syslogd.SyslogPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.websocket.WebSocketContext;
import ro.pippo.core.websocket.WebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogSocketHandler implements WebSocketHandler, LogForwardListener {

    private final static Logger log = LoggerFactory.getLogger(LogSocketHandler.class);

    private final List<WebSocketContext> webSocketContextList = new ArrayList<>();


    @Override
    public void onMessage(WebSocketContext webSocketContext, String message) {
        log.debug("onMessage() - {}", message);
        try {
            webSocketContext.sendMessage(message);
        } catch (IOException e) {
            log.warn("onMessage() - {}", message);
        }
    }

    @Override
    public void onMessage(WebSocketContext webSocketContext, byte[] message) {
        log.debug("onMessage()");
        System.out.println("TestWebSocket.onMessage");
    }

    @Override
    public void onOpen(WebSocketContext webSocketContext) {
        log.debug("onOpen()");
        webSocketContextList.add(webSocketContext);
            }

    @Override
    public void onClose(WebSocketContext webSocketContext, int closeCode, String message) {
        log.debug("onClose()");
        webSocketContextList.remove(webSocketContext);
    }

    @Override
    public void onTimeout(WebSocketContext webSocketContext) {
        log.debug("onTimeout()");
        webSocketContextList.remove(webSocketContext);
    }

    @Override
    public void onError(WebSocketContext webSocketContext, Throwable t) {
        log.debug("onError()");
        webSocketContextList.remove(webSocketContext);
    }


    @Override
    public void onForwardEvent(LogForwardEvent event) {
        log.debug("onForwardEvent()");
        String msg = String.format("<tbody hx-swap-oob=\"afterbegin:#content\">%s</tbody>", SyslogPrinter.toHtml(event.getMessage()));
        webSocketContextList.forEach( webSocketContext -> {
            try {
                webSocketContext.sendMessage(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
