package org.simulatest.insistencelayer.integration;

import static junit.framework.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.junit.Before;
import org.junit.Test;
import org.simulatest.insistencelayer.InsistenceLayerManager;

public class InsistenceLayerIntegrationTest {
	
	private InsistenceLayerManager insistenceLayerManager;
	private Connection connection;
	private Statement statement;
	
	@Before
	public void setup() throws Exception {
		Class.forName("org.h2.Driver");
		connection = DriverManager.getConnection("jdbc:h2:~/.h2/test", "sa", "");
		
		insistenceLayerManager = new InsistenceLayerManager(connection);
		statement = connection.createStatement();
		
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS LOG (NAME VARCHAR(50))");
	}
	
	@Test
	public void integrationTest() throws SQLException {		
		insistenceLayerManager.increaseLevel();
		statement.executeUpdate("INSERT INTO LOG VALUES ('1')");
		assertEquals(1, countFromTableLog());
				
		insistenceLayerManager.increaseLevel();
		statement.executeUpdate("DELETE FROM LOG");
		assertEquals(0, countFromTableLog());
		
		insistenceLayerManager.increaseLevel();
		statement.executeUpdate("INSERT INTO LOG values ('1')");
		statement.executeUpdate("INSERT INTO LOG values ('2')");
		statement.executeUpdate("INSERT INTO LOG values ('3')");
		assertEquals(3, countFromTableLog());
		
		insistenceLayerManager.increaseLevel();
		assertEquals(3, countFromTableLog());
		
		insistenceLayerManager.decreaseLevel();
		assertEquals(3, countFromTableLog());
		
		insistenceLayerManager.decreaseLevel();
		assertEquals(0, countFromTableLog());
		
		insistenceLayerManager.decreaseLevel();
		assertEquals(1, countFromTableLog());
		
		insistenceLayerManager.decreaseLevel();
		assertEquals(0, countFromTableLog());
	}
	
	@Test
	public void resetCurrentLevelTest() throws SQLException {
		insistenceLayerManager.increaseLevel();
		assertEquals(1, insistenceLayerManager.getCurrentLevel());
		statement.executeUpdate("INSERT INTO LOG VALUES ('1')");
		
		insistenceLayerManager.increaseLevel();
		assertEquals(2, insistenceLayerManager.getCurrentLevel());
		
		statement.executeUpdate("INSERT INTO LOG values ('1')");
		statement.executeUpdate("INSERT INTO LOG values ('2')");
		statement.executeUpdate("INSERT INTO LOG values ('3')");
		assertEquals(4, countFromTableLog());
		
		insistenceLayerManager.resetCurrentLevel();
		assertEquals(2, insistenceLayerManager.getCurrentLevel());
		
		assertEquals(1, countFromTableLog());
	}
	
	private int countFromTableLog() throws SQLException {
		ResultSet rs = statement.executeQuery("SELECT COUNT(*) from log");
		rs.next();
		return rs.getInt(1);
	}

}