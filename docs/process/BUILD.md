Build individual jar files and execute
> mvn package -Padmin && java -jar target/admin.jar
> mvn package -Pbot && java -jar target/bot.jar
> mvn package -Pscraper && java -jar target/scraper.jar

Build docker image (for bot) and execute. Image is stored in Docker daemon not in build directory.
> mvn compile jib:dockerBuild && docker run --rm filmlinkd

