package me.kw.sfis;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.Bukkit;

public class SQLConnector {

	public Connection connection;
	private String host;
	private String database;
	private String username;
	private String password;

	public SQLConnector(String host, String database, String username, String password) {
		this.host = host;
		this.database = database;
		this.username = username;
		this.password = password;
	}
	
	public boolean checkDetails(boolean print){
		if(host == null || database == null || username == null || password == null ||
				host == "" || database == "" || username == "" || password == ""){
			if(print){
			Bukkit.getConsoleSender().sendMessage("§csfi§7$ §4Severe error whilst attempting to login to SQL Database:"
					+ "\n    §eDetailed error walkthrough:"
					+ "\n        §aSucessfully initialized SQLConnector!"
					+ "\n        §eAttempting to build login url..."
					+ "\n        §3URL: jdbc:mysql://" + host + "/" + database 
					+ "\n        §eisHostNull: " + (host == null || host == "") 
					+ "\n        §eisDatabaseNull: " + (database == null || database == "")
					+ "\n        §eisUsernameNull: " + (username == null || username == "")
					+ "\n        §eisPasswordNull: " + (password == null || password == "")
					+ "\n        §cOne or multiple of the required fields is null in config.yml");
			}
			return true;
		}
		return false;
	}

	public Runnable openConnection = new Runnable(){
		@Override
		public void run() {
			openConnection();
		}
	};

	private void openConnection(){
		try {
			if ((connection != null) && (!connection.isClosed())) {
			  return;
			}
			synchronized (this) {
	      if ((connection != null) && (!connection.isClosed())) {
	        return;
	      }
	      Class.forName("com.mysql.jdbc.Driver");
	      connection = java.sql.DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, username, password);
	    }
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
