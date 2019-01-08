package com.example.rest.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.rest.model.Container;
import com.example.rest.util.Shell;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1beta1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.AppsV1beta1Deployment;
import io.kubernetes.client.models.AppsV1beta1DeploymentSpec;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LabelSelector;
import io.kubernetes.client.models.V1LocalObjectReference;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodTemplateSpec;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
import io.kubernetes.client.models.V1ServicePort;
import io.kubernetes.client.models.V1ServiceSpec;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.util.Config;

@Service(value = "apiService")
public class APIServiceImpl implements APIService {

	private static AppsV1beta1Api appsV1betaApi;
	private static CoreV1Api coreV1Api;
	private static ExtensionsV1beta1Api extensionV1betaApi;

	@Value("${pretty}")
	private String pretty;

	@Value("${isPrivateRegistry}")
	private Boolean isPrivateRegistry;

	@Value("${registrySecret}")
	private String registrySecret;

	@Value("${labelName}")
	private String labelName;

	@Value("${kubectlPath}")
	private String kubectlPath;

	static {
		try {
			ApiClient client = Config.defaultClient();
			appsV1betaApi = new AppsV1beta1Api(client);
			coreV1Api = new CoreV1Api(client);
			extensionV1betaApi = new ExtensionsV1beta1Api(client);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("got API client exception");
		}

	}

	@Override
	public void createDeployment(String namespace, Container container) throws Exception {

		AppsV1beta1Deployment body = this.prepareDeployment(container);

		try {
			System.out.println("start createDeployment=" + container.getName());
			AppsV1beta1Deployment result = appsV1betaApi.createNamespacedDeployment(namespace, body, pretty);
			System.out.println("deployment is created");
		} catch (ApiException e) {
			e.printStackTrace();
			System.out.println(e.getResponseBody());
			throw new Exception("failed to create deployment");
		}

	}

	@Override
	public void replaceDeployment(String namespace, Container container) throws Exception {

		AppsV1beta1Deployment body = this.prepareDeployment(container);

		try {
			System.out.println("start replaceDeployment=" + container.getName());

//			replace does not work
//			AppsV1beta1Deployment result = appsV1betaApi.replaceNamespacedDeployment(container.getName(), namespace, body, pretty);

			// delete + create offers more flexibility (no need to match exact deployment labels)
			this.deleteDeployment(namespace, container.getName());
			this.createDeployment(namespace, container);

			System.out.println("redeployment is created");
		} catch (ApiException e) {
			e.printStackTrace();
			System.out.println(e.getResponseBody());
			throw new Exception("failed to redeploy");
		}

	}

	private AppsV1beta1Deployment prepareDeployment(Container container) {
		// 1. prepare

		// deployment
		AppsV1beta1Deployment body = new AppsV1beta1Deployment();
		V1ObjectMeta deployMetadata = new V1ObjectMeta();

		// deployment/spec
		AppsV1beta1DeploymentSpec deploySpec = new AppsV1beta1DeploymentSpec();

		// deployment/spec/selector
		V1LabelSelector selector = new V1LabelSelector();
		Map<String, String> matchLabels = new HashMap<String, String>();

		// deployment/spec/template
		V1PodTemplateSpec template = new V1PodTemplateSpec();
		V1ObjectMeta templateMetadata = new V1ObjectMeta();
		Map<String, String> labels = new HashMap<String, String>();

		// deployment/spec/template/spec
		V1PodSpec podSpec = new V1PodSpec();

		// deployment/spec/template/spec/secret
		List<V1LocalObjectReference> imagePullSecrets = new LinkedList<V1LocalObjectReference>();
		V1LocalObjectReference v1LocalObjectReference = new V1LocalObjectReference();
	
		if(isPrivateRegistry) {
			imagePullSecrets = new LinkedList<V1LocalObjectReference>();
			v1LocalObjectReference = new V1LocalObjectReference();
		}

		// deployment/spec/template/spec/containers
		List<V1Container> containers = new LinkedList<V1Container>();
		V1Container v1container = new V1Container();
		List<V1ContainerPort> ports = new LinkedList<V1ContainerPort>();
		V1ContainerPort v1containerPort = new V1ContainerPort();

		// 2. set

		// deployment
		body.setMetadata(deployMetadata);
		deployMetadata.setName(container.getName());
		body.setSpec(deploySpec);

		// deployment/spec

		// deployment/spec/selector
		deploySpec.setSelector(selector);
		selector.setMatchLabels(matchLabels);
		matchLabels.put(labelName, container.getName());
		matchLabels.put("type", container.getType().toString());

		// deployment/spec/template
		deploySpec.setTemplate(template);
		template.setMetadata(templateMetadata);
		templateMetadata.setLabels(labels);
		labels.put(labelName, container.getName());
		labels.put("type", container.getType().toString());

		// deployment/spec/template/spec
		template.setSpec(podSpec);

		// deployment/spec/template/spec/secret
		if(isPrivateRegistry) {
			podSpec.setImagePullSecrets(imagePullSecrets);
			v1LocalObjectReference.setName(registrySecret);
			imagePullSecrets.add(v1LocalObjectReference);
		}
		
		// deployment/spec/template/spec/containers
		podSpec.setContainers(containers);
		containers.add(v1container);
		v1container.setName(container.getName());
		v1container.setImage(container.getImage());
		v1container.setPorts(ports);
		ports.add(v1containerPort);
		v1containerPort.setContainerPort(container.getPort());

		return body;
	}

