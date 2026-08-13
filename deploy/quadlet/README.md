Podman quadlet files go in the `/etc/containers/systemd/` directory so copy all the *.container files to the server.
The services are configured to use the `/opt/filmlinkd/.env` as the central location for configuration so copy that to
the server.

```
sudo cp deploy/quadlet/filmlinkd.network /etc/containers/systemd
sudo cp deploy/quadlet/filmlinkd-app.container /etc/containers/systemd
sudo cp deploy/quadlet/filmlinkd-db.container /etc/containers/systemd
sudo cp deploy/quadlet/filmlinkd-pgdata.volume /etc/containers/systemd
```

Reload the system and start the daemon. I like to start them by order of dependency first so that I can see any
potential failures earlier.

```
sudo systemctl daemon-reload
sudo systemctl start filmlinkd-pgdata-volume.service
sudo systemctl start filmlinkd-db.service
sudo systemctl start filmlinkd-app.service
```
