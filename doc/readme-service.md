# Syslogd as a system service

## For systemd

To install as a systemd service, copy the [syslogd.service](syslogd.service)
file into */etc/systemd/system/*, edit the file and configure your required options.

Enable and start the service:

```shell
systemctl daemon-reload
systemctl enable syslogd.service
systemctl restart syslogd.service
```

To read log output from the service, use:

```shell
journalctl -f -u syslogd.service
```
