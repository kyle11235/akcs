package com.example.rest.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.rest.Config;
import com.example.rest.model.App;
import com.example.rest.model.Container;
import com.example.rest.model.TypeEnum;
import com.example.rest.util.Shell;
import com.example.rest.util.UnzipUtil;

import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;

@Service(value = "appService")
public class AppServiceImpl implements AppService {

	@Autowired
	private APIService apiService;

	@Value("${dockerPath}")
	private String dockerPath;

	@Value("${registryRepository}")
	private String registryRepository;

	@Value("${registryHost}")
	private String registryHost;

	@Value("${registryUsername}")
	private String registryUsername;

	@Value("${registryPassword}")
	private String registryPassword;

	@Value("${donotDeploy}")
	private Boolean donotDeploy;

	@Value("${namespace}")
	private String namespace;

	@Value("${labelName}")
	private String labelName;

	@Override
	public List<App> listApps() {

		List<App> out = new LinkedList<App>();

		// all services
		V1ServiceList services = apiService.listServices(namespace, labelName);
		Map<String, V1Service> serviceMap = new HashMap<String, V1Service>();
		if(services != null) {
			for (V1Service service : services.getItems()) {
				serviceMap.put(service.getMetadata().getName(), service);
			}

			// create apps based on pods that has label app
			V1PodList pods = apiService.listPods(namespace, labelName);
			for (V1Pod pod : pods.getItems()) {
				String name = pod.getMetadata().getLabels().get(labelName);
				App app = new App(name);
				String type = pod.getMetadata().getLabels().get("type");
				if (type != null) {
					app.setType(TypeEnum.valueOf(type));
				}
				app.setUrl(pod.getSpec().getNodeName());
				V1Service service = serviceMap.get(name);
				if (service != null) {
					app.setUrl(app.getUrl() + ":" + service.getSpec().getPorts().get(0).getNodePort());
				}
				out.add(app);
			}
		}
		
		return out;
	}

	@Override
	public void createApp(String name, TypeEnum type, MultipartFile file) throws Exception {

		System.out.println("start createApp=" + name + ",type=" + type);

		// check name
		Pattern p = Pattern.compile("^(?![0-9]+$)(?!-)[a-zA-Z0-9%-]{1,63}(?!-)$");
		Matcher m = p.matcher(name);
		if (!m.matches()) {
			throw new Exception("name " + name + " is invalid");
		}

		// check if name is in use
		if (apiService.getPod(namespace, labelName + "=" + name) != null) {
			throw new Exception("name " + name + " is already in use");
		}

		// prepare app
		App app = this.prepareApp(name, type, file);

		// build & push
		Container container = this.prepareContainer(app);

		// deploy
		if (!donotDeploy) {
			apiService.createDeployment(namespace, container);
			apiService.createService(namespace, container);
		}

		System.out.println("finished createApp=" + name + ",type=" + type);
	}

	// replace deployment only, keep existing service
	@Override
	public void updateApp(String name, TypeEnum type, MultipartFile file) throws Exception {

		System.out.println("start updateApp=" + name + ",type=" + type);

		// check if name exists
		if (apiService.getPod(namespace, labelName + "=" + name) == null) {
			throw new Exception("name " + name + " does not exist");
		}

		// prepare app
		App app = this.prepareApp(name, type, file);

		// build & push
		Container container = this.prepareContainer(app);

		// deploy
		if (!donotDeploy) {
			apiService.replaceDeployment(namespace, container);
		}

		System.out.println("finished updateApp=" + name + ",type=" + type);

	}

	private App prepareApp(String name, TypeEnum type, MultipartFile file) throws Exception {
		// check file
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		System.out.println("original filename=" + fileName);

		if (fileName.contains("..")) {
			// This is a security check
			throw new Exception("cannot store file " + fileName + " with relative path outside current directory ");
		}

		if (file.isEmpty()) {
			throw new Exception("file " + fileName + " is empty ");
		}

		// prepare app file, delete in case action happens in the same akcs instance
		File appFile = new File(Config.APPS, name);
		FileUtils.deleteDirectory(appFile);
		appFile.mkdir();

		// save package
		File savedFile = null;
		try {
			fileName = Config.UPLOAD_NAME + fileName.substring(fileName.lastIndexOf("."), fileName.length());
			System.out.println("saved filename=" + fileName);
			savedFile = new File(appFile, fileName);
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new Exception("failed to store file " + fileName, e);
		}

		// copy builder file
		File builderFile = new File(appFile, Config.BUILDER_FOLDER_NAME);
		FileUtils.copyDirectory(Config.BUILDER, builderFile);

		App app = new App();
		app.setName(name);
		app.setType(type);
		app.setSavedFile(savedFile);
		app.setAppFile(appFile);
		app.setBuilderFile(builderFile);

		return app;
	}

	private Container prepareContainer(App app) throws IOException {
		File shellFile = null;
		Integer port = null;
		if (app.getType().equals(TypeEnum.html)) {
			UnzipUtil.unzip(app.getSavedFile().getAbsolutePath(), app.getAppFile().getAbsolutePath());
			app.getSavedFile().delete();
			shellFile = new File(app.getBuilderFile(), "html/build.sh");
			port = 80;
		}
		if (app.getType().equals(TypeEnum.node)) {
			UnzipUtil.unzip(app.getSavedFile().getAbsolutePath(), app.getAppFile().getAbsolutePath());
			app.getSavedFile().delete();
			shellFile = new File(app.getBuilderFile(), "node/build.sh");
			port = 8080;
		}
		if (app.getType().equals(TypeEnum.jar)) {
			shellFile = new File(app.getBuilderFile(), "java/build.sh");
			port = 8080;
		}
		if (app.getType().equals(TypeEnum.war)) {
			shellFile = new File(app.getBuilderFile(), "tomcat/build.sh");
			port = 8080;
		}

		String image = registryRepository + "/" + app.getName() + ":latest";
		Files.setPosixFilePermissions(shellFile.toPath(), PosixFilePermissions.fromString("rwxrwxrwx"));
		String[] params = { dockerPath, registryHost, registryUsername, registryPassword, app.getName(), image };
		Shell.executeParams(shellFile.getAbsolutePath(), params);

		return new Container(app.getName(), app.getType(), image, port);
	}

}
