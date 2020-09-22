# Syslog Daemon

Basic syslog server written in Java. All received messages are written to *stdout*.

The syslog server is able to listen on UDP and/or TCP and parses syslog messages in either RFC5424 or RFC3164 (BSD) format.

*This project is in no way associated with, supported or endorsed by, International Business Machines Corporation (IBM).*

## Usage Instructions

- Install the syslogd package (*.deb* or *.rpm*) from [downloads](https://bitbucket.org/mnellemann/syslogd/downloads/) or compile from source.
- Run *bin/syslogd*, use the *-h* option for help :)

