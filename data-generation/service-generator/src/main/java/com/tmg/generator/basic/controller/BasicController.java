package com.tmg.generator.basic.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tmg.generator.basic.service.ServiceGenerator;

@Controller
@RequestMapping(value = "/generate", method = RequestMethod.GET)
public class BasicController {

	@Autowired
	private ServiceGenerator generator;

	private static Logger logger = Logger.getLogger(BasicController.class);

	@RequestMapping(value = "/code", method = RequestMethod.GET)
	public @ResponseBody void generateCode(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			generator.generate();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
