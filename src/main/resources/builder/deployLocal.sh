DOCKER=$1
APP_NAME=$2
EXPOSE_PORT=$3
INTERNAL_PORT=$4
IMAGE=$5


SHELL_DIR=$(dirname "$BASH_SOURCE")
APP_DIR=$(cd $SHELL_DIR/../..; pwd)


SHELL_DIR=$(dirname "$BASH_SOURCE")
APP_DIR=$(cd $SHELL_DIR; pwd)

if [ "$(docker ps -aq -f name=$APP_NAME)" ]; then
	# stop and run
	$DOCKER stop $APP_NAME && $DOCKER rm $APP_NAME && $DOCKER run --name $APP_NAME -d -p $EXPOSE_PORT:$INTERNAL_PORT $IMAGE
else
	# run
	$DOCKER run --name $APP_NAME -d -p $EXPOSE_PORT:$INTERNAL_PORT $IMAGE
fi
