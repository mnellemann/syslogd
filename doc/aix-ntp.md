# AIX NTP & Timezone

## Change date and timezone

Use smitty ```smitty chtz_date``` or edit the */etc/environment* file and set **TZ=** environment variable to a valid timezone.

## Network Time Synchronization

Synchronize once against a remote timeserver:

```shell
ntpdate -d 0.pool.ntp.org
```

To keep time accurate going forward, edit /etc/ntp.conf and add one or more timeservers:

```
server 0.pool.ntp.org
server 1.pool.ntp.org
server 2.pool.ntp.org
server 3.pool.ntp.org
```

Then enable the NTP service and/or refresh it:

```
chrctcp -S -a xntpd
refresh -s xntpd
```


