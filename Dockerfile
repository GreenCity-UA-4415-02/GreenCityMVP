FROM openjdk:21-ea-21-slim AS runner
WORKDIR runner
COPY **/target/app.jar runner/
CMD java -jar runner/app.jar 
