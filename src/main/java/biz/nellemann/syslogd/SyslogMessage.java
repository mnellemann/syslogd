package biz.nellemann.syslogd;

import java.time.Instant;

public class SyslogMessage {

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
    protected Integer facility;


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
    Integer severity;

    // The VERSION field denotes the version of the syslog protocol specification.
    Integer version;

    // The TIMESTAMP field is a formalized timestamp derived from [RFC3339].
    Instant timestamp;

    // The HOSTNAME field identifies the machine that originally sent the syslog message.
    String hostname;

    // The APP-NAME field SHOULD identify the device or application that originated the message.
    String application;

    // The PROCID field is often used to provide the process name or process ID associated with a syslog system.
    String processId;

    // The MSGID SHOULD identify the type of message.
    String messageId;

    // STRUCTURED-DATA provides a mechanism to express information in a well defined, easily parseable and interpretable data format.
    String structuredData;

    // The MSG part contains a free-form message that provides information about the event.
    String message;


    public String toString() {
        return String.format("%s %s %s: %s", timestamp.toString(), hostname, application, message);
    }

}
