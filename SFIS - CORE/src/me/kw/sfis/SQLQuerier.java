package me.kw.sfis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLQuerier {
	
	public static ResultSet safeQuery(String query){
		PreparedStatement prep;
		try {
			prep = Core.getConnection().connection.prepareStatement(query);
			return prep.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void safeUpdate(String query){
		PreparedStatement prep;
		try {
			prep = Core.getConnection().connection.prepareStatement(query);
			prep.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