	@Override
	public void deleteDeployment(String namespace, String name) throws Exception {

		V1DeleteOptions body = new V1DeleteOptions();
		Integer gracePeriodSeconds = 0;
		Boolean orphanDependents = null;
		String propagationPolicy = "Foreground";
		try {
			System.out.println("start deleteDeployment=" + name);

			// policy Foreground should delete underlying resources(replicaSet and pods) but does not work
			
//			V1Status result = appsV1betaApi.deleteNamespacedDeployment(name, namespace, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy);
//			extensionV1betaApi.deleteNamespacedDeployment(name, namespace, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy);

			String[] params = {"delete", "deployment", name, "-n", namespace};
			Shell.executeParams(kubectlPath, params);
			
			System.out.println("deployment is deleted");
		} catch (Exception e) {
			e.printStackTrace();
			// catch Exception because delete will be successful but still exception parsing
			// json result - Expected a string but was BEGIN_OBJECT at line 1 column 1273
			// path $.status
			if (!e.getClass().getName().equals("com.google.gson.JsonSyntaxException")) {
				throw new Exception("failed to delete deployment");
			}
		}

	}

	@Override
	public Integer createService(String namespace, Container container) throws Exception {

		// 1. prepare

		// service
		V1Service body = new V1Service();
		V1ObjectMeta serviceMetadata = new V1ObjectMeta();
		Map<String, String> labels = new HashMap<String, String>();

		// service/spec
		V1ServiceSpec spec = new V1ServiceSpec();

		// service/spec/selector
		Map<String, String> selector = new HashMap<String, String>();

		// service/spec/ports
		List<V1ServicePort> ports = new LinkedList<V1ServicePort>();
		V1ServicePort port = new V1ServicePort();

		// 2. set

		// service
		body.setMetadata(serviceMetadata);
		serviceMetadata.setName(container.getName());
		labels.put(labelName, container.getName());
		labels.put("type", container.getType().toString());
		serviceMetadata.setLabels(labels);

		// service/spec
		body.setSpec(spec);
		spec.setType("NodePort");

		// service/spec/selector
		spec.setSelector(selector);
		selector.put(labelName, container.getName());

		// service/spec/ports
		spec.setPorts(ports);
		ports.add(port);
		port.setPort(8080);
		port.setTargetPort(new IntOrString(container.getPort())); // port of container

		try {
			System.out.println("start createService=" + container.getName());
			V1Service result = coreV1Api.createNamespacedService(namespace, body, pretty);
			System.out.println("service is created");
			return result.getSpec().getPorts().get(0).getNodePort();
		} catch (ApiException e) {
			e.printStackTrace();
			System.out.println(e.getResponseBody());
			throw new Exception("failed to expose deployment");
		}
	}

	@Override
	public Integer getService(String namespace, String labelSelector) {
		try {
			V1ServiceList list = coreV1Api.listNamespacedService(namespace, pretty, null, null, null, labelSelector, null, null, null, null);
			if (list.getItems().size() > 0) {
				V1Service service = list.getItems().get(0);
				System.out.println("start getService=" + labelSelector);
				return service.getSpec().getPorts().get(0).getNodePort();
			}
		} catch (ApiException e) {
			e.printStackTrace();
			System.out.println(e.getResponseBody());
		}
		return null;
	}

	@Override
	public V1ServiceList listServices(String namespace, String labelSelector) {
		try {
			return coreV1Api.listNamespacedService(namespace, pretty, null, null, null, labelSelector, null, null, null, null);
		} catch (ApiException e) {
			e.printStackTrace();
			System.out.println(e.getResponseBody());
		}
		return null;
	}

	@Override
	public void deleteService(String namespace, String name) throws Exception {
		V1DeleteOptions body = new V1DeleteOptions();
		Integer gracePeriodSeconds = 0;
		Boolean orphanDependents = null;
		String propagationPolicy = "Orphan";
		try {
			System.out.println("start deleteService=" + name);
			V1Status result = coreV1Api.deleteNamespacedService(name, namespace, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy);
			System.out.println("service is deleted");
		} catch (ApiException e) {
			e.printStackTrace();
			System.out.println(e.getResponseBody());
			throw new Exception("failed to delete deployment");
		}
	}

	@Override
	public String getPod(String namespace, String labelSelector) {
		try {
			V1PodList list = coreV1Api.listNamespacedPod(namespace, pretty, null, null, null, labelSelector, null, null, null, null);
			if (list.getItems().size() > 0) {
				V1Pod pod = list.getItems().get(0);
				System.out.println("start getPod=" + labelSelector);
				return pod.getSpec().getNodeName();
			}
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public V1PodList listPods(String namespace, String labelSelector) {
		try {
			return coreV1Api.listNamespacedPod(namespace, pretty, null, null, null, labelSelector, null, null, null, null);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {

		APIServiceImpl api = new APIServiceImpl();

		// sample
		String namespace = "kyle";
		String name = "app=akcs-ui";
//		String image = "gcr.io/google-samples/node-hello:1.0";
		String image = "ixx.oxxx.io/xxx14401/do_not_delete/hello:latest";

		// test
//		String IP = api.getPod(namespace, name);
//		Integer nodePort = api.getService(namespace, name);
//		System.out.println("URL=" + IP + ":" + nodePort);

		api.deleteDeployment(namespace, "simple-jsp-app");
	}

}
