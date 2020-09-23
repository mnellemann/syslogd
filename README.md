# Syslog Daemon

Basic syslog server written in Java. All received messages are written to *stdout*.

The syslog server is able to listen on UDP and/or TCP and parses syslog messages in either RFC5424 or RFC3164 (BSD) format. The default syslog port (514) requires you to run syslogd as root / administrator. If you do not with to do so, you can choose a port number (with the -p flag) above 1024.

## Usage Instructions

- Install the syslogd package (*.deb* or *.rpm*) from [downloads](https://bitbucket.org/mnellemann/syslogd/downloads/) or compile from source.
- Run *bin/syslogd*, use the *-h* option for help :)

````
Usage: syslogd [-hV] [--[no-]tcp] [--[no-]udp] [--rfc3164] [-p=<port>]
Simple syslog server that prints messages to stdout.
  -h, --help          Show this help message and exit.
      --[no-]tcp      Listen on TCP, true by default.
      --[no-]udp      Listen on UDP, true by default.
  -p, --port=<port>   Listening port, 514 (privileged) by default.
      --rfc3164       Parse RFC3164 syslog message, RFC5424 by default.
  -V, --version       Print version information and exit.
````
