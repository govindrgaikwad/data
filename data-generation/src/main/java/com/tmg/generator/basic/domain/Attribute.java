package com.tmg.generator.basic.domain;

public class Attribute {

	private String columnName;

	private String name;

	private String camelCaseName;

	private String dataType;

	private boolean primaryKey;

	private boolean foreignKey;

	private String referenceTableName;

	private String camelCaseTableName;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
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

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(boolean foreignKey) {
		this.foreignKey = foreignKey;
	}

	public String getReferenceTableName() {
		return referenceTableName;
	}

	public void setReferenceTableName(String referenceTableName) {
		this.referenceTableName = referenceTableName;
	}

	public String getCamelCaseTableName() {
		return camelCaseTableName;
	}

	public void setCamelCaseTableName(String camelCaseTableName) {
		this.camelCaseTableName = camelCaseTableName;
	}

}
