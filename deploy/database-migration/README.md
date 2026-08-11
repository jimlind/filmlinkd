You have to jump through a bunch of hoops which is gross.
If you have all the gcloud tools locally installed you can work around things a bit I don't want to have all those
tools installed for a one-time job.

For the sake of this document we are going to be working on the users-dev table. I think we can safely ignore the vip
table. It's very small and can easily manually be manipulated as it makes sense. Easier than trying to force an
automation around it.

Go to: https://console.cloud.google.com/firestore/databases/-default-/import-export
You can only export to a Google bucket. It'll come with a timestamp so you can use an existing bucket.
I told it to write to one region because in theory writing to one region means it's cheaper.

Go to: https://console.cloud.google.com/storage/browser
You can see the full path of the export. It will be a timestamp. Click the box to select the folder and click the
download button. It will give you a dialog telling what command to run to download an entire directory.
Something like:
$ gcloud storage cp --recursive "gs://project-export-bucket/2026-08-10T22:13:25_74823" .

Use the icon in the top right corner to activate a cloud shell. It's a shell that live in the Google Cloud with your
credentials so you can do almost everything without needing everything install the tools locally.

When the prompt is open something like:

```
$ mkdir ./firestore-data-export
$ gcloud storage cp --recursive "gs://project-export-bucket/2026-08-10T22:13:25_74823" ./firestore-data-export
$ tar -czf current-firestore-data-export.tar.gz ./firestore-data-export
```

Within the cloud shell there's a vertical ... button for "More" that will allow you to select Download. Then browse to
your new tar.gz file and select it, and you can download it straight to your local computer.

Extract the firestore data

```
$ tar -xzf current-firestore-data-export.tar.gz
```

A new folder containing the complete export will now exist in the local folder firestore-data-export. Create a folder
inside that to contain the decoded data that we'll take care of later.

```
$ mkdir ../firestore-data-export/decoded
```

Clone the repo of a tool that'll translate the firestore data to something globally usable. Create a venv to contain
any of the dependencies and install them. They work fine in the default Python 3.12 installed on my Mac.

```
$ git clone https://github.com/renatofdds/firestore-export-json
$ cd firestore-export-json
$ python3 -m venv .venv
$ .venv/bin/pip install -r requirements.txt
```

This is the full command for the users-dev decoding from inside the cloned git project when the data is outside of it

```
$ .venv/bin/python fs_to_json.py ../firestore-data-export/*/all_namespaces/kind_users-dev ../firestore-data-export/decoded/users-dev
```

It will create a file called output-0.

In GCP we had one giant datastore "users" and "users-dev" that helped us differentiate but self-hosted means
that the database names should stay the same but things like credentials and data can vary.

Because of this at the top of the output-0 file you need to change "users-dev" to "users" so replicate what we'll be
doing with production data.

Now run the migrate.py and dedupe commands. They don't have any dependencies so can be run without venv.

```
$ cd ../
$ python3 01.migrate.py --json-dir firestore-data-export/decoded/users-dev --csv-dir firestore-users-dev-csv
$ python3 02.dedupe.py --csv-dir firestore-users-dev-csv
```

We now have a bunch of normalized deduped CSVs of our user data available for importing. So start the database server.
If the database doesn't exist yet, it is automatically created using the data with the environment and commands. This
should be run in the "deploy" directory.

```
$ cd ../
$ podman-compose up -d db
$ podman-compose exec -T db psql -U filmlinkd -d filmlinkd -f /dev/stdin < database-migration/schema.sql
$ podman-compose exec db psql -U filmlinkd -d filmlinkd -c '\d+'
```

Copy the CSV files over to the container so they can be imported, then import one at a time, and confirm data.

```
$ podman cp database-migration/firestore-users-dev-csv deploy_db_1:/tmp/
$ podman-compose exec -T db psql -U filmlinkd -d filmlinkd -c "\copy users FROM '/tmp/firestore-users-dev-csv/users.csv' CSV HEADER"
$ podman-compose exec -T db psql -U filmlinkd -d filmlinkd -c "\copy user_channels FROM '/tmp/firestore-users-dev-csv/user_channels.csv' CSV HEADER"
$ podman-compose exec -T db psql -U filmlinkd -d filmlinkd -c "\copy user_previous_entries FROM '/tmp/firestore-users-dev-csv/user_previous_entries.csv' CSV HEADER"
$ podman-compose exec -T db psql -U filmlinkd -d filmlinkd -c "SELECT letterboxd_id, user_name, display_name FROM users;"
```
