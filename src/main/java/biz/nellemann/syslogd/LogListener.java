package biz.nellemann.syslogd;

public interface LogListener {
    public void onLogEvent(LogEvent event);
}
