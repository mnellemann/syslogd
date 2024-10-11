#!/bin/sh
# shellcheck disable=SC2154
# RedHat postTrans script
# $1 == 1  for install
# $1 == 2  for upgrade

#echo "Running RedHat Post Transaction Script with: $@"

# Install
if [ "$1" = "1" ] ; then
    test -n "$config_source" && install_config "$service_name" "$config_source"
    install_service "$service_name" "$sysv_source" "$systemd_source"
fi

refresh_service "$service_name"

exit 0
