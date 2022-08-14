1. Start Scylla cluser 3 nodes
```
cd docker
./setupScyllaCluster.sh
```

2. Set Java version
```
jenv shell 1.8
```

3. Compile Server by Maven. Can use Java 11
```
jenv shell 11
cd server
mvn clean install
```

4. Compile and start Server by Gradle. Only work with Java 1.8 for now
```
jenv shell 1.8
cd server
./gradlew runServer
```