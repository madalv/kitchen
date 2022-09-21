## Steps to run Kitchen


1. Create fatJar.

Step 1 can be done either by 
- uncommenting the line in the Dockerfile, 
- or, if you don't want to suffer, create it yourself before calling docker compose.
Creating a fatJar can be done by:
```
gradlew buildFatJar
```
Or, just use IntelliJ IDEA and execute the Gradle task there.

2. Run the command below:

```
docker compose up --build
```

## Other notes

If you want to run it on your local machine, set the hostname in the requests to `localhost` and run `Application.kt`. The ports don't need to be changed.
