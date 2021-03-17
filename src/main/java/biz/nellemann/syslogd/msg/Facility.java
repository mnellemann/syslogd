package biz.nellemann.syslogd.msg;

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

    kernel(0),
    user(1),
    mail(2),
    daemon(3),
    auth(4),
    syslog(5),
    print(6),
    news(7),
    uucp(8),
    cron(9),
    authpriv(10),
    ftp(11),
    ntp(12),
    audit(13),
    alert(14),
    time(15),
    local0(16),
    local1(17),
    local2(18),
    local3(19),
    local4(20),
    local5(21),
    local6(22),
    local7(23);

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
