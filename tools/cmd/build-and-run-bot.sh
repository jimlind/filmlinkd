#! /bin/bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
mvn package -Pbot && java -jar target/bot.jar