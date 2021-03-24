# PowerVC Remote Loggging

Configure rsyslog on your PowerVC instance to forward authentication messages to a remote logging solution. We use *10.32.64.1* as our remote syslog server in this example.

Create a file new file in the **/etc/rsyslog.d** folder (eg. *remote.conf*) with the following content:

```text
# Log authentication messages to remote host
auth.info,authpriv.info @10.32.64.1
```

Restart the rsyslog service

```shell
systemctl restart rsyslog
```
