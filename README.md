# Syslog Server

All received messages are written to *stdout* and/or forwarded to a remote logging solution.

The syslog server is able to listen on both UDP and TCP and parses syslog messages in either RFC5424 or RFC3164 (BSD) format.

The default syslog port (514) requires you to run syslogd as root / administrator.
If you do not wish to do so, you can choose a port number (with the *-p* or *--port* flag) above 1024.

Supported remote logging solutions are Syslog (RFC5424 over UDP), Graylog (GELF over UDP) and Grafana Loki.

## Usage Instructions

- Install the syslogd package (*.deb* or *.rpm*) from [downloads](https://bitbucket.org/mnellemann/syslogd/downloads/) or build from source.
- Run *bin/syslogd*, use the *-h* option for help :)

```text
Usage: syslogd [-dhV] [--[no-]ansi] [--[no-]stdout] [--[no-]tcp] [--[no-]udp]
               [--rfc5424] [-g=<uri>] [-l=<url>] [-p=<num>] [-s=<uri>]
  -d, --debug          Enable debugging [default: 'false'].
  -g, --gelf=<uri>     Forward to Graylog <udp://host:port>.
  -h, --help           Show this help message and exit.
  -l, --loki=<url>     Forward to Grafana Loki <http://host:port>.
      --[no-]ansi      Output ANSI colors [default: true].
      --[no-]stdout    Output messages to stdout [default: true].
      --[no-]tcp       Listen on TCP [default: true].
      --[no-]udp       Listen on UDP [default: true].
  -p, --port=<num>     Listening port [default: 514].
      --rfc5424        Parse RFC-5424 messages [default: RFC-3164].
  -s, --syslog=<uri>   Forward to Syslog <udp://host:port> (RFC-5424).
  -V, --version        Print version information and exit.
```

### Examples

Listening on a non-standard syslog port:

```
java -jar /path/to/syslogd-x.y.z-all.jar --port 1514
```

or, if installed as a *deb* or *rpm* package:

```
/opt/syslogd/bin/syslogd --port 1514
```

Listening on the standard syslog port (requires root privileges) and forwarding messages on to another log-system on a non-standard port.

```
java -jar /path/to/syslogd-x.y.z-all.jar --syslog udp://remotehost:514
```

Forwarding to a Graylog server in GELF format.

```
java -jar /path/to/syslogd-x.y.z-all.jar --gelf udp://remotehost:12201
```

Forwarding to a Grafana Loki server.

```
java -jar /path/to/syslogd-x.y.z-all.jar --loki http://remotehost:3100
```

If you don't want any output locally (only forwarding), you can use the ```--no-stdout``` flag.


## Notes

### IBM AIX and VIO Servers

Syslog messages from AIX (and IBM Power Virtual I/O Servers) can be troublesome with some logging solutions. These can be received with
syslogd and then forwarded on to your preferred logging solution.


## Development Notes

### Test Grafana Loki

Run Loki and Grafana in local containers to test.

```shell
docker run --rm -d --name=loki -p 3100:3100 grafana/loki
docker run --rm -d --name=grafana --link loki:loki -p 3000:3000 grafana/grafana:7.1.3
```

