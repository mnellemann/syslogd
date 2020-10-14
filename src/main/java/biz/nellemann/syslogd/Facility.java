package biz.nellemann.syslogd;

import java.util.HashMap;
import java.util.Map;

/*
   0             kernel messages
   1             user-level messages
   2             mail system
   3             system daemons
   4             security/authorization messages
   5             messages generated internally by syslogd
   6             line printer subsystem
   7             network news subsystem
   8             UUCP subsystem
   9             clock daemon
   10             security/authorization messages
   11             FTP daemon
   12             NTP subsystem
   13             log audit
   14             log alert
   15             clock daemon (note 2)
   16             local use 0  (local0)
   17             local use 1  (local1)
   18             local use 2  (local2)
   19             local use 3  (local3)
   20             local use 4  (local4)
   21             local use 5  (local5)
   22             local use 6  (local6)
   23             local use 7  (local7)
*/
public enum Facility {

    KERNEL(0),
    USER(1),
    MAIL(2),
    DAEMON(3),
    AUTH(4),
    SYSLOG(5),
    PRINT(6),
    NEWS(7),
    UUCP(8),
    CRON(9),
    AUTHPRIV(10),
    FTP(11),
    NTP(12),
    AUDIT(13),
    ALERT(14),
    TIME(15),
    LOCAL0(16),
    LOCAL1(17),
    LOCAL2(18),
    LOCAL3(19),
    LOCAL4(20),
    LOCAL5(21),
    LOCAL6(22),
    LOCAL7(23);

    // Cache lookups
    private static final Map<Integer, Facility> BY_NUMBER = new HashMap<>();
    static {
        for (Facility f: values()) {
            BY_NUMBER.put(f.facilityNumber, f);
        }
    }

    public static Facility getByNumber(Integer number) {
        return BY_NUMBER.get(number);
    }

    public Integer toNumber() {
        return this.facilityNumber;
    }

    private final Integer facilityNumber;
    Facility(int facilityNumber) {
        this.facilityNumber = facilityNumber;
    }

}
