# Local Development (Updated 2026-11-26)

## Podman Basics

I'm avoiding Docker and Rancher and using `podman` everywhere because it
doesn't have any licensing or odd restrictions. Focusing on delivering
containers so that things are easily portable between cheap disposable hosts.

To get `podman` working on macOS first you have to start the internal machine.
You only have to run the init command once, and then you can start as needed
later.
> podman machine init
> podman machine start

When you are done using the virtual machines you may as well shut off the
internal machine so you aren't using resources to run a system you aren't using.
> podman machine stop

All podman compose commands should be run inside the `deploy` folder to
reference the `compose.yml` file there.

## Run The Complete Application

If you want to run the complete application as a series of containers that
allows you to read the system logs and easily close it run the application
with the command below and control+c to kill the process.
> podman compose up

If you want to run the application in the background and watch the process
or the logs and kill it using podman commands then these are useful commands.
> podman compose up -d
> podman ps
> podman-compose logs -f
> podman compose stop

## Running Parts of the Application

If you want to start up only the database for example you can look at names
of services and start run only the database in the container.
> podman-compose up -d db

## Cleaning Up After Podman

To do a proper clean up of composed containers use "down" and if nothing is
running it will respond as such.
> podman-compose down

# Day-To-Day Development

Start up only the database in the container so that we don't have to have
all that baggage running locally. It will be reachable at `127.0.0.1:5432` or
`localhost:5432` only for connections and because running in the background
will need to be managed by podman commands but won't get in your way.
> podman-compose up -d db

We likely want to run the Java application locally so that you can quickly
iterate on the code. It would be more hassle than it is worth to run it in
a container. First set up the environment so the variables are registered and
the database override is in place and the correct Java binary is configured.
> set -a && source deploy/.env.dev && set +a
> export DATABASE_URL=jdbc:postgresql://localhost:5432/filmlinkd
> export JAVA_HOME=/opt/homebrew/opt/openjdk@21

Then package and run the Java application.
> mvn package
> java -jar target/app.jar