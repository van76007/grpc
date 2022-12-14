#!/bin/bash

LOCATION="${HOME}/scylla"
IMAGE=scylladb/scylla
PORT=9042

recreate_persistent_store() {
    for d in ${LOCATION}/mapped/node{1,2,3}; do
        echo "Re-creating $d"
        rm -rf $d
        mkdir $d
    done
}

stop_and_remove_container() {
    echo "Stopping and removing container $1"
    docker stop $1
    docker rm $1
}

stop_and_remove_all_containers() {
    for c in $(docker ps --all | awk '{print $1"_"$2;}'); do
        if [ "$c" != "CONTAINER_ID" ]; then
          if [[ ${c:0:21} == *"scylla"* ]]; then
	        stop_and_remove_container ${c:0:12}
	      fi
        fi
    done
}

run_scylla() {
    docker run --security-opt seccomp=unconfined --name "scylla-node$1" --publish $2:${PORT} \
               --volume ${LOCATION}/mapped/node$1:/var/lib/scylla \
               --volume ${LOCATION}/scylla.yaml:/etc/scylla/scylla.yaml \
               --detach ${IMAGE} --smp 4 --memory 8G
}

append_scylla() {
    docker run --security-opt seccomp=unconfined --name "scylla-node$1" --publish $2:${PORT} \
               --volume ${LOCATION}/mapped/node$1:/var/lib/scylla \
               --volume ${LOCATION}/scylla.yaml:/etc/scylla/scylla.yaml \
               --detach ${IMAGE} --smp 4 --memory 8G \
               --seeds="$(docker inspect --format='{{ .NetworkSettings.IPAddress }}' scylla-node1)"
}

# Delete any previous docker setup
if [ -d ${LOCATION} ]; then
  stop_and_remove_all_containers
  echo "recreate persistent store"
  recreate_persistent_store
fi

# Prepare
if [ ! -d ${LOCATION} ]; then
    echo "First time preparations"
    mkdir ${LOCATION}
    docker pull scylladb/scylla
    echo "Finish pull scylla"
    docker run scylladb/scylla just-some-unrecognised-argument > /dev/null 2>&1
    echo "Finish dry run scylla"
    docker cp $(docker ps -lq):/etc/scylla/scylla.yaml ${LOCATION}/scylla.yaml
    echo "Finish copy scylla.yaml"
    docker rm $(docker ps -lq)
    echo "Finish rm docker"
    cat >> ${LOCATION}/scylla.yaml <<EOF

authenticator: 'com.scylladb.auth.TransitionalAuthenticator'
authorizer: 'com.scylladb.auth.TransitionalAuthorizer'

#

EOF

fi

# May be a bit drastic in general, but uncomment if you want to always start from known ground
#stop_and_remove_all_containers

#
echo "Start 1st node"
run_scylla 1 9042

# Await the first node coming up
TIME=45
echo "Waiting for the first node to come up (${TIME} sec)"
sleep ${TIME}

# CREATE INDEX ON history(key);

docker exec -i scylla-node1 cqlsh -ucassandra -pcassandra <<EOF
CREATE KEYSPACE demo WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 } AND DURABLE_WRITES=true;
USE demo;
CREATE TABLE IF NOT EXISTS history (
    occasion    TIMESTAMP,
    key VARCHAR,
    value VARCHAR,
    PRIMARY KEY (occasion, key)
);

INSERT INTO history (
    occasion,
    key,
    value
) VALUES (
    toTimeStamp(now()),
    '0',
    '10'
);

INSERT INTO history (
    occasion,
    key,
    value
) VALUES (
    toTimeStamp(now()),
    '0',
    '20'
);

INSERT INTO history (
    occasion,
    key,
    value
) VALUES (
    toTimeStamp(now()),
    '1',
    '11'
);

INSERT INTO history (
    occasion,
    key,
    value
) VALUES (
    toTimeStamp(now()),
    '1',
    '21'
);

INSERT INTO history (
    occasion,
    key,
    value
) VALUES (
    toTimeStamp(now()),
    '2',
    '32'
);

INSERT INTO history (
    occasion,
    key,
    value
) VALUES (
    toTimeStamp(now()),
    '2',
    '12'
);

SELECT * FROM history;
exit;
EOF

docker exec -i scylla-node1 nodetool status demo

#
append_scylla 2 9043
append_scylla 3 9044

# Give the next to nodes some leeway
echo "Wait for the next two nodes to come up (${TIME} sec)"
sleep ${TIME}

docker exec -i scylla-node1 nodetool status demo

echo done