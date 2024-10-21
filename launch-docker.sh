#!/bin/bash

# Check if Docker is running, if not exit the script
checkDocker() {
    if ! docker info >/dev/null 2>&1; then
        echo "Error: Docker is not running. Please start Docker first."
        exit 1
    fi
    echo "Docker is running!"
}

echo "Checking Docker..."
checkDocker

# Move into current script's directory.
PROJ_ROOT="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
cd "$PROJ_ROOT" || exit  # cd current directory

cd "instance1" || exit
USER=$(id -u):$(id -g) DB_PATH=$PROJ_ROOT/instance1/db APP_PATH=$PROJ_ROOT/instance1/app docker compose up -d

echo "Doing Heavy lifting, please stand by ('-')7"

sleep 120

docker exec -ti phylodb-db-1 bash -c "cypher-shell -u neo4j -p password < /scripts/init/init_schema.cypher"
docker exec -ti phylodb-db-1 bash -c "cypher-shell -u neo4j -p password < /scripts/init/init_data.cypher"

cd ..
