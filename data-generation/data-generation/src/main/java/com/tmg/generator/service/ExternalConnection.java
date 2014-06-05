package com.tmg.generator.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ExternalConnection")
public class ExternalConnection {

	private static Logger logger = Logger.getLogger(ExternalConnection.class);

	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws DataAccessException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
		} catch (SQLException e) {
			logger.error("Error creating connection" + e.getMessage(), e);
			throw new DataAccessException("Error creating connection"
					+ e.getMessage(), e);
		}
		return conn;
	}

}
