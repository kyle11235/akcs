package com.example.rest;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.ApplicationHome;

import com.example.rest.util.UnzipUtil;

public class Config {

	public static final String ENV_AKCS_HOME = "AKCS_HOME";
	public static final String DIR_AKCS = ".akcs";
	public static final String WAR_NAME = "akcs.war";
	public static final String BUILDER_FOLDER_NAME = "builder";
	public static final String UPLOAD_NAME = "app";
	public static File AKCS_HOME;
	public static File BUILDER;
	public static File APPS;

	public static void setAkcsHome() throws IOException {

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

		// refresh builder files
		BUILDER = new File(AKCS_HOME, BUILDER_FOLDER_NAME);
		FileUtils.deleteDirectory(BUILDER);

		ApplicationHome appHome = new ApplicationHome(AppWar.class);
		System.out.println("akcs is started at " + appHome.getDir().getAbsolutePath());

		// for dev mode
		File builderSrc = new File(appHome.getDir(), BUILDER_FOLDER_NAME);
		if (!builderSrc.exists()) {
			// for production mode
			File war = new File(appHome.getDir(), WAR_NAME);
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

	}

}
