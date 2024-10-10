#!/bin/sh
# shellcheck disable=SC2154
# Debian post install script
# See https://wiki.debian.org/MaintainerScripts
# $1 == "configure"
# $1 == "upgrade"

#echo "Running Debian Post Install Script with: $@"

# Configure package
if [ "$1" = "configure" ] ; then
    test -n "$config_source" && install_config "$service_name" "$config_source"
    install_service "$service_name" "$sysv_source" "$systemd_source"
fi

# Upgrade package
if [ "$1" = "upgrade" ] ; then
    refresh_servie  "$service_name"
fi
