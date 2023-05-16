# Documentation

## Pub/Sub Message Systems

### Log Entry

There are one or more scrapers that are not tied to specific servers that publish messages to
Google's Pub/Sub that are picked up the bot shards that can then send to the servers they are
attached to.

They are published on the "filmlinkd-log-entry-topic" topic.
They are recieved on the "filmlinkd-log-entry-subscription" subscriptions.

We should ack these messages immediatly as there is a general assumption that they'll be repeated
with some frequency.

### Log Entry Result

The bot shards then know when an actual log entry has been written and need to notify the scrapers
but don't have immediate access to them so instead uses Pub/Sub again.

They are published on the "filmlinkd-log-entry-result-topic" topic.
They are recieved on the "filmlinkd-log-entry-result-subscription" subscription.

In hindsight, I'm not sure if the result needs to be in Pub/Sub.
It is used to write to the database and to update the user list cache but I'm not sure if that requires
a whole new queue to take care of.

_I THINK_ the idea was that I only wanted to let the scraper update thier cache after it was successfully
posted. That idea likely was carry over from a pre-VIP scraper where it generally made sense and likely
came from the idea that it was when the database was updated. Then some how those two pieces of logic got
conjoined and because a one thing that happens together so it made to put them behind a Pub/Sub.