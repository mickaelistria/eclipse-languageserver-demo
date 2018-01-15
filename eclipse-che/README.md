## build workspace image

```
$ cd chamrousse-workspace-image
$ mvn clean install
```

it will build local docker image

```
[INFO] DOCKER> [florentbenoit/chamrousse:latest]: Created docker-build.tar in 65 milliseconds
[INFO] DOCKER> [florentbenoit/chamrousse:latest]: Built image sha256:a48db
```

## Start Eclipse Che
Then we can start Eclipse Che with script start-che.sh
the script includes parameter CHE_DOCKER_ALWAYS__PULL__IMAGE="false" to not try to pull image from dockerhub at each start (helpful if internet access is down)

## Build stack
Once che is started and available at http://localhost:8080 we add the chamrousse stack from http://localhost:8080/dashboard/#/stacks

"Build stack from recipe"

we select `docker image` tab and enter `florentbenoit/chamrousse`

we pickup a name like "A chamrousse stack" and save

## Create and start workspace
From http://localhost:8080/dashboard/#/workspaces
Select `Add workspace` and pickup "A chamrousse stack` from the list

## Create a project
Once workspace is booted
Create a project with menu "Workspace/Create project..."
Select blank type and enter project name like "Chamrousse"

## Create a file
Right click on "chamrousse" project and select "New..." and "Chamrousse File"

pickup dummy name like example
Use ctrl+tab in the ski file and there is code assist
