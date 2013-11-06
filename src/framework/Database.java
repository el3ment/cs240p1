package framework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Essentially Database is a singleton for controlling database access.
 * This allows us to maintain a single connection rather than opening and closing one for each model
 * request.
 * <p>
 * It's main purpose is to abstract the nuts and bolts of the SQL request from each of the models
 */
public class Database {
	
	private static Database instance = null;
	private Connection connection;
	
	protected Database(){ }

	/**
	 * Create or get an instance of the Database singleton
	 *
	 * @return	instance of Database
	 */
	public static Database getInstance(){
		if(instance == null)
			instance = new Database();
		
		return instance;
	}
	
	/**
	 * Executes an SQL statement, returns a result set
	 *
	 * @return	ResultSet of results
	 */
	public ResultSet execute(String query) throws SQLException{
		return this.execute(query, true);
	}
	
	/**
	 * Returns a ResultSet for an SQL expression.
	 *
	 * @param	query	SQL statement
	 * @return			ResultSet for response
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public ResultSet execute(String query, boolean doesReturnResults) throws SQLException{
		
		try{
			Class.forName("org.sqlite.JDBC");
			
			// Create the connection if it's not already there
			if(connection == null)
				connection = DriverManager.getConnection(
						"jdbc:sqlite:./database/indexer_server.sqlite");
			
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			
			//System.out.println("Trying to run query : " + query);
			
			ResultSet rs = null;
			if(doesReturnResults)
				rs = statement.executeQuery(query);
			else
				statement.executeUpdate(query);
			
			return rs;
			
		}catch(ClassNotFoundException e){
			System.err.println("SQLite JDBC Class not found");
		}
		
		return null;
	}
	
	
	/**
	 * Empties all tables permanently
	 *
	 */
	public void empty(){
		
		try {
			this.execute("DELETE FROM \"values\"", false);
			this.execute("DELETE FROM \"fields\"", false);
			this.execute("DELETE FROM \"images\"", false);
			this.execute("DELETE FROM \"records\"", false);
			this.execute("DELETE FROM \"users\"", false);
			this.execute("DELETE FROM \"projects\"", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
