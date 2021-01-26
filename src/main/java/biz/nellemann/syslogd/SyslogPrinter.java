package biz.nellemann.syslogd;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class SyslogPrinter {

    public static String toString(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg.timestamp.toString());
        sb.append(String.format("  [%8.8s.%-6.6s] ", msg.facility, msg.severity));
        sb.append(String.format(" %-16.16s ", msg.hostname));
        sb.append(String.format(" %-32.32s  ", msg.application));
        sb.append(msg.message);
        return sb.toString();
    }


    public static String toAnsiString(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder();

        sb.append(msg.timestamp.toString());

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
        sb.append(" " + new java.text.SimpleDateFormat("MMM dd HH:mm:ss").format(new java.util.Date(msg.timestamp.toEpochMilli())));
        sb.append(" " + msg.hostname);
        sb.append(" " + msg.application);
        sb.append(": " + msg.message);

        return sb.toString();
    }


    // <13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [timeQuality tzKnown="1" isSynced="1" syncAccuracy="125500"] adfdfdf3432434565656
    public static String toRfc5424(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(getPri(msg.facility, msg.severity));
        sb.append("1"); // Version
        sb.append(" " + new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new java.util.Date(msg.timestamp.toEpochMilli())));
        sb.append(" " + msg.hostname);
        sb.append(" " + msg.application);
        sb.append(": " + msg.message);

        return sb.toString();
    }

    static private String getPri(Facility facility, Severity severity) {
        int prival = (facility.toNumber() * 8) + severity.toNumber();
        return String.format("<%d>", prival);
    }

}
