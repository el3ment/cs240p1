
package framework;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An abstract Model template from which all the custom models inherit.
 * <p>
 * It is used like :
 * <code> public class CustomModel extends Model<CustomModelRecord>{ }</code>
 * <p>
 * This allows us to perform the generic SQL statement queries that are 
 * shared between all the models like find() or findBy() or findAll() etc.
 * Ideally, we would define RELATIONSHIPS in the custom models
 * and leave as much of the SQL requests to the generic model.
 */
public abstract class Model<ChildModelRecord extends ModelRecord> {
	
	@SuppressWarnings("serial")
	public class ResultRow extends HashMap<String, String>{ }
	@SuppressWarnings("serial")
	public class ResultList extends ArrayList<ChildModelRecord>{ }
	
	/**
	 * Returns the table name for the model. This is required by subclasses.
	 *
	 * @return	name of the table
	 */
	public abstract String getName();
	
	/**
	 * Generates an instance of ChildModelRecord. This has to be 
	 * implemented by subclasses.
	 *
	 * @return	instance of the template ChildModelRecord
	 */
	public abstract ChildModelRecord generate(ResultRow genericMap);
	
	
	/**
	 * Populated by subclasses, this caches column names so PRAGMA requests are only issued once.
	 */
	private ArrayList<String> columns;
	
	
	/**
	 * Returns a ResultList response from a fully qualified SQL statement
	 *
	 * @return	instance of Database
	 */
	public ResultList findAllByQuery(String query) throws SQLException{

		// Execute query, get metaData, create blank containers for data
		ResultSet rs = Database.getInstance().execute(query);
		ResultSetMetaData columns = rs.getMetaData();
		int totalColumns = columns.getColumnCount();
		ResultList list = new ResultList();
		ResultRow map = new ResultRow();
		
		// Loop through each row, and each column, creating a
		// generic map which generate() can use to fabricate a ChildModel
		while(rs.next()){
			for(int i = 1; i <= totalColumns; ++i){
				map.put(columns.getColumnName(i), rs.getString(i));
			}
			list.add(generate(map));
		}
		
		// If the list is populated, return it
		// otherwise return null
		if(!list.isEmpty())
			return list;
		else
			return null;
    
	}
	
	/**
	 * Generates a formatted list of columns for the current model used
	 * in SELECT statements.
	 *
	 * @return	column names separated by spaces with alias
	 * 			(ex. "fields.id" becomes "fields_id")
	 */
	public String getColumnQuery() throws SQLException{
		String queryPartial = "";
		String separator = "";
		ArrayList<String> columns = this.getColumns();
		
		for(int i = 0; i < columns.size(); i++){
			separator = ((i < columns.size() - 1) ? ", " : "");
			queryPartial += this.getName() + "." + columns.get(i) 
					+ " as " + this.getName() 
					+ "_" + columns.get(i) + separator;
		}
		
		return queryPartial;
	}
	
	/**
	 * Saves a ChildModelRecord instance back into the database
	 *
	 * @return	the id of the last modified record
	 */
	public int save(ChildModelRecord child) throws SQLException{
			return this.insert(child);
	}
	
	/**
	 * Executes update using child record id
	 *
	 * @return	the id of the record to be updated
	 * @throws SQLException 
	 */
	public void updateField(ChildModelRecord child, String fieldname, String newValue)
			throws SQLException {
		String query = "UPDATE '" + this.getName() 
				+ "' SET " + fieldname + " = " + 
					(newValue == null 
						? "null" 
						: "'" + newValue + "'") 
				+ " WHERE id = '" + child.id + "'";
		Database.getInstance().execute(query, false);
	}
	
	

	/**
	 * Adds record to database
	 * 
	 * @param	child		a childRecord to be inserted
	 * @return				the table name for the model
	 */
	public int insert(ChildModelRecord child) throws SQLException{
		String query = "INSERT INTO \"" + this.getName() + "\" (";
		String valueQuery = "";
		ArrayList<String> columns = this.getColumns();
		
		int id = 0;
		boolean isIncluded;
		boolean isFirst = true;
		
		for(int i = 0; i < columns.size(); i++){
			
			isIncluded = false;
			
			try{
				Field field = child.getClass().getField(columns.get(i));
				if(!columns.get(i).equals("id")){
					valueQuery += (!isFirst ? ", " : "") 
							+ (field.get(child) != null 
								? "'" + field.get(child) + "'" 
								: "null");
					isIncluded = true;
				}
			}catch(Exception e){
				System.out.println(columns.get(i) + " error" + e + this.getName());
				e.fillInStackTrace();
			}
			
			if(isIncluded){
				query += (!isFirst ? ", " : "") + columns.get(i);
				isFirst = false;
			}
		}
		
		query += ") VALUES (" + valueQuery + ");";
		
		Database.getInstance().execute(query, false);
		id = Database.getInstance()
				.execute("SELECT last_insert_rowid() as id")
				.getInt("id");
		child.id = id;
		
		return id;
	}
	
	/**
	 * Dynamically requests the columns from the current table.
	 *
	 * @return	an ArrayList<String> of column names useful for 
	 * 			building queries.
	 */
	public ArrayList<String> getColumns() throws SQLException{
		if(this.columns == null){
			ResultSet rs = Database.getInstance()
				.execute("PRAGMA table_info(\""+ this.getName() +"\")");
			this.columns = new ArrayList<String>();
			
			while(rs.next()){
				columns.add(rs.getString("name"));
			}
		}
		
		return this.columns;
	}
	
	/**
	 * Searches for a record of the current model with an id()
	 *
	 * @return	instance of ChildModelRecord
	 */
	public ChildModelRecord find(int id) throws SQLException{
		return this.findFirstBy("id", id + "");
	}
	
	/**
	 * Executes a simple fieldName = searchValue WHERE clause on the current model
	 *
	 * @return	a ResultList of ChildModelRecords
	 */
	public ResultList findAllBy(String fieldName, String searchValue) throws SQLException{
		
		ResultList list = this.findAllByQuery(
				"SELECT " + this.getColumnQuery() 
				+ " FROM " + this.getName() 
				+ " WHERE " + fieldName + " LIKE '" + searchValue + "'");
		
		return list;
	}
	
	/**
	 * Finds records using a custom WHERE clause
	 *
	 * @return	an instance of ChildModelRecord
	 */
	public ChildModelRecord findFirstByClause(String clause) throws SQLException{
		
		ResultList list = this.findAllByQuery(this.buildFindAllQuery() 
				+ " WHERE " + clause + " LIMIT 0, 1");
		
		if(list == null || list.isEmpty())
			return null;
		else
			return list.get(0);
	}
	
	/**
	 * Executes a simple fieldName = searchValue query on the current model, and limits to 1 record
	 *
	 * @return	instance of ChildModelRecord
	 */
	public ChildModelRecord findFirstBy(String fieldName, String searchValue) throws SQLException{
		return this.findFirstByClause(fieldName + " LIKE '" + searchValue + "'");
	}
	
	/**
	 * Finds all records of the current model.
	 *
	 * @return	ResultList of all records for current Model.
	 */
	public ResultList findAll() throws SQLException{
		ResultList list = this.findAllByQuery(this.buildFindAllQuery());
		
		return list;
	}
	
	/**
	 * Builds basic select query without WHERE, JOIN, or LIMIT clauses
	 *
	 * @return	partial SELECT statement
	 */
	public String buildFindAllQuery() throws SQLException{
		
		return "SELECT " + this.getColumnQuery() + " FROM " + this.getName();
	}

}
