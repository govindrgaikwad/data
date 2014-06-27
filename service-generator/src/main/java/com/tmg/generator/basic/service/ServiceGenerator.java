package com.tmg.generator.basic.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tmg.generator.basic.domain.Attribute;
import com.tmg.generator.basic.domain.Embeddable;
import com.tmg.generator.basic.domain.Table;
import com.tmg.generator.basic.exception.CompilerException;
import com.tmg.generator.basic.exception.DataAccessException;
import com.tmg.generator.basic.exception.FileException;
import com.tmg.generator.basic.exception.SourceGenerationException;

@Service("ServiceGenerator")
public class ServiceGenerator {

	private static Logger logger = Logger.getLogger(ServiceGenerator.class);

	@Autowired
	DataBaseMetaData data;

	@Transactional
	public void generateSourceCode() throws DataAccessException, FileException,
			CompilerException, SourceGenerationException {
		try {
			Map<String, String> generatedSources = new HashMap<String, String>();
			String path = this.getClass().getClassLoader().getResource("")
					.getPath();
			String[] parts = path.split("classes");
			String basePath = parts[0] + "src/";
			logger.info(basePath);
			List<Table> tables = null;
			tables = data.getTables("UIFramework", null, null,
					new String[] { "Table" });
			init();

			Map<String, String> dataSources = generatePOJO(tables);

			Map<String, String> entitySources = generateEntity(tables);

			Map<String, String> repositorySources = generateRepository(tables);

			Map<String, String> serviceSources = generateService(tables);

			Map<String, String> webserviceSources = generateWebService(tables);

			Map<String, String> webserviceImplSources = generateWebServiceImpl(tables);

			Map<String, String> commonSorces = generateCommonClass(basePath);

			Map<String, String> controllerSources = generateController(tables);

			generatedSources.putAll(dataSources);
			generatedSources.putAll(entitySources);
			generatedSources.putAll(repositorySources);
			generatedSources.putAll(serviceSources);
			generatedSources.putAll(webserviceSources);
			generatedSources.putAll(webserviceImplSources);
			generatedSources.putAll(commonSorces);
			generatedSources.putAll(controllerSources);

			generateWebserviceXml(tables, path);

			generateJavaFiles(generatedSources, basePath);

			compileClasses(generatedSources, path);

		} catch (DataAccessException e) {
			logger.error(" Error Retrieving Tables " + e.getMessage(), e);
			throw new DataAccessException("Error Retrieving Tables"
					+ e.getMessage(), e);
		} catch (FileException e) {
			logger.error(" Error Writing classes " + e.getMessage(), e);
			throw new FileException("Error Writing classes" + e.getMessage(), e);
		} catch (SourceGenerationException e) {
			logger.error(" Error generating source code " + e.getMessage(), e);
			throw new SourceGenerationException("Error generating source code"
					+ e.getMessage(), e);
		} catch (CompilerException e) {
			logger.error(" Error compiling classes " + e.getMessage(), e);
			throw new CompilerException("Error compiling classes"
					+ e.getMessage(), e);
		}

	}

