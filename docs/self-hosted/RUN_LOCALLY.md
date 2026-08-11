Compile the full package (build-all) and run the compiled jar and run locally with:

```
export JAVA_HOME=$(/usr/libexec/java_home -v 26)
set -a && source deploy/.env && set +a
mvn package
java -jar target/app.jar
```

Build the container and run locally with:

```
podman build -t filmlinkd-app:local .
podman run -d --name filmlinkd-app --rm --env-file deploy/.env filmlinkd-app:local
```

Then watch logs and stop it with:

```
podman logs -f filmlinkd-app
podman stop filmlinkd-app
```

Or run the whole thing

```
cd deploy
podman-compose up -d
```
