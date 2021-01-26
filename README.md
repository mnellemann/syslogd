# Simple Syslog Server

Basic syslog server written in Java. All received messages are written to *stdout* or optionally forwarded to another syslog server.

The syslog server is able to listen on UDP and/or TCP and parses syslog messages in either RFC5424 or RFC3164 (BSD) format.

The default syslog port (514) requires you to run syslogd as root / administrator.
If you do not wish to do so, you can choose a port number (with the -p flag) above 1024.

## Usage Instructions

- Install the syslogd package (*.deb* or *.rpm*) from [downloads](https://bitbucket.org/mnellemann/syslogd/downloads/) or build from source.
- Run *bin/syslogd*, use the *-h* option for help :)

````
Usage: syslogd [-fhV] [--[no-]ansi] [--[no-]stdout] [--[no-]tcp] [--[no-]udp]
               [--rfc5424] [--forward-host=<hostname>] [--forward-port=<port>]
               [-p=<port>]
Simple Syslog Server
  -f, --forward       Forward messages (UDP RFC-3164) [default: false].
      --forward-host=<hostname>
                      Forward to host [default: localhost].
      --forward-port=<port>
                      Forward to port [default: 1514].
  -h, --help          Show this help message and exit.
      --[no-]ansi     Output ANSI colors [default: true].
      --[no-]stdout   Output messages to stdout [default: true].
      --[no-]tcp      Listen on TCP [default: true].
      --[no-]udp      Listen on UDP [default: true].
  -p, --port=<port>   Listening port [default: 514].
      --rfc5424       Parse RFC-5424 messages [default: RFC-3164].
  -V, --version       Print version information and exit.
````
