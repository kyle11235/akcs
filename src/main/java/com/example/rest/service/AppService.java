package com.example.rest.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.rest.model.App;
import com.example.rest.model.TypeEnum;

public interface AppService {

	public List<App> listApps();
	
	public void createApp(String name, TypeEnum type, MultipartFile file) throws Exception;

	public void updateApp(String name, TypeEnum type, MultipartFile file) throws Exception;
}
