# Syslogd as a System Service

## Systemd

Edit the **syslogd.service** and provide wanted options.

To install as a systemd service, copy the **syslogd.service**
file into */etc/systemd/system/* and enable the service:

    systemctl daemon-reload
    systemctl enable syslogd.service
    systemctl restart syslogd.service

To read log output from the service, use:

    journalctl -f -u syslogd.service
