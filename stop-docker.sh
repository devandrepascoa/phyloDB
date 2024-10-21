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
pushd "$PROJ_ROOT"  # cd current directory

#newgrp docker

echo "> Stopping phyloDB Docker database container..."
docker container stop phylodb-sourcegit_db_1

echo ""
echo ""

echo "> Stopping phyloDB Docker app container..."
docker container stop phylodb-sourcegit_app_1


# Change back to original group ID.
#newgrp

popd