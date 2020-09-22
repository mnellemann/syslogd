package biz.nellemann.syslogd;


import java.util.EventObject;

public class LogEvent extends EventObject {

    private String message;

    public LogEvent(Object source, String message ) {
        super( source );
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
