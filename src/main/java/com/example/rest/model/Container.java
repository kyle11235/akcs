package com.example.rest.model;

public class Container {

	private String name;
	private String image;
	private Integer port;
	private TypeEnum type;

	public Container() {

	}

	public Container(String name, TypeEnum type, String image, Integer port) {
		this.name = name;
		this.type = type;
		this.image = image;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public TypeEnum getType() {
		return type;
	}

	public void setType(TypeEnum type) {
		this.type = type;
	}

}
