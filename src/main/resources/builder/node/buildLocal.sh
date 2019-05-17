

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
for file in $APP_DIR/*; do
  if [ $file != "$APP_DIR/builder" ]
    then
      cp -r $file $SHELL_DIR/tmp/
  fi
done 

printf "\nbuild image...\n\n"
$DOCKER build -t $IMAGE $SHELL_DIR







