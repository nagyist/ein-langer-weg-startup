package de.mft.interpretation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

public class DBController {
	
	private static final String URL = "jdbc:mysql://";
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static String hostname;
	private static String username;
	private static String password;
	private static String dbname;
	private Connection connection;
	  
	public DBController() {
		this.setHostname("127.0.0.1:3306/");
		this.setUsername("root");
		this.setPassword("root");
		this.setDbname("dblp");
		this.setConnection(this.connectToDatabase());
	}
	
	public DBController(String hostname, String username, String password, String dbname) {
		this.setHostname(hostname);
		this.setUsername(username);
		this.setPassword(password);
		this.setDbname(dbname);
		this.setConnection(this.connectToDatabase());
	}
	
	public static void main(String[] args) throws SQLException {
		DBController dbc = new DBController();
		System.out.println(dbc.toString());
	}
	
	public int size(ResultSet rs) {
		int size = -1;
    	try {
			rs.last();
			size =  rs.getRow();
			rs.first();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return size;
	}
	
	public ResultSet executeQuery(String sql) {
		try {
			Statement st = this.connection.createStatement();
			return st.executeQuery(sql);			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	
	public int executeUpdateQuery(String sql) {
		try {
			Statement st = this.getConnection().createStatement();
			return st.executeUpdate(sql);			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return 0;
	}
	
	public void countExecutions(int i, int count) {
		String line = null;
		if (i%count==0) {
			line = MessageFormat.format("In Class {0}: {1}x{2} Executed!", this.getClass().getName(), i/count, count);
			System.out.println(line);
		}
	}
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		DBController.hostname = hostname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		DBController.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		DBController.password = password;
	}
	public String getDbname() {
		return dbname;
	}
	public void setDbname(String dbname) {
		DBController.dbname = dbname;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	private Connection connectToDatabase() {
		Connection conn = null;
		try {
			Class.forName(DRIVER).newInstance();
			conn = DriverManager.getConnection(URL + getHostname() + getDbname(), getUsername(),
					getPassword());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	public String toString() {
		return "The DRIVER: " + DRIVER + "\n"
				+ "The URL: " + URL + "\n"
				+ "The Hostname: " + hostname + "\n"
				+ "The USERNAME: " + username + "\n"
				+ "The PASSWORD: " + password + "\n"
				+ "The DATABASE NAME: " + dbname + "\n"
				;
	}
	
}
