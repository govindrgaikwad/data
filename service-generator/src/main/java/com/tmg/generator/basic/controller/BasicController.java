package com.tmg.generator.basic.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tmg.generator.basic.service.ServiceGenerator;

@Controller
public class BasicController {

	@Autowired
	private ServiceGenerator generator;

	private static Logger logger = Logger.getLogger(BasicController.class);

	@RequestMapping(value = "/generateCode", method = RequestMethod.GET)
	public @ResponseBody ModelAndView generateCode(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView model = new ModelAndView();
		try {
			boolean result = generator.generateSourceCode();
			if (result) {
				model.setViewName("success");
			} else {
				model.setViewName("error");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return model;
	}
}
