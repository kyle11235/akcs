package com.example.rest.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.ApplicationHome;

import com.example.rest.App;

public class Config {

	public static final String ENV_AKCS_HOME = "ENV_AKCS_HOME";
	public static final String DIR_AKCS = ".akcs";
	public static final String CONFIG_FILE = "config.properties";
	public static final String BUILDER_FOLDER_NAME = "builder";
	public static final String UPLOAD_NAME = "app";
	public static final String JAR_NAME = "akcs.jar";
	
	public static File AKCS_HOME;
	public static File BUILDER;
	public static File APPS;

	private static Properties pps;

	public static synchronized void init() {
		if (pps == null) {
			pps = new Properties();
			try {
				String akcsHome = System.getenv(ENV_AKCS_HOME);
				System.out.println(ENV_AKCS_HOME + "=" + akcsHome);

				if (akcsHome != null) {
					AKCS_HOME = new File(akcsHome);
				} else {
					AKCS_HOME = new File(FileUtils.getUserDirectory(), DIR_AKCS);
				}

				System.out.println("AKCS home will be " + AKCS_HOME.getAbsolutePath());
				if (!AKCS_HOME.exists()) {
					AKCS_HOME.mkdir();
				}
				
				// read config
				pps.load(new FileInputStream(AKCS_HOME + File.separator + CONFIG_FILE));

				// refresh builder files
				BUILDER = new File(AKCS_HOME, BUILDER_FOLDER_NAME);
				FileUtils.deleteDirectory(BUILDER);

				// for dev mode
				ApplicationHome appHome = new ApplicationHome(App.class);
				System.out.println("akcs is started at " + appHome.getDir().getAbsolutePath());
				
				File builderSrc = new File(appHome.getDir(), BUILDER_FOLDER_NAME);
				if (!builderSrc.exists()) {
					// for production mode
					File war = new File(appHome.getDir(), JAR_NAME);
					File tmp = FileUtils.getTempDirectory();
					UnzipUtil.unzip(war.getAbsolutePath(), tmp.getAbsolutePath());
					builderSrc = new File(tmp, "WEB-INF/classes/" + BUILDER_FOLDER_NAME);
				}

				System.out.println("builder files are copied to " + BUILDER.getAbsolutePath());
				FileUtils.copyDirectory(builderSrc, BUILDER);

				// apps
				APPS = new File(AKCS_HOME, "apps");
				if (!APPS.exists()) {
					APPS.mkdir();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String getValue(String key) {
		if (pps == null) {
			init();
		}
		return pps.getProperty(key);
	}

}
