1. Start Scylla cluser 3 nodes
```
If need, build again
rm -rf ~/scylla
cd docker
./setupScyllaCluster.sh
```

2. Set Java version
```
jenv shell 1.8
```

3. Compile Client by Maven then refresh to prevent IDE error.
```
cd server
mvn clean install
```

4. Compile and start Server by Gradle. Only work with Java 1.8 for now
```
cd server
./gradlew runServer
```

5. Test Server by command-line. Note that we have to enable service ProtoReflectionService
```
grpcurl --plaintext -d '{"key":"0", "start":1000000}' localhost:8090 scyllaquery.QueryScylla/ExecuteQuery
```

6. Compile and start Client by Gradle. Only work with Java 1.8 for now
```
jenv shell 1.8
cd client_java

./gradlew clientRunner

./gradlew multiClientRunner
```

7. Stress test
```
./bench.sh
```

OTHER TECH

1. Scala: Akka Grpc
```aidl
cd server_scala
sbt clean compile
sbt "runMain grpc.scala.server.GreeterServer"
```