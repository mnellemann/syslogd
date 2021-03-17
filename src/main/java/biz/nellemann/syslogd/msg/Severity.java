package biz.nellemann.syslogd.msg;

import java.util.HashMap;
import java.util.Map;

/*
    0       Emergency: system is unusable
    1       Alert: action must be taken immediately
    2       Critical: critical conditions
    3       Error: error conditions
    4       Warning: warning conditions
    5       Notice: normal but significant condition
    6       Informational: informational messages
    7       Debug: debug-level messages
 */
public enum Severity {

    emerg(0),
    alert(1),
    crit(2),
    error(3),
    warn(4),
    notice(5),
    info(6),
    debug(7);

    // Cache lookups
    private static final Map<Integer, Severity> BY_NUMBER = new HashMap<>();
    static {
        for (Severity s: values()) {
            BY_NUMBER.put(s.severityNumber, s);
        }
    }

    public static Severity getByNumber(Integer number) {
        return BY_NUMBER.get(number);
    }

    public Integer toNumber() {
        return this.severityNumber;
    }

    private final Integer severityNumber;
    Severity(int severityNumber) {
        this.severityNumber = severityNumber;
    }

}
