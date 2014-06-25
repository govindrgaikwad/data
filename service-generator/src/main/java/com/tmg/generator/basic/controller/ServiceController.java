package com.tmg.generator.basic.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tmg.generator.basic.exception.CompilerException;
import com.tmg.generator.basic.exception.DataAccessException;
import com.tmg.generator.basic.exception.FileException;
import com.tmg.generator.basic.exception.SourceGenerationException;
import com.tmg.generator.basic.service.ServiceGenerator;

@Controller
public class ServiceController {

	@Autowired
	private ServiceGenerator generator;

	@RequestMapping(value = "/generateCode", method = RequestMethod.GET)
	public @ResponseBody ModelAndView generateCode(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView model = new ModelAndView();
		try {
			generator.generateSourceCode();
			model.setViewName("success");
		} catch (DataAccessException e) {
			model.setViewName("database");
		} catch (SourceGenerationException e) {
			model.setViewName("source");
		} catch (FileException e) {
			model.setViewName("file");
		} catch (CompilerException e) {
			model.setViewName("compiler");
		}
		return model;
	}
}