	@Transactional
	private Map<String, String> generatePOJO(List<Table> tables)
			throws SourceGenerationException {
		Map<String, String> result = new HashMap<String, String>();
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity.getTemplate("/templates/Data.vm");
			String basePackage = "com.tmg.generator.data";
			for (Table table : tables) {
				StringWriter sw = new StringWriter();
				context.put("Table", table);

				template.merge(context, sw);

				sw.flush();
				sw.close();

				String fullyQualifiedName = basePackage + "." + table.getName()
						+ "Data";
				result.put(fullyQualifiedName, sw.toString());

			}
		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error(
					"Error while creating POJO classes :" + e.getMessage(), e);
			throw new SourceGenerationException(
					"Error while creating POJO classes :" + e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateEntity(List<Table> tables)
			throws SourceGenerationException {
		Map<String, String> result = new HashMap<String, String>();
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity.getTemplate("/templates/Entity.vm");
			String basePackage = "com.tmg.generator.entity";
			context.put("StringUtil", new StringUtils());
			context.put("Tables", tables);
			for (Table table : tables) {
				if (table.isEmbeddableFlag()) {
					generateEmbeddedEntity(table, result);
				}
				context.put("Table", table);
				StringWriter sw = new StringWriter();
				template.merge(context, sw);
				sw.flush();
				sw.close();

				String fullyQualifiedName = basePackage + "." + table.getName();
				result.put(fullyQualifiedName, sw.toString());
			}
		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error(
					"Error while creating Entity classes :" + e.getMessage(), e);
			throw new SourceGenerationException(
					"Error while creating Entity classes :" + e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	private void generateEmbeddedEntity(Table table, Map<String, String> result)
			throws SourceGenerationException {
		StringWriter sw = new StringWriter();
		List<Attribute> existingAttributes = table.getAttributes();
		List<Attribute> deleteAttributes = new ArrayList<Attribute>();
		List<Attribute> attributes = new ArrayList<Attribute>();
		Embeddable embeddable = new Embeddable();
		embeddable.setName(table.getName() + "EmbeddableId");
		embeddable.setCamelCaseName(table.getCamelCaseName() + "EmbeddableId");
		for (Attribute attribute : existingAttributes) {
			if (attribute.isPrimaryKey()) {
				attributes.add(attribute);
				deleteAttributes.add(attribute);
			}
		}
		existingAttributes.removeAll(deleteAttributes);
		embeddable.setAttributes(attributes);
		table.setAttributes(existingAttributes);
		table.setEmbeddable(embeddable);
		try {
			VelocityContext context = new VelocityContext();
			Template temp = Velocity.getTemplate("/templates/Embeddable.vm");
			String basePackage = "com.tmg.generator.entity";
			context.put("Embeddable", embeddable);

			temp.merge(context, sw);

			sw.flush();
			sw.close();

			String fullyQualifiedName = basePackage + "."
					+ embeddable.getName();
			result.put(fullyQualifiedName, sw.toString());
		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error(
					"Error while creating Embeddable classes :"
							+ e.getMessage(), e);
			throw new SourceGenerationException(
					"Error while creating Embeddable classes :"
							+ e.getMessage(), e);
		}

	}

	@Transactional
	private Map<String, String> generateRepository(List<Table> tables)
			throws SourceGenerationException {
		Map<String, String> result = new HashMap<String, String>();
		StringWriter sw = new StringWriter();
		StringWriter sw1 = new StringWriter();
		StringWriter sw2 = new StringWriter();
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity
					.getTemplate("/templates/Repository.vm");
			Template custom = Velocity
					.getTemplate("/templates/CustomRepository.vm");
			Template customImpl = Velocity
					.getTemplate("/templates/CustomRepositoryImpl.vm");
			String basePackage = "com.tmg.generator.repository";
			custom.merge(context, sw);
			sw.flush();
			sw.close();
			customImpl.merge(context, sw1);
			sw1.flush();
			sw1.close();

			String fullyQualifiedName = basePackage
					+ ".GenericCustomRepository";
			result.put(fullyQualifiedName, sw.toString());
			fullyQualifiedName = basePackage + ".GenericRepositoryImpl";
			result.put(fullyQualifiedName, sw1.toString());

			context.put("Tables", tables);
			context.put("Repository", "GenericRepository");
			context.put("Entity", tables.get(0).getName());

			template.merge(context, sw2);

			sw2.flush();
			sw2.close();

			fullyQualifiedName = basePackage + ".GenericRepository";
			result.put(fullyQualifiedName, sw2.toString());
		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error("Error while creating Repository :" + e.getMessage(),
					e);
			throw new SourceGenerationException(
					"Error while creating Repository :" + e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateService(List<Table> tables)
			throws SourceGenerationException {
		Map<String, String> result = new HashMap<String, String>();
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity.getTemplate("/templates/Service.vm");
			String basePackage = "com.tmg.generator.services";
			context.put("Tables", tables);
			context.put("StringUtil", new StringUtils());
			for (Table table : tables) {
				context.put("Table", table);
				StringWriter sw = new StringWriter();

				template.merge(context, sw);

				sw.flush();
				sw.close();

				String fullyQualifiedName = basePackage + "." + table.getName()
						+ "Service";
				result.put(fullyQualifiedName, sw.toString());
			}
		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error("Error while creating Services :" + e.getMessage(), e);
			throw new SourceGenerationException(
					"Error while creating Services :" + e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateWebService(List<Table> tables)
			throws SourceGenerationException {
		Map<String, String> result = new HashMap<String, String>();
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity
					.getTemplate("/templates/WebService.vm");
			String basePackage = "com.tmg.generator.webservices";
			context.put("Tables", tables);
			context.put("StringUtil", new StringUtils());
			for (Table table : tables) {
				context.put("Table", table);
				StringWriter sw = new StringWriter();
				template.merge(context, sw);

				sw.flush();
				sw.close();

				String fullyQualifiedName = basePackage + "." + table.getName()
						+ "WebService";
				result.put(fullyQualifiedName, sw.toString());
			}
		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error(
					"Error while creating Webservice Interfaces :"
							+ e.getMessage(), e);
			throw new SourceGenerationException(
					"Error while creating Webservice Interfaces :"
							+ e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateWebServiceImpl(List<Table> tables)
			throws SourceGenerationException {
		Map<String, String> result = new HashMap<String, String>();
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity
					.getTemplate("/templates/WebServiceImpl.vm");
			String basePackage = "com.tmg.generator.webservices";
			context.put("Tables", tables);
			context.put("StringUtil", new StringUtils());
			for (Table table : tables) {
				context.put("Table", table);
				StringWriter sw = new StringWriter();

				template.merge(context, sw);

				sw.flush();
				sw.close();

				String fullyQualifiedName = basePackage + "." + table.getName()
						+ "WebServiceImpl";
				result.put(fullyQualifiedName, sw.toString());
			}
		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error("Error while creating Webservices Implementation :"
					+ e.getMessage(), e);
			throw new SourceGenerationException(
					"Error while creating Webservices Implementation :"
							+ e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateCommonClass(String path)
			throws SourceGenerationException {
		Map<String, String> result = new HashMap<String, String>();
		StringWriter sw = new StringWriter();
		StringWriter sw1 = new StringWriter();
		StringWriter fault = new StringWriter();
		StringWriter exception = new StringWriter();

		try {
			VelocityContext context = new VelocityContext();
			Template operation = Velocity
					.getTemplate("/templates/OperationResult.vm");
			Template serviceOperation = Velocity
					.getTemplate("/templates/ServiceOperationResult.vm");
			Template serviceFault = Velocity
					.getTemplate("/templates/ServiceFault.vm");
			Template dataException = Velocity
					.getTemplate("/templates/DataAccessException.vm");
			String basePackage = "com.tmg.generator.common";
			operation.merge(context, sw);
			sw.flush();
			sw.close();
			serviceOperation.merge(context, sw1);
			sw1.flush();
			sw1.close();
			serviceFault.merge(context, fault);
			fault.flush();
			fault.close();
			dataException.merge(context, exception);
			exception.flush();
			exception.close();

			String fullyQualifiedName = basePackage + ".OperationResult";
			result.put(fullyQualifiedName, sw.toString());
			fullyQualifiedName = basePackage + ".ServiceOperationResult";
			result.put(fullyQualifiedName, sw1.toString());
			fullyQualifiedName = basePackage + ".ServiceFault";
			result.put(fullyQualifiedName, fault.toString());
			fullyQualifiedName = basePackage + ".DataAccessException";
			result.put(fullyQualifiedName, exception.toString());

		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error(
					"Error while creating Result Classes :" + e.getMessage(), e);
			throw new SourceGenerationException(
					"Error while creating Result Classes :" + e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateController(List<Table> tables)
			throws SourceGenerationException {
		Map<String, String> result = new HashMap<String, String>();
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity
					.getTemplate("/templates/Controller.vm");
			String basePackage = "com.tmg.generator.controller";
			context.put("Tables", tables);
			context.put("StringUtil", new StringUtils());
			for (Table table : tables) {
				context.put("Table", table);
				StringWriter sw = new StringWriter();

				template.merge(context, sw);

				sw.flush();
				sw.close();

				String fullyQualifiedName = basePackage + "." + table.getName()
						+ "Controller";
				result.put(fullyQualifiedName, sw.toString());
			}
			Template exception = Velocity
					.getTemplate("/templates/ExceptionControllerAdvice.vm");
			StringWriter controller = new StringWriter();
			exception.merge(context, controller);
			controller.flush();
			controller.close();
			String fullyQualifiedName = basePackage + "."
					+ "ExceptionControllerAdvice";
			result.put(fullyQualifiedName, controller.toString());
		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error("Error while creating Controller :" + e.getMessage(),
					e);
			throw new SourceGenerationException(
					"Error while creating Controller :" + e.getMessage(), e);
		}
		return result;
	}

	@Transactional
	private void generateWebserviceXml(List<Table> tables, String path)
			throws FileException {
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity
					.getTemplate("/templates/webservice-xml.vm");
			context.put("Tables", tables);

			File file = new File(path + "/");
			if (!file.exists())
				file.mkdirs();
			FileWriter w;

			w = new FileWriter(path + "/webservice.xml");
			template.merge(context, w);
			w.close();

		} catch (ResourceNotFoundException | ParseErrorException
				| MethodInvocationException | IOException e) {
			logger.error(
					"Error while creating Webservice XML :" + e.getMessage(), e);
			throw new FileException("Error while creating Webservice XML :"
					+ e.getMessage(), e);
		}

	}

	private void generateJavaFiles(Map<String, String> generatedSources,
			String basepath) throws FileException {

		File baseFolder = new File(basepath);
		for (String key : generatedSources.keySet()) {

			String className = key.substring(key.lastIndexOf(".") + 1,
					key.length());
			String basePackage = key.substring(0, key.lastIndexOf("."));
			String otherFolders = basePackage.replace(".", "/");
			File dirs = new File(baseFolder, otherFolders);
			if (!dirs.exists()) {
				dirs.mkdirs();
			}

			String source = generatedSources.get(key);
			File javaFile = new File(dirs, className + ".java");

			try {
				FileOutputStream fos = new FileOutputStream(javaFile);
				fos.write(source.getBytes());
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				logger.error("File Not Found" + e.getMessage(), e);
				throw new FileException("File Not Found" + e.getMessage(), e);

			} catch (IOException e) {
				logger.error("Error while writing File" + e.getMessage(), e);
				throw new FileException("Error while writing File"
						+ e.getMessage(), e);
			}

		}

	}

	private void compileClasses(Map<String, String> generatedSources,
			String basePath) throws CompilerException {

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		System.out.println(compiler);
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				null, null, null);

		// ClassFileManager classFileManager = (ClassFileManager) fileManager;
		List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
		for (String qualifiedClassName : generatedSources.keySet()) {
			String javaCode = generatedSources.get(qualifiedClassName);
			jfiles.add(new JavaSourceFromString(qualifiedClassName, javaCode));
		}

		String calculatedClassPath = getClassPath();
		URLClassLoader loader = (URLClassLoader) ServiceGenerator.class
				.getClassLoader();
		URL urls[] = loader.getURLs();
		List<File> classpath = new ArrayList<File>();
		for (URL url : urls) {
			if (url.toString().endsWith("jar")) {
				String[] jar = url.toString().split("file:/");
				String string = jar[1];
				classpath.add(new File(string));
			}
		}
		logger.info(calculatedClassPath);
		Iterable<File> files = Arrays.asList(new File(basePath));
		Iterable<File> path = classpath;
		try {
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, files);
			fileManager.setLocation(StandardLocation.CLASS_PATH, path);

		} catch (IOException e) {
			logger.error("Error while compiling classes" + e.getMessage(), e);
			throw new CompilerException("Error while compiling classes"
					+ e.getMessage(), e);
		}
		List<String> optionList = new ArrayList<String>();
		// set compiler's classpath to be same as the runtime's
		optionList.addAll(Arrays.asList("-classpath", calculatedClassPath));

		// We specify a task to the compiler. Compiler should use our file
		// manager and our list of "files".
		// Then we run the compilation with call()
		int isSupportedOption = compiler.isSupportedOption("-classpath");
		logger.info(isSupportedOption);

		JavaFileManager manager = fileManager;
		boolean compilationStatus = compiler.getTask(null, manager, null,
				optionList, null, jfiles).call();
		if (compilationStatus == false) {
			logger.error("ERROR: compilation failed ");
			throw new CompilerException("Error while compiling classes");
		} else {
			logger.info("Compilation Completed Successfully");
		}
	}

	private String getClassPath() {
		URL classLocationURL = getClass().getProtectionDomain().getCodeSource()
				.getLocation();
		File f = null;
		try {
			f = new File(classLocationURL.getFile());
		} catch (Exception e1) {

		}
		String classLocation = f.getAbsolutePath();

		String baseClassPath = System.getProperty("java.class.path");
		String calculatedClassPath = baseClassPath + File.pathSeparator
				+ classLocation;

		return calculatedClassPath;
	}

	private void init() {
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
	}

}
