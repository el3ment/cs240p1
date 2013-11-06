package model;

import java.sql.SQLException;

import org.w3c.dom.Element;

import server.DataImporter;
import framework.Model;
import framework.ModelRecord;

/**
 * The corresponding model to User records.
 * These classes inherit from Model<Record> to give them all the basic
 * find, findBy, findAll, etc. type functionality. These functions
 * rely on a few configurations on the subclasses - namely getName()
 * returns the name of the table.
 * <p>
 * In the future, it would be advisable to define relationships here
 * as well - perhaps in a function like getRelationships() that 
 * returns a HashMap of table and joinIds, thus automatically generating nested
 * records as needed.
 * <p>
 * Although these should be singletons by nature, by design they are not. This is to
 * help with testing, possible eventual threading, and as bit of a shortcut for the time being.
 * Because these models do NOT contain state, having multiple instances of this class does
 * not hurt significantly.
 *
 */
public class UserModel extends Model<UserModel.User>{
	
	/**
	 * Abstract getName function to return the table name
	 * 
	 * @return				the table name for the model
	 */
	public String getName(){ return "users"; }
	
	// Make it a singleton
	private static UserModel instance = null;
	protected UserModel(){ }

	/**
	 * Create or get an instance of the Model
	 *
	 * @return	instance of the model
	 */
	public static UserModel getInstance(){
		if(instance == null)
			instance = new UserModel();
		
		return instance;
	}
	
	/**
	 * The UserModel.User class represents a User record from the database
	 */
	public static class User extends ModelRecord{
		
		public String username;
		public String password;
		public String firstname;
		public String lastname;
		public String email;
		public Integer indexedrecords;
		
		public User(Element item) throws SQLException{
			username = DataImporter.getValue(
				(Element) item.getElementsByTagName("username").item(0)); 
			password = DataImporter.getValue(
				(Element) item.getElementsByTagName("password").item(0)); 
			firstname = DataImporter.getValue(
				(Element) item.getElementsByTagName("firstname").item(0)); 
			lastname = DataImporter.getValue(
				(Element) item.getElementsByTagName("lastname").item(0)); 
			email = DataImporter.getValue(
				(Element) item.getElementsByTagName("email").item(0)); 
			indexedrecords = Integer.parseInt(DataImporter.getValue(
				(Element) item.getElementsByTagName("indexedrecords").item(0))); 
			UserModel.getInstance().save(this);
		}
		
		public User(){ }
	}

	/**
	 * Generates a new instance of User given a map of keys and values.
	 * <p>
	 * This function avoids the problem with creating a new instance of a non-static template
	 * type in the parent Model{@literal <RecordType>} class. It might be possible to do this
	 * dynamically using the Reflection API
	 *
	 * @return	instance of User
	 */
	public User generate(ResultRow genericRow){
		User user = new User();
		
		user.username = genericRow.get("users_username");
		user.password = genericRow.get("users_password");
		user.firstname = genericRow.get("users_firstname");
		user.lastname = genericRow.get("users_lastname");
		user.email = genericRow.get("users_email");
		user.id = Integer.parseInt(genericRow.get("users_id"));
		user.indexedrecords = Integer.parseInt(
			(genericRow.get("users_indexedrecords") == null 
				? "0" 
				: genericRow.get("users_indexedrecords"))
		);

		return user;
	}

}
