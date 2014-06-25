package com.tmg.generator.basic.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tmg.generator.basic.domain.Attribute;
import com.tmg.generator.basic.domain.ForeignKey;
import com.tmg.generator.basic.domain.Relationship;
import com.tmg.generator.basic.domain.Table;
import com.tmg.generator.basic.exception.DataAccessException;

@Service("DataBaseMetaData")
public class DataBaseMetaData {

	private static Logger logger = Logger.getLogger(DataBaseMetaData.class);

	@Autowired
	ExternalConnection externalconnection;

	@Transactional(readOnly = true)
	public List<String> getCatalogs() throws DataAccessException {
		List<String> catalogs = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = externalconnection.getConnection();
			DatabaseMetaData data = connection.getMetaData();
			ResultSet rs = data.getCatalogs();
			while (rs.next()) {
				logger.info("**************List Of All Catalogs***************");
				logger.info(rs.getString("TABLE_CAT"));
				catalogs.add(rs.getString("TABLE_CAT"));
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("Error Retrieving Catalogs" + e.getMessage(), e);
			throw new DataAccessException("Error Retrieving Catalogs"
					+ e.getMessage(), e);
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new DataAccessException("Error closing connection"
						+ e.getMessage(), e);
			}
		}
		return catalogs;
	}

	@Transactional(readOnly = true)
	public List<String> getSchemas(String catalog, String schemaPattern)
			throws DataAccessException {
		List<String> schemas = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = externalconnection.getConnection();
			DatabaseMetaData data = connection.getMetaData();
			ResultSet rs = data.getSchemas(catalog, schemaPattern);
			while (rs.next()) {
				logger.info("**************List Of All Schemas***************");
				logger.info(rs.getString("TABLE_SCHEM"));
				schemas.add(rs.getString("TABLE_SCHEM"));
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("Error Retrieving Schemas" + e.getMessage(), e);
			throw new DataAccessException("Error Retrieving Schemas"
					+ e.getMessage(), e);
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new DataAccessException("Error closing connection"
						+ e.getMessage(), e);
			}
		}
		return schemas;
	}

	@Transactional(readOnly = true)
	public List<Table> getTables(String catalog, String schemaPattern,
			String tableNamePattern, String[] types) throws DataAccessException {
		List<Table> tables = new ArrayList<Table>();
		Connection connection = null;
		try {
			connection = externalconnection.getConnection();
			DatabaseMetaData data = connection.getMetaData();
			ResultSet rs = data.getTables(catalog, schemaPattern,
					tableNamePattern, types);
			while (rs.next()) {
				logger.info("**************Table Information***************");
				Table table = new Table();
				table.setTableSchema(rs.getString("TABLE_SCHEM"));
				String tableName = rs.getString("TABLE_NAME").trim();
				table.setTableName(tableName);
				tableName = tableName.replaceAll("\\s", "");
				tableName = tableName.substring(0, 1).toUpperCase().trim()
						+ tableName.substring(1);
				logger.info(tableName);
				table.setName(tableName);
				tableName = tableName.substring(0, 1).toLowerCase().trim()
						+ tableName.substring(1);
				table.setCamelCaseName(tableName);
				List<Relationship> relationships = getExportedKeys(catalog,
						rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"));

				if (relationships != null && relationships.size() > 1) {

					for (int i = 0; i < relationships.size() - 1; i++) {
						int k = 1;
						for (int j = i + 1; j < relationships.size(); j++) {

							Relationship relationship1 = relationships.get(i);

							Relationship relationship2 = relationships.get(j);

							if (relationship1.getCamelCaseName()
									.equalsIgnoreCase(
											relationship2.getCamelCaseName())) {

								relationship2.setCamelCaseName(relationship2
										.getCamelCaseName() + k);

								k = k + 1;
							}
						}
					}
				}
				table.setRelationships(relationships);
				table.setAttributes(getColumns(catalog,
						rs.getString("TABLE_SCHEM"),
						rs.getString("TABLE_NAME"), null));
				int i = 0;
				for (Attribute attribute : table.getAttributes()) {

					if (attribute.isPrimaryKey()) {
						i++;
					}
				}
				if (i > 1) {
					table.setEmbeddableFlag(true);
				}
				if (i == 0) {
					table.setIdentity(true);
				}
				tables.add(table);
			}

			rs.close();
		} catch (SQLException e) {
			logger.error("Error Retrieving Tables" + e.getMessage(), e);
			throw new DataAccessException("Error Retrieving Tables"
					+ e.getMessage(), e);
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new DataAccessException("Error closing connection"
						+ e.getMessage(), e);
			}
		}
		return tables;
	}

	@Transactional(readOnly = true)
	public List<Attribute> getColumns(String catalog, String schemaPattern,
			String tableNamePattern, String columnNamePattern)
			throws DataAccessException {
		List<Attribute> attributes = new ArrayList<Attribute>();
		Connection connection = null;
		try {
			logger.info("**Columns**");
			connection = externalconnection.getConnection();
			DatabaseMetaData data = connection.getMetaData();
			ResultSet rs = data.getColumns(catalog, schemaPattern,
					tableNamePattern, columnNamePattern);
			List<ForeignKey> foreignKeys = getImportedKeys(catalog,
					schemaPattern, tableNamePattern);

			if (foreignKeys != null && foreignKeys.size() > 1) {
				for (int i = 0; i < foreignKeys.size() - 1; i++) {
					int k = 1;
					ForeignKey key1 = foreignKeys.get(i);
					for (int j = i + 1; j < foreignKeys.size(); j++) {
						ForeignKey key2 = foreignKeys.get(j);
						if (key1.getCamelCaseName().equalsIgnoreCase(
								key2.getCamelCaseName())) {
							key2.setCamelCaseName(key2.getCamelCaseName() + k);

							k = k + 1;
						}
					}
				}
			}
			List<String> primaryKeys = getPrimaryKeys(catalog, schemaPattern,
					tableNamePattern);

			while (rs.next()) {
				Attribute attribute = new Attribute();
				String columnName = rs.getString("COLUMN_NAME").trim();
				attribute.setColumnName(columnName);
				columnName = columnName.replaceAll("[^a-zA-Z 0-9_]+", "")
						.replaceAll("\\s+", "");
				attribute.setName(columnName);
				columnName = columnName.substring(0, 1).toLowerCase().trim()
						+ columnName.substring(1);
				logger.info(columnName);
				attribute.setCamelCaseName(columnName);
				attribute.setDataType(getJavaTypeName(rs.getInt("DATA_TYPE")));

				if (primaryKeys != null && primaryKeys.size() > 0) {
					for (String primaryKey : primaryKeys) {
						if (primaryKey.trim().equalsIgnoreCase(
								rs.getString("COLUMN_NAME"))) {
							attribute.setPrimaryKey(true);
						}
					}
				}

				if (foreignKeys != null && foreignKeys.size() > 0) {
					for (ForeignKey foreignKey : foreignKeys) {
						if (foreignKey
								.getCompareColumn()
								.trim()
								.equalsIgnoreCase(
										rs.getString("COLUMN_NAME").trim())) {
							attribute.setForeignKey(true);
							attribute.setReferenceTableName(foreignKey
									.getTableName());
							attribute.setCamelCaseTableName(foreignKey
									.getCamelCaseName());
						}
					}
				}
				attributes.add(attribute);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("Error Retrieving Columns" + e.getMessage(), e);
			throw new DataAccessException("Error Retrieving Columns"
					+ e.getMessage(), e);
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new DataAccessException("Error closing connection"
						+ e.getMessage(), e);
			}
		}

		logger.info("***************End***********************");
		return attributes;
	}

	@Transactional(readOnly = true)
	public List<Relationship> getExportedKeys(String catalog, String schema,
			String table) throws DataAccessException {
		List<Relationship> relationships = new ArrayList<Relationship>();
		Connection connection = null;
		try {
			logger.info("**Relationships**");
			connection = externalconnection.getConnection();
			DatabaseMetaData data = connection.getMetaData();
			ResultSet rs = data.getExportedKeys(catalog, schema, table);
			while (rs.next()) {
				Relationship relationship = new Relationship();
				String tableName = rs.getString("FKTABLE_NAME");
				tableName = tableName.replaceAll("\\s", "");
				tableName = tableName.substring(0, 1).toUpperCase().trim()
						+ tableName.substring(1);
				logger.info(tableName);
				relationship.setType(tableName);
				String name = rs.getString("FKTABLE_NAME").trim();
				name = name.replaceAll("\\s", "");
				if (!name.substring(name.length() - 1).equalsIgnoreCase("s"))
					relationship.setName(name + "s");
				else
					relationship.setName(name);
				name = name.substring(0, 1).toLowerCase().trim()
						+ name.substring(1);
				if (!name.substring(name.length() - 1).equalsIgnoreCase("s"))
					relationship.setCamelCaseName(name + "s");
				else
					relationship.setCamelCaseName(name);
				relationship.setColumnName(rs.getString("FKCOLUMN_NAME"));
				relationships.add(relationship);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("Error Retrieving Foreign Key Table" + e.getMessage(),
					e);
			throw new DataAccessException("Error Retrieving  Foreign Key Table"
					+ e.getMessage(), e);
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new DataAccessException("Error closing connection"
						+ e.getMessage(), e);
			}
		}
		return relationships;
	}

	@Transactional(readOnly = true)
	public List<ForeignKey> getImportedKeys(String catalog, String schema,
			String table) throws DataAccessException {
		List<ForeignKey> keys = new ArrayList<ForeignKey>();
		Connection connection = null;
		try {
			connection = externalconnection.getConnection();
			DatabaseMetaData data = connection.getMetaData();
			ResultSet rs = data.getImportedKeys(catalog, schema, table);
			while (rs.next()) {
				ForeignKey key = new ForeignKey();
				String tableName = rs.getString("PKTABLE_NAME").trim();
				tableName = tableName.replaceAll("\\s", "");
				tableName = tableName.substring(0, 1).toUpperCase().trim()
						+ tableName.substring(1);
				key.setTableName(tableName);
				key.setCompareColumn(rs.getString("FKCOLUMN_NAME"));
				String name = rs.getString("PKTABLE_NAME").trim();
				name = name.replaceAll("\\s", "");
				name = name.substring(0, 1).toLowerCase().trim()
						+ name.substring(1);
				key.setCamelCaseName(name);

				keys.add(key);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error(
					"Error Retrieving Foreign Key Columns" + e.getMessage(), e);
			throw new DataAccessException(
					"Error Retrieving Foreign Key Columns" + e.getMessage(), e);
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new DataAccessException("Error closing connection"
						+ e.getMessage(), e);
			}
		}
		return keys;
	}

	@Transactional(readOnly = true)
	public List<String> getPrimaryKeys(String catalog, String schema,
			String table) throws DataAccessException {
		
		if(table.equalsIgnoreCase("Account"))
		{
			System.out.println();
		}
		List<String> keys = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = externalconnection.getConnection();
			DatabaseMetaData data = connection.getMetaData();
			ResultSet rs = data.getPrimaryKeys(catalog, schema, table);
			while (rs.next()) {
				keys.add(rs.getString("COLUMN_NAME"));
			}
			rs.close();
		} catch (SQLException e) {
			logger.error(
					"Error Retrieving Primary Key columns" + e.getMessage(), e);
			throw new DataAccessException(
					"Error Retrieving  Primary Key columns" + e.getMessage(), e);
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new DataAccessException("Error closing connection"
						+ e.getMessage(), e);
			}
		}

		return keys;

	}

	@Transactional(readOnly = true)
	public String convertSQLTypesToJava(String dataType) {
		if (dataType.equalsIgnoreCase("bigint")) {
			dataType = "long";
		}

		if (dataType.equalsIgnoreCase("binary")
				|| dataType.equalsIgnoreCase("timestamp")
				|| dataType.equalsIgnoreCase("udt")
				|| dataType.equalsIgnoreCase("varbinary")
				|| dataType.equalsIgnoreCase("image")) {
			dataType = "byte[]";
		}

		if (dataType.equalsIgnoreCase("bit")
				|| dataType.equalsIgnoreCase("Flag")) {
			dataType = "boolean";
		}

		if (dataType.equalsIgnoreCase("char")
				|| dataType.equalsIgnoreCase("nchar")
				|| dataType.equalsIgnoreCase("ntext")
				|| dataType.equalsIgnoreCase("nvarchar")
				|| dataType.equalsIgnoreCase("bit")
				|| dataType.equalsIgnoreCase("text")
				|| dataType.equalsIgnoreCase("uniqueidentifier")
				|| dataType.equalsIgnoreCase("varchar")
				|| dataType.equalsIgnoreCase("xml")
				|| dataType.equalsIgnoreCase("sysname")
				|| dataType.equalsIgnoreCase("Name")
				|| dataType.equalsIgnoreCase("Email")) {
			dataType = "String";
		}

		if (dataType.equalsIgnoreCase("date")
				|| dataType.equalsIgnoreCase("datetime")
				|| dataType.equalsIgnoreCase("datetimeoffset (2)")
				|| dataType.equalsIgnoreCase("datetime2")
				|| dataType.equalsIgnoreCase("smalldatetime")
				|| dataType.equalsIgnoreCase("time")) {
			dataType = "Date";
		}
		if (dataType.equalsIgnoreCase("decimal")
				|| dataType.equalsIgnoreCase("float")
				|| dataType.equalsIgnoreCase("money")
				|| dataType.equalsIgnoreCase("numeric")
				|| dataType.equalsIgnoreCase("real")
				|| dataType.equalsIgnoreCase("smallmoney")) {
			dataType = "double";
		}
		if (dataType.equalsIgnoreCase("int")
				|| dataType.equalsIgnoreCase("smallint")
				|| dataType.equalsIgnoreCase("tinyint")
				|| dataType.equalsIgnoreCase("int identity")
				|| dataType.equalsIgnoreCase("phone")) {
			dataType = "int";
		}
		return dataType;
	}

	@Transactional(readOnly = true)
	public String getJavaTypeName(int type) {
		switch (type) {
		case Types.BIT:
			return "Boolean";
		case Types.TINYINT:
			return "Integer";
		case Types.SMALLINT:
			return "Integer";
		case Types.INTEGER:
			return "Integer";
		case Types.BIGINT:
			return "Integer";
		case Types.FLOAT:
			return "Double";
		case Types.REAL:
			return "Double";
		case Types.DOUBLE:
			return "Double";
		case Types.NUMERIC:
			return "Double";
		case Types.DECIMAL:
			return "Double";
		case Types.CHAR:
			return "String";
		case Types.VARCHAR:
			return "String";
		case Types.LONGVARCHAR:
			return "String";
		case Types.DATE:
			return "Date";
		case Types.TIME:
			return "Time";
		case Types.TIMESTAMP:
			return "Date";
		case Types.BINARY:
			return "byte[]";
		case Types.VARBINARY:
			return "byte[]";
		case Types.LONGVARBINARY:
			return "byte[]";
		case Types.NULL:
			return "null";
		case Types.OTHER:
			return "Object";
		case Types.JAVA_OBJECT:
			return "Object";
		case Types.DISTINCT:
			return "Integetr";
		case Types.STRUCT:
			return "Srtuct";
		case Types.ARRAY:
			return "Array";
		case Types.BLOB:
			return "Blob";
		case Types.CLOB:
			return "Clob";
		case Types.REF:
			return "Ref";
		case Types.DATALINK:
			return "URL ";
		case Types.BOOLEAN:
			return "Boolean";
		case Types.ROWID:
			return "RowId";
		case Types.NCHAR:
			return "String";
		case Types.NVARCHAR:
			return "String";
		case Types.LONGNVARCHAR:
			return "String";
		case Types.NCLOB:
			return "Clob";
		case Types.SQLXML:
			return "SQLXML";
		}

		return "Object";
	}
}
