# Syslog Server

All received messages are written to *stdout* and/or forwarded to another syslog server.

The syslog server is able to listen on both UDP and TCP and parses syslog messages in either RFC5424 or RFC3164 (BSD) format.

The default syslog port (514) requires you to run syslogd as root / administrator.
If you do not wish to do so, you can choose a port number (with the *-p* or *--port* flag) above 1024.

## Usage Instructions

- Install the syslogd package (*.deb* or *.rpm*) from [downloads](https://bitbucket.org/mnellemann/syslogd/downloads/) or build from source.
- Run *bin/syslogd*, use the *-h* option for help :)

````
Usage: syslogd [-dghV] [--[no-]ansi] [--[no-]stdout] [--[no-]tcp] [--[no-]udp]
               [--rfc5424] [-f=<host>] [-p=<port>]
Syslog Server
  -d, --debug            Enable debugging [default: 'false'].
  -f, --forward=<host>   Forward to UDP host[:port] (RFC-5424).
  -g, --gelf             Forward in Graylog (GELF) JSON format.
  -h, --help             Show this help message and exit.
      --[no-]ansi        Output ANSI colors [default: true].
      --[no-]stdout      Output messages to stdout [default: true].
      --[no-]tcp         Listen on TCP [default: true].
      --[no-]udp         Listen on UDP [default: true].
  -p, --port=<port>      Listening port [default: 514].
      --rfc5424          Parse RFC-5424 messages [default: RFC-3164].
  -V, --version          Print version information and exit.
````

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
java -jar /path/to/syslogd-x.y.z-all.jar --forward remotehost:1514
```

Forwarding to a Graylog server in GELF format.

```
java -jar /path/to/syslogd-x.y.z-all.jar --forward remotehost:12201 --gelf
```


If you don't want any output locally (only forwarding), you can use the ```--no-stdout``` flag.


## Notes

Syslog messages from AIX (and IBM Power Virtual I/O Servers) can be troublesome with some logging solutions. These can be received with
syslogd and optionally forwarded on to Graylog, Splunk or other logging solutions.


## Development


### Test Grafana Loki

```shell
docker run --rm -d --name=loki -p 3100:3100 grafana/loki
```
