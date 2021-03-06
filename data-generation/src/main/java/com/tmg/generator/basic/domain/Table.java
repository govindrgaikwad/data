package com.tmg.generator.basic.domain;

import java.util.List;

/**
 * This class is used to store the All information about the DataBase Table.
 * 
 * @author Govind Gaikwad
 * 
 * @version 1.0 June 27, 2014.
 */
public class Table {

	private String tableName;

	private String name;

	private String camelCaseName;

	private String tableSchema;

	private List<Relationship> relationships;

	private List<Attribute> attributes;

	private boolean embeddableFlag;

	private Embeddable embeddable;

	private boolean identity;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCamelCaseName() {
		return camelCaseName;
	}

	public void setCamelCaseName(String camelCaseName) {
		this.camelCaseName = camelCaseName;
	}

	public String getTableSchema() {
		return tableSchema;
	}

	public void setTableSchema(String tableSchema) {
		this.tableSchema = tableSchema;
	}

	public List<Relationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public boolean isEmbeddableFlag() {
		return embeddableFlag;
	}

	public void setEmbeddableFlag(boolean embeddableFlag) {
		this.embeddableFlag = embeddableFlag;
	}

	public Embeddable getEmbeddable() {
		return embeddable;
	}

	public void setEmbeddable(Embeddable embeddable) {
		this.embeddable = embeddable;
	}

	public boolean isIdentity() {
		return identity;
	}

	public void setIdentity(boolean identity) {
		this.identity = identity;
	}

}
