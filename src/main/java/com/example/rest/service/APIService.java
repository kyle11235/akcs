package com.example.rest.service;

import com.example.rest.model.Container;

import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1ServiceList;

public interface APIService {

	public void createDeployment(String namespace, Container container) throws Exception;

	public void replaceDeployment(String namespace, Container container) throws Exception;

	public void deleteDeployment(String namespace, String name) throws Exception;

	public Integer createService(String namespace, Container container) throws Exception;

	public Integer getService(String namespace, String labelSelector);

	public V1ServiceList listServices(String namespace, String labelSelector);

	public void deleteService(String namespace, String name) throws Exception;

	public String getPod(String namespace, String labelSelector);

	public V1PodList listPods(String namespace, String labelSelector);

}
