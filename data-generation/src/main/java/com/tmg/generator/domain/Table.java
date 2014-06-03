package com.tmg.generator.domain;

import java.util.List;

public class Table {

	private String tableName;

	private String name;

	private String camelCaseName;

	private String tableSchema;

	private List<Relationship> relationships;

	private List<Attribute> attributes;

	private boolean embeddable;

	private String embeddableName;

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

	public boolean isEmbeddable() {
		return embeddable;
	}

	public void setEmbeddable(boolean embeddable) {
		this.embeddable = embeddable;
	}

	public String getEmbeddableName() {
		return embeddableName;
	}

	public void setEmbeddableName(String embeddableName) {
		this.embeddableName = embeddableName;
	}

}
