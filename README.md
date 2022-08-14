1. Set Java version
```aidl
jenv shell 1.8
```
2. Compile
```
jenv shell 11
cd server
mvn clean install
```

2. Gradle. Only work with Java 1.8 for now
```
jenv shell 1.8
cd server
./gradlew runServer
```