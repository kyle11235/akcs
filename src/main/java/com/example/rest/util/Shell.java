package com.example.rest.util;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

public class Shell {

	public static void executeParams(String command, String[] params) throws ExecuteException, IOException {
		CommandLine cmdLine = new CommandLine(command);
		if (params != null) {
			for (String p : params) {
				cmdLine.addArgument(p);
			}
		}
		DefaultExecutor executor = new DefaultExecutor();
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000 * 30);
		executor.setWatchdog(watchdog);
		executor.execute(cmdLine);
	}

	public static void execute(String command, String param) throws ExecuteException, IOException {
		String[] params = {param};
		executeParams(command, params);
	}

	public static void main(String[] args) throws ExecuteException, IOException {
//		String[] params = { "Cheese", "Pepperoni", "Black Olives" };
//		executeParams("echo", params);
//		execute("pwd", null);
//		execute("pwd", null);

		execute("/Users/kyle/.akcs/apps/node/docker/node/build.sh", null);
		

	}

}
