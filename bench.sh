#!/bin/bash

## The list of benchmarks to run
BENCHMARKS_TO_RUN="${@}"
##  ...or use all the *_bench dirs by default
BENCHMARKS_TO_RUN="${BENCHMARKS_TO_RUN:-$(find . -maxdepth 1 -name '*_bench' -type d | sort)}"

RESULTS_DIR="results/$(date '+%y%d%mT%H%M%S')"
export GRPC_BENCHMARK_DURATION=${GRPC_BENCHMARK_DURATION:-"100s"}
export GRPC_BENCHMARK_WARMUP=${GRPC_BENCHMARK_WARMUP:-"30s"}
export GRPC_SERVER_CPUS=${GRPC_SERVER_CPUS:-"2"}
export GRPC_SERVER_RAM=${GRPC_SERVER_RAM:-"512m"}
# GRPC_CLIENT_CONNECTIONS <= GRPC_CLIENT_CONCURRENCY as GRPC_CLIENT_CONCURRENCY workers share GRPC_CLIENT_CONNECTIONS connection
export GRPC_CLIENT_CONNECTIONS=${GRPC_CLIENT_CONNECTIONS:-"8"}
export GRPC_CLIENT_CONCURRENCY=${GRPC_CLIENT_CONCURRENCY:-"8"}
export GRPC_CLIENT_QPS=${GRPC_CLIENT_QPS:-"0"}
export GRPC_CLIENT_QPS=$(( GRPC_CLIENT_QPS / GRPC_CLIENT_CONCURRENCY ))
export GRPC_CLIENT_CPUS=${GRPC_CLIENT_CPUS:-"16"}
export GRPC_REQUEST_SCENARIO=${GRPC_REQUEST_SCENARIO:-"complex_proto"}
export GRPC_IMAGE_NAME="${GRPC_IMAGE_NAME:-grpc_bench}"
export GRPC_PORT="8090"
# Use output of ifconfig | grep en0 Do not use localhost or 127.0.0.1
export GRPC_SERVER="192.168.1.34"
export NAME="test_grpc"

wait_for_server() {
	for ((i=1;i<=10*30;i++)); do
		nc -z ${GRPC_SERVER} ${GRPC_PORT} && return 0
		sleep .1
	done
	return 1
}


echo "==> Running benchmark for ${NAME}..."

mkdir -p "${RESULTS_DIR}"

# Start the gRPC Server container
#docker run \
#	--name "${NAME}" \
#	--rm \
#	--cpus "${GRPC_SERVER_CPUS}" \
#	--memory "${GRPC_SERVER_RAM}" \
#	-e GRPC_SERVER_CPUS \
#	-e GRPC_SERVER_RAM \
#	-p 50051:50051 \
#	-p 5000:5000 \
#	--detach \
#	--tty \
#	"$GRPC_IMAGE_NAME:${NAME}-$GRPC_REQUEST_SCENARIO" >/dev/null

#printf 'Waiting for server to come up... '
if ! wait_for_server; then
  echo 'server unresponsive!'
  exit 1
fi
echo 'ready.'

# Warm up the service
echo "gRPC Server Warmup..."

docker run --name ghz --rm --network=host -v "${PWD}/proto:/proto:ro" \
    	    -v "${PWD}/payload:/payload:ro" \
    		--cpus $GRPC_CLIENT_CPUS \
    	  obvionaoe/ghz:v0.103.0 \
    		--proto=/proto/scyllaquery.proto \
    		--call=scyllaquery.QueryScylla.ExecuteQuery \
            --insecure \
            --count-errors \
            --enable-compression \
            --concurrency="${GRPC_CLIENT_CONCURRENCY}" \
            --connections="${GRPC_CLIENT_CONNECTIONS}" \
            --rps="${GRPC_CLIENT_QPS}" \
            --duration "${GRPC_BENCHMARK_WARMUP}" \
            --data-file /payload/payload \
    		"${GRPC_SERVER}:${GRPC_PORT}" > /dev/null

# Actual benchmark
echo "Benchmarking now... "

# Start collecting stats
./collect_stats.sh "${NAME}" "${RESULTS_DIR}" &

# Start the gRPC Client
docker run --name ghz --rm --network=host -v "${PWD}/proto:/proto:ro" \
	    -v "${PWD}/payload:/payload:ro" \
		--cpus $GRPC_CLIENT_CPUS \
    obvionaoe/ghz:v0.103.0 \
		--proto=/proto/scyllaquery.proto \
		--call=scyllaquery.QueryScylla.ExecuteQuery \
		    -- async \
        --insecure \
        --concurrency="${GRPC_CLIENT_CONCURRENCY}" \
        --connections="${GRPC_CLIENT_CONNECTIONS}" \
        --rps="${GRPC_CLIENT_QPS}" \
        --duration "${GRPC_BENCHMARK_DURATION}" \
        --data-file /payload/payload \
		"${GRPC_SERVER}:${GRPC_PORT}" >"${RESULTS_DIR}/${NAME}".report

# Show quick summary
cat << EOF
  done.
  Results:
  $(cat "${RESULTS_DIR}/${NAME}".report | grep "Requests/sec" | sed -E 's/^ +/    /')
EOF

kill -INT %1 2>/dev/null
#docker container stop "${NAME}" >/dev/null

if sh analyze.sh $RESULTS_DIR; then
  cat ${RESULTS_DIR}/bench.params
  echo "All done."
else
  echo "Analysis fiascoed."
  ls -lha $RESULTS_DIR
  for f in $RESULTS_DIR/*; do
  	echo
  	echo
  	echo "$f"
	  cat "$f"
  done
  exit 1
fi
