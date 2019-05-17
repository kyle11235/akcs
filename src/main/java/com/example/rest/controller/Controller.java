
package com.example.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.rest.model.App;
import com.example.rest.model.TypeEnum;
import com.example.rest.service.AppService;

@CrossOrigin
@RestController
@RequestMapping()
public class Controller {

	@Autowired
	private AppService appService;

	@GetMapping("/")
	public String index() {
		return "hello AKCS";
	}

	@GetMapping(value = "/app")
	public List<App> listApps() {
		return appService.listApps();
	}

	@PostMapping("/create")
	public String create(@RequestParam("name") String name, @RequestParam("type") TypeEnum type, @RequestParam("file") MultipartFile file) {
		try {
			appService.createApp(name, type, file);
		} catch (Exception e) {
			e.printStackTrace();
			return "error: " + e.getMessage();
		}
		return "success";
	}

	@PostMapping("/update")
	public String update(@RequestParam("name") String name, @RequestParam("type") TypeEnum type, @RequestParam("file") MultipartFile file) {
		try {
			appService.updateApp(name, type, file);
		} catch (Exception e) {
			e.printStackTrace();
			return "error: " + e.getMessage();
		}
		return "success";
	}

	@PostMapping("/delete")
	public String delete(@RequestParam("name") String name) {

		// no authorization no delete

		return "success";
	}
}
