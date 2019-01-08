

DOCKER=$1
REGISTRY_HOST=$2
REGISTRY_USER=$3
REGISTRY_PASS=$4
APP_NAME=$5
IMAGE=$6

SHELL_DIR=$(dirname "$BASH_SOURCE")
APP_DIR=$(cd $SHELL_DIR/../..; pwd)

printf "\nremove image...\n\n"
$DOCKER rmi $IMAGE

printf "\nclean tmp...\n\n"
rm -rf $SHELL_DIR/tmp
mkdir $SHELL_DIR/tmp

printf "\ncopy app...\n\n"
cp -r $APP_DIR/app.jar $SHELL_DIR/tmp/

printf "\nbuild image...\n\n"
$DOCKER build -t $IMAGE $SHELL_DIR

printf "\nlogin...\n\n"
$DOCKER login --username=$REGISTRY_USER --password=$REGISTRY_PASS $REGISTRY_HOST

printf "\npush image...\n\n"
$DOCKER push $IMAGE






