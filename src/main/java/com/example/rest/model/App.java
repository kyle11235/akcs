package com.example.rest.model;

import java.io.File;

public class App {

	private String name;
	private TypeEnum type;
	private String url;
	private File savedFile;
	private File appFile;
	private File builderFile;

	public App() {

	}

	public App(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TypeEnum getType() {
		return type;
	}

	public void setType(TypeEnum type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getSavedFile() {
		return savedFile;
	}

	public void setSavedFile(File savedFile) {
		this.savedFile = savedFile;
	}

	public File getAppFile() {
		return appFile;
	}

	public void setAppFile(File appFile) {
		this.appFile = appFile;
	}

	public File getBuilderFile() {
		return builderFile;
	}

	public void setBuilderFile(File builderFile) {
		this.builderFile = builderFile;
	}

}
