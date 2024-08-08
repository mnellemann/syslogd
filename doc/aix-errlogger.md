# AIX errlogger to remote syslog

Instructions for how to forward *errlogger* messages from IBM AIX and IBM Power Systems VIO Servers to a remote logging solution.

More information about the AIX *errlogger* is available on the IBM [knowledge center](https://www.ibm.com/support/knowledgecenter/ssw_aix_72/generalprogramming/error_notice.html).

## On each AIX / VIO Server

### Prepare the local syslog service

Configure the local syslog service to forward messages to your remote syslog service (only port 514/UDP is supported).

Create an empty local log file:

```shell
touch /var/log/error.log
```

Add the following to the /etc/syslog.conf file:

```text
# Remote logging to remote host on port 514/UDP (AIX does not support non-default port number)
*.warn @10.32.64.1

# Also log to a local file, rotated daily and kept for 7 days
*.warn /var/log/error.log rotate time 1d files 7

# Optionally log authentication messages to remote host
#auth.info,authpriv.info @10.32.64.1
```
We use *10.32.64.1* as our remote syslog server in the above example.


Restart the syslog service:

```shell
refresh -s syslogd
```

### Forward errlogger to the local syslog

We configure the AIX [error logger](https://www.ibm.com/docs/en/aix/7.3?topic=concepts-error-logging-overview) to forward messages to the local syslog service.

Create an odm errnotify logging template file:

```shell
cat << EOF >/tmp/err.tpl
errnotify:
en_name = "syslog1"
en_persistenceflg = 1
en_method = "/usr/bin/logger -plocal0.err [errnotify] seq: \$1 - \$(/usr/bin/errpt -l \$1 | tail -1)"
EOF
```

Add the template:

```shell
odmadd /tmp/err.tpl
```


Verify messages show up in the local syslog */var/log/error.log* file:

```shell
odmget -q"en_name='syslog1'" errnotify
errlogger Testing 123
```

#### Notes

If you need to delete the errnotify again:

```shell
odmdelete -o errnotify -q"en_name=syslog1"
```

To lookup err message details by a seq. no, run:

```shell
errpt -a -l [seq-no]
```

Or from the VIO padmin shell:

```shell
errlog -ls -seq [seq-no]
```
