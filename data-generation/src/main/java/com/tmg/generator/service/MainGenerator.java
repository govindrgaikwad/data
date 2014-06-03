package com.tmg.generator.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tmg.generator.domain.Attribute;
import com.tmg.generator.domain.Embeddable;
import com.tmg.generator.domain.Table;

@Service("MainGenerator")
public class MainGenerator {

	private static Logger logger = Logger.getLogger(MainGenerator.class);

	@Autowired
	DataBaseMetaData data;
	
	@Transactional
	public void generate() {
		try {

			String basePath = this.getClass().getClassLoader().getResource("")
					.getPath();
			String patharray[] = basePath.split("/classes/");
			basePath = patharray[0].substring(1);
			System.out.println(basePath);
			List<Table> tables = null;
			try {
				tables = data.getTables("UIFramework", null, null,
						new String[] { "Table" });
			} catch (DataAccessException e) {
				logger.error("Error Retrieving Tables" + e.getMessage(), e);
				throw new DataAccessException("Error Retrieving Tables"
						+ e.getMessage(), e);
			}
			generateEntity(tables, basePath);
			generateRepository(tables, basePath);
			generatePOJO(tables, basePath);
		} catch (DataAccessException e) {
			e.printStackTrace();
		} catch (FileWriteException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ApplicationContext ac = new ClassPathXmlApplicationContext(
				"data-generation.xml");
		MainGenerator generator = ac.getBean(MainGenerator.class);
		generator.generate();
		
	}

	@Transactional
	public void generatePOJO(List<Table> tables, String path)
			throws FileWriteException {
		Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/Data.vm");
		context.put("PackageName", "com.tmg.generator.data");

		for (Table table : tables) {
			File file = new File("src/generated/com/tmg/generator/data/");
			if (!file.exists())
				file.mkdirs();
			context.put("Table", table);
			FileWriter w;
			try {
				w = new FileWriter("src/generated/com/tmg/generator/data/"
						+ table.getName() + "Data.java");
				template.merge(context, w);
				w.close();

			} catch (IOException e) {
				logger.error("Error Creating Entities" + e.getMessage(), e);
				throw new FileWriteException("Error Creating Entities"
						+ e.getMessage(), e);
			}

		}

	}

	@Transactional
	public void generateEntity(List<Table> tables, String path)
			throws FileWriteException {

		Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/Entity.vm");
		context.put("PackageName", "com.tmg.generator.entity");
		context.put("StringUtil", new StringUtils());

		for (Table table : tables) {
			File file = new File("src/generated/com/tmg/generator/entity/");
			if (!file.exists())
				file.mkdirs();
			if (table.isEmbeddable()) {
				generateEmbeddedEntity(table, path);
			}
			context.put("Table", table);
			FileWriter w;
			try {
				w = new FileWriter("src/generated/com/tmg/generator/entity/"
						+ table.getName() + ".java");
				template.merge(context, w);
				w.close();

			} catch (IOException e) {
				logger.error("Error Creating Entities" + e.getMessage(), e);
				throw new FileWriteException("Error Creating Entities"
						+ e.getMessage(), e);
			}

		}

	}

	@Transactional
	private void generateEmbeddedEntity(Table table, String path)
			throws FileWriteException {

		VelocityContext context = new VelocityContext();
		List<Attribute> existingAttributes = table.getAttributes();
		List<Attribute> deleteAttributes = new ArrayList<Attribute>();
		List<Attribute> attributes = new ArrayList<Attribute>();
		Embeddable embeddable = new Embeddable();
		embeddable.setName(table.getName() + "EmbeddableId");
		for (Attribute attribute : existingAttributes) {
			if (attribute.isPrimaryKey()) {
				attributes.add(attribute);
				deleteAttributes.add(attribute);
			}
		}
		existingAttributes.removeAll(deleteAttributes);
		embeddable.setAttributes(attributes);
		table.setAttributes(existingAttributes);
		table.setEmbeddableName(table.getTableName() + "EmbeddableId");
		Template temp = Velocity.getTemplate("/templates/Embeddable.vm");
		context.put("PackageName", "com.tmg.data");
		context.put("Embeddable", embeddable);
		FileWriter w;
		try {
			w = new FileWriter("src/generated/com/tmg/generator/entity/"
					+ embeddable.getName() + ".java");
			temp.merge(context, w);
			w.close();

		} catch (IOException e) {
			logger.error("Error Creating Entities" + e.getMessage(), e);
			throw new FileWriteException("Error Creating Entities"
					+ e.getMessage(), e);
		}

	}

	@Transactional
	public void generateRepository(List<Table> tables, String path)
			throws FileWriteException {

		Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(p);
		VelocityContext context = new VelocityContext();
		Template template = Velocity.getTemplate("/templates/Repository.vm");
		Template custom = Velocity
				.getTemplate("/templates/CustomRepository.vm");
		Template customImpl = Velocity
				.getTemplate("/templates/CustomRepositoryImpl.vm");
		File file = new File("src/generated/com/tmg/generator/repository/");
		if (!file.exists())
			file.mkdirs();
		context.put("PackageName", "com.tmg.generator.entity");

		FileWriter fileCustom = null;
		FileWriter fileCustomImpl = null;
		try {
			fileCustom = new FileWriter(
					"src/generated/com/tmg/generator/repository/GenericCustomRepository.java");
			custom.merge(context, fileCustom);
			fileCustom.close();

			fileCustomImpl = new FileWriter(
					"src/generated/com/tmg/generator/repository/GenericCustomRepositoryImpl.java");
			customImpl.merge(context, fileCustomImpl);
			fileCustomImpl.close();

		} catch (IOException e) {
			logger.error("Error Creating Repository" + e.getMessage(), e);
			throw new FileWriteException("Error Creating Repository"
					+ e.getMessage(), e);
		}

		context.put("Tables", tables);
		context.put("Repository", "GenericRepository");
		context.put("Entity", tables.get(0).getName());
		FileWriter w;
		try {
			w = new FileWriter(
					"src/generated/com/tmg/generator/repository/GenericRepository.java");
			template.merge(context, w);
			w.close();

		} catch (IOException e) {
			logger.error("Error Creating Repository" + e.getMessage(), e);
			throw new FileWriteException("Error Creating Repository"
					+ e.getMessage(), e);
		}

	}


}
