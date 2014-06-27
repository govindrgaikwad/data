package com.tmg.generator.basic.domain;

public class ForeignKey {


	private String tableName;

	private String camelCaseName;
	
	private String compareColumn;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getCamelCaseName() {
		return camelCaseName;
	}

	public void setCamelCaseName(String camelCaseName) {
		this.camelCaseName = camelCaseName;
	}

	public String getCompareColumn() {
		return compareColumn;
	}

	public void setCompareColumn(String compareColumn) {
		this.compareColumn = compareColumn;
	}

	
}
