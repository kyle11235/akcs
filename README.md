
# AKCS (Application K8S Cloud Service)

AKCS (Application K8S Cloud Service) enables you to deploy your traditional apps as many as you want in an easy way with backed k8s.

### dev & test

	1. install k8s & kubectl & docker (better to as root)
	place kubeconfig file as ~/.kube/config
	
	2. if use private registry, create secret in k8s (notice the namespace), secret will be used in deployment for pulling image
	kubectl create secret docker-registry akcs --docker-server=ixx.oxxx.io --docker-username='$CLOUD_DOMAIN/$USERNAME' --docker-password='$AUTH_TOKEN' --docker-email='xxx@xxx.com' -n kyle

	3. install dependency
	wget https://github.com/kubernetes-client/java/archive/client-java-parent-2.0.0.zip
	unzip java-client-java-parent-2.0.0.zip
	cd java-client-java-parent-2.0.0
	mvn install
	
	4. start akcs server
	config application.properties
	cd akcs
	mvn spring-boot:run or run AppWar directly from IDE
	localhost:8080
	
	5. start akcs ui
	config request url to akcs in session.js
	install oracle JET
	cd akcs/ui
	npm install
	ojet serve

### package & deploy

	1. deploy akcs server
	do config application.properties !!!
	cd akcs
	mvn clean package
	nohup java -Xms4096m -Xmx4096m -jar akcs.war > out.log 2>&1 & (Optional: -Dserver.port=9090)
	localhost:8080

	2. deploy akcs ui	
	config request url to akcs in session.js
	cd akcs/ui/web
	zip -r ui.zip .
	deploy ui.zip by akcs-ip:8080/upload.html

### release

- 2018-08-17 - beta1
- 2018-08-19 - add update function, enabling cluster by removing state of akcs server

### todo

- native k8s
- log
- account



