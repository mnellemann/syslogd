# Power Systems HMC Remote Logging

Instructions for how to forward syslog messages from a IBM Power Systems HMC to a remote logging solution.

More information about HMC logging is available on the IBM [knowledge center](https://www.ibm.com/support/pages/hmc-logging-and-auditing).


### Instructions

Network / Firewall must allow UDP (and possible TCP) traffic on port 514 from HMC to the remote syslog server. We use *10.32.64.1* as our remote syslog server in the example below.

To add a remote logging destination:

```shell
chhmc -c syslog -t udp -s add -h 10.32.64.1 --input "filter_msg_contains_discard_strings=run-parts,slice,session,leases,renewal,0anacron,Session,DHCPREQUEST,DHCPACK,CMD"
```

In the above example we filter away some messages that we are not interested in forwarding on remotely.

To remove it again:

```shell
chhmc -c syslog -t udp -s remove -h 10.32.64.1
```
