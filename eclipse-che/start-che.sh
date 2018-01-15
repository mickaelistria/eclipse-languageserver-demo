#!/bin/sh
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -e CHE_DOCKER_ALWAYS__PULL__IMAGE="false" -v /tmp/da6:/data florentbenoit/che-server:chamrousse-demo start --fast
