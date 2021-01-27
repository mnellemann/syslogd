package biz.nellemann.syslogd;

import biz.nellemann.syslogd.msg.Facility;
import biz.nellemann.syslogd.msg.Severity;
import biz.nellemann.syslogd.msg.SyslogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogPrinter {

    private final static Logger log = LoggerFactory.getLogger(SyslogPrinter.class);

    private final static char SPACE = ' ';

    public static String toString(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder(msg.timestamp.toString());
        sb.append(String.format("  [%8.8s.%-6.6s] ", msg.facility, msg.severity));
        sb.append(String.format(" %-16.16s ", msg.hostname));
        sb.append(String.format(" %-32.32s  ", msg.application));
        sb.append(msg.message);
        return sb.toString();
    }


    public static String toAnsiString(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder(msg.timestamp.toString());

        if (msg.severity.toNumber() < 3) {
            sb.append(Ansi.RED);
        } else if (msg.severity.toNumber() < 5) {
            sb.append(Ansi.YELLOW);
        } else {
            sb.append(Ansi.GREEN);
        }

        sb.append(String.format("  [%8.8s.%-6.6s] ", msg.facility, msg.severity)).append(Ansi.RESET);
        sb.append(Ansi.BLUE).append(String.format(" %-16.16s ", msg.hostname)).append(Ansi.RESET);
        sb.append(Ansi.CYAN).append(String.format(" %-32.32s  ", msg.application)).append(Ansi.RESET);
        sb.append(msg.message);

        return sb.toString();
    }


    // <13>Sep 23 08:53:28 xps13 mark: adfdfdf3432434
    public static String toRfc3164(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(getPri(msg.facility, msg.severity));
        sb.append(new java.text.SimpleDateFormat("MMM dd HH:mm:ss").format(new java.util.Date(msg.timestamp.toEpochMilli())));
        sb.append(SPACE).append(msg.hostname);
        sb.append(SPACE).append(msg.application);
        sb.append(":").append(SPACE).append(msg.message);
        log.debug(sb.toString());
        return sb.toString();
    }


    // <13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [timeQuality tzKnown="1" isSynced="1" syncAccuracy="125500"] adfdfdf3432434565656
    // <34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8
    public static String toRfc5424(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(getPri(msg.facility, msg.severity)).append("1");
        sb.append(SPACE).append(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date(msg.timestamp.toEpochMilli())));
        sb.append(SPACE).append(msg.hostname);
        sb.append(SPACE).append(msg.application);
        sb.append(SPACE).append(msg.processId);
        sb.append(SPACE).append(msg.messageId);
        sb.append(SPACE).append(msg.structuredData);
        sb.append(SPACE).append(msg.message);
        log.debug(sb.toString());
        return sb.toString();
    }


    static private String getPri(Facility facility, Severity severity) {
        int pri = (facility.toNumber() * 8) + severity.toNumber();
        return String.format("%c%d%c", '<', pri, '>');
    }

}
