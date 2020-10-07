package biz.nellemann.syslogd;

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

    EMERG(0),
    ALERT(1),
    CRIT(2),
    ERROR(3),
    WARN(4),
    NOTICE(5),
    INFO(6),
    DEBUG(7);

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

    private Integer severityNumber;
    Severity(int severityNumber) {
        this.severityNumber = severityNumber;
    }

}
