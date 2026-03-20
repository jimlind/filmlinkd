#! /bin/bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
mvn package -Pscraper && java -jar target/scraper.jar