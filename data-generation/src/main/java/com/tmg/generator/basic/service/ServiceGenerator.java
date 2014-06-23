package com.tmg.generator.basic.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tmg.generator.basic.domain.Attribute;
import com.tmg.generator.basic.domain.Embeddable;
import com.tmg.generator.basic.domain.Table;

@Service("ServiceGenerator")
public class ServiceGenerator {

	private static Logger logger = Logger.getLogger(ServiceGenerator.class);

	@Autowired
	DataBaseMetaData data;

	@Transactional
	public void generate() {
		try {
			Map<String, String> generatedSources = new HashMap<String, String>();
			String basePath = "src/main/java";
			List<Table> tables = null;
			try {
				tables = data.getTables("UIFramework", null, null,
						new String[] { "Table" });
			} catch (DataAccessException e) {
				logger.error("Error Retrieving Tables" + e.getMessage(), e);
				throw new DataAccessException("Error Retrieving Tables"
						+ e.getMessage(), e);
			}
			Map<String, String> dataSources = generatePOJO(tables);

			Map<String, String> entitySources = generateEntity(tables);
			Map<String, String> repositorySources = generateRepository(tables);
			Map<String, String> serviceSources = generateService(tables);
			Map<String, String> webserviceSources = generateWebService(tables);
			Map<String, String> webserviceImplSources = generateWebServiceImpl(tables);
			Map<String, String> controllerSources = generateController(tables);
			Map<String, String> testCasesSources = generateTestCases(tables);

			generatedSources.putAll(dataSources);
			generatedSources.putAll(entitySources);
			generatedSources.putAll(repositorySources);
			generatedSources.putAll(serviceSources);
			generatedSources.putAll(webserviceSources);
			generatedSources.putAll(webserviceImplSources);
			generatedSources.putAll(controllerSources);
			generateWebserviceXml(tables, "src/main/resources");
			generateJavaFiles(generatedSources, basePath);
			generateJavaFiles(testCasesSources, "src/test/java");
		} catch (DataAccessException e) {
			e.printStackTrace();
		} catch (FileWriteException e) {
			e.printStackTrace();
		}
	}

