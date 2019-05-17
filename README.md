
# AKCS (Application K8S Cloud Service)

AKCS (Application K8S Cloud Service) enables you to deploy your traditional apps as many as you want in an easy way with backed k8s.

## config

    export ENV_AKCS_HOME=/Users/kyle/workspace/akcs/akcshome
    OR put akcs/akcshome/config.properties into ~/.akcs without env variable set

## run immediately

- start api server
    update docker path in config.properties
    ./3_run.sh

- start ui
    ./ui/3_run.sh
    http://localhost:8000

## develop

- k8s mode (optional)

    1. install k8s & kubectl & cd lib && ./install_sdk.sh
    place kubeconfig file as ~/.kube/config

    2. if use private registry, create secret in k8s (notice the namespace), secret will be used in deployment for pulling image
    kubectl create secret docker-registry akcs --docker-server=ixx.oxxx.io --docker-username='$CLOUD_DOMAIN/$USERNAME' --docker-password='$AUTH_TOKEN' --docker-email='xxx@xxx.com' -n kyle

    3. update config.properties

- start api server
    cd akcs
    mvn spring-boot:run

- start ui
    install oracle JET
    cd akcs/ui
    npm install
    ojet serve
    http://localhost:8000

### deploy

- deploy akcs server
    cd akcs
    mvn clean package
    nohup java -Xms4096m -Xmx4096m -jar akcs.jar > out.log 2>&1 & (Optional: -Dserver.port=9090)
    localhost:8080

- deploy ui
    config request url to akcs in session.js
    cd akcs/ui/web
    zip -r ui.zip .
    deploy ui.zip by akcs-ip:8080/upload.html

### release

- 2018-08-17 - beta1
- 2018-08-19 - add update function, enabling cluster by removing state of akcs server
- 2019-05-17 - local mode

### todo

- docker in docker
- local mode - port check/list apps/docker ui
- document
- native k8s
- log
