#!/bin/bash

# Move into current script's directory.
PROJ_ROOT="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
cd "$PROJ_ROOT" || exit  # cd current directory

cd "instance1" || exit
USER=$(id -u):$(id -g) DB_PATH=$PROJ_ROOT/instance1/db APP_PATH=$PROJ_ROOT/instance1/app docker compose up -d

docker exec -ti phylodb-db-1 bash -c "cypher-shell -u neo4j -p password < /scripts/init/init_schema.cypher"
docker exec -ti phylodb-db-1 bash -c "cypher-shell -u neo4j -p password < /scripts/init/init_data.cypher"

cd ..