	@Transactional
	private Map<String, String> generatePOJO(List<Table> tables)
			throws FileWriteException {
		Map<String, String> result = new HashMap<String, String>();
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/Data.vm");
		String basePackage = "com.tmg.generator.data";
		for (Table table : tables) {
			StringWriter sw = new StringWriter();
			context.put("Table", table);
			try {
				template.merge(context, sw);

				sw.flush();
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String fullyQualifiedName = basePackage + "." + table.getName()
					+ "Data";
			result.put(fullyQualifiedName, sw.toString());

		}
		return result;
	}

	@Transactional
	private Map<String, String> generateEntity(List<Table> tables)
			throws FileWriteException {
		Map<String, String> result = new HashMap<String, String>();
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
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
			try {
				template.merge(context, sw);

				sw.flush();
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String fullyQualifiedName = basePackage + "." + table.getName();
			result.put(fullyQualifiedName, sw.toString());
		}
		return result;
	}

	@Transactional
	private void generateEmbeddedEntity(Table table, Map<String, String> result)
			throws FileWriteException {
		StringWriter sw = new StringWriter();
		VelocityContext context = new VelocityContext();
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
		Template temp = Velocity.getTemplate("/templates/Embeddable.vm");
		String basePackage = "com.tmg.generator.entity";
		context.put("Embeddable", embeddable);
		try {
			temp.merge(context, sw);

			sw.flush();
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String fullyQualifiedName = basePackage + "." + embeddable.getName();
		result.put(fullyQualifiedName, sw.toString());

	}

	@Transactional
	private Map<String, String> generateRepository(List<Table> tables)
			throws FileWriteException {
		Map<String, String> result = new HashMap<String, String>();
		StringWriter sw = new StringWriter();
		StringWriter sw1 = new StringWriter();
		StringWriter sw2 = new StringWriter();
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/Repository.vm");
		Template custom = Velocity
				.getTemplate("/templates/CustomRepository.vm");
		Template customImpl = Velocity
				.getTemplate("/templates/CustomRepositoryImpl.vm");
		String basePackage = "com.tmg.generator.repository";
		try {
			custom.merge(context, sw);
			sw.flush();
			sw.close();
			customImpl.merge(context, sw1);
			sw1.flush();
			sw1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String fullyQualifiedName = basePackage + ".GenericCustomRepository";
		result.put(fullyQualifiedName, sw.toString());
		fullyQualifiedName = basePackage + ".GenericRepositoryImpl";
		result.put(fullyQualifiedName, sw1.toString());

		context.put("Tables", tables);
		context.put("Repository", "GenericRepository");
		context.put("Entity", tables.get(0).getName());
		try {
			template.merge(context, sw2);

			sw2.flush();
			sw2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fullyQualifiedName = basePackage + ".GenericRepository";
		result.put(fullyQualifiedName, sw2.toString());
		return result;
	}

	@Transactional
	private Map<String, String> generateService(List<Table> tables)
			throws FileWriteException {
		Map<String, String> result = new HashMap<String, String>();

		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/Service.vm");
		String basePackage = "com.tmg.generator.services";
		context.put("Tables", tables);
		context.put("StringUtil", new StringUtils());
		for (Table table : tables) {
			context.put("Table", table);
			StringWriter sw = new StringWriter();
			try {
				template.merge(context, sw);

				sw.flush();
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String fullyQualifiedName = basePackage + "." + table.getName()
					+ "Service";
			result.put(fullyQualifiedName, sw.toString());
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateWebService(List<Table> tables)
			throws FileWriteException {
		Map<String, String> result = new HashMap<String, String>();

		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/WebService.vm");
		String basePackage = "com.tmg.generator.webservices";
		context.put("Tables", tables);
		context.put("StringUtil", new StringUtils());
		for (Table table : tables) {
			context.put("Table", table);
			StringWriter sw = new StringWriter();
			try {
				template.merge(context, sw);

				sw.flush();
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String fullyQualifiedName = basePackage + "." + table.getName()
					+ "WebService";
			result.put(fullyQualifiedName, sw.toString());
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateWebServiceImpl(List<Table> tables)
			throws FileWriteException {
		Map<String, String> result = new HashMap<String, String>();

		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity
				.getTemplate("/templates/WebServiceImpl.vm");
		String basePackage = "com.tmg.generator.webservices";
		context.put("Tables", tables);
		context.put("StringUtil", new StringUtils());
		for (Table table : tables) {
			context.put("Table", table);
			StringWriter sw = new StringWriter();
			try {
				template.merge(context, sw);

				sw.flush();
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String fullyQualifiedName = basePackage + "." + table.getName()
					+ "WebServiceImpl";
			result.put(fullyQualifiedName, sw.toString());
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateController(List<Table> tables)
			throws FileWriteException {
		Map<String, String> result = new HashMap<String, String>();

		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/Controller.vm");
		String basePackage = "com.tmg.generator.controller";
		context.put("Tables", tables);
		context.put("StringUtil", new StringUtils());
		for (Table table : tables) {
			context.put("Table", table);
			StringWriter sw = new StringWriter();
			try {
				template.merge(context, sw);

				sw.flush();
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String fullyQualifiedName = basePackage + "." + table.getName()
					+ "Controller";
			result.put(fullyQualifiedName, sw.toString());
		}
		return result;
	}

	@Transactional
	private Map<String, String> generateTestCases(List<Table> tables)
			throws FileWriteException {
		Map<String, String> result = new HashMap<String, String>();

		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/JUnit.vm");
		String basePackage = "com.tmg.generator.test";
		context.put("Tables", tables);
		for (Table table : tables) {
			context.put("Table", table);
			StringWriter sw = new StringWriter();
			try {
				template.merge(context, sw);

				sw.flush();
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String fullyQualifiedName = basePackage + "." + table.getName()
					+ "Test";
			result.put(fullyQualifiedName, sw.toString());
		}
		return result;
	}

	@Transactional
	private void generateWebserviceXml(List<Table> tables, String path)
			throws FileWriteException {
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity
				.getTemplate("/templates/webservice-xml.vm");
		context.put("Tables", tables);

		File file = new File(path + "/");
		if (!file.exists())
			file.mkdirs();
		FileWriter w;
		try {
			w = new FileWriter(path + "/webservice.xml");
			template.merge(context, w);
			w.close();

		} catch (IOException e) {
			logger.error("Error Creating webservice xml" + e.getMessage(), e);
			throw new FileWriteException("Error Creating webservice xml"
					+ e.getMessage(), e);
		}

	}

	private void generateJavaFiles(Map<String, String> generatedSources,
			String basepath) {

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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ApplicationContext ac = new ClassPathXmlApplicationContext(
				"data-generation.xml");
		ServiceGenerator generator = ac.getBean(ServiceGenerator.class);
		generator.generate();

	}
}
