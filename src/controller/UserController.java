package controller;

import java.sql.SQLException;

import framework.Controller;
import server.API;
import server.API.NoResultsFoundException;
import model.*;

/**
 * Handles the controller logic for the User model. This class should abstract any logic performed
 * on data from the model. The models should only handle rudimentary logic, database relationships
 * and a secondary form of validation
 */
public class UserController extends Controller{

	// Custom Exceptions
	// Hides warning regarding hash code for custom exceptions
	@SuppressWarnings("serial")
	public class InvalidUsernameOrPasswordException extends Exception{ }
	@SuppressWarnings("serial")
	public class BatchAlreadyAssignedException extends Exception{ }
	
	public UserModel.User _currentUser; // populated after a successful login() or requireLogin()
	
	// Models 
	public UserModel userModel = UserModel.getInstance();
	public ImageModel imageModel = ImageModel.getInstance();
	public ProjectModel projectModel = ProjectModel.getInstance();
	
	
	/**
	 * Tests a username and password against the database and returns the user if found.
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @return				{@code UserModel.User} of the found user or null if not found
	 * @throws InvalidInputException 
	 */
	public UserModel.User login(String username, String password) 
			throws SQLException, InvalidInputException{
		
		this.requireValidNotEmpty(username);
		this.requireValidNotEmpty(password);
		
		UserModel.User foundUser = userModel.findFirstBy("username", username);
		
		if(foundUser != null && foundUser.password.equals(password)){
			_currentUser = foundUser;
			return foundUser;
		}else{
			return null;
		}
		
	}
	
	
	/**
	 * Provides similar functionality to UserController.login, but throws an
	 * exception if invalid.
	 * <p>
	 * In the API, you'll see that we call userModel.requireLogin() at the 
	 * beginning of each method
	 * then in the API wrapper, we surround all of the calls with a 
	 * <code>try{ }catch(){ }</code> block and handle the 
	 * InvalidUsernameOrPasswordException
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @return				string a serialization of first name, lastName and 
	 * 						number of indexed records separated by newlines
	 * @throws InvalidInputException 
	 */
	public boolean requireLogin(String username, String password) 
			throws SQLException, InvalidUsernameOrPasswordException, InvalidInputException{
		
		this.requireValidNotEmpty(username);
		this.requireValidNotEmpty(password);
		
		UserModel.User foundUser = this.login(username, password);
		if(foundUser != null){
			_currentUser = foundUser;
			return true;
		}else
			throw new InvalidUsernameOrPasswordException();
		
	}
	
	/**
	 * Verifies the user does not already have an assignment, and then assigns 
	 * the user to a batch from t project
	 * <p>
	 * If the user does have an assignment already it throws a 
	 * <code>BatchAlreadyAssignedException</code>
	 * 
	 * TODO : ignore _currentUser, more flexible if passed in as parameter
	 * 
	 * @param	projectId	id of the project
	 * @return				{@code ImageModel.Image} for the batch submitted
	 * @throws NoResultsFoundException 
	 * @throws InvalidInputException 
	 */
	public ImageModel.Image assignBatch(int projectId) 
			throws BatchAlreadyAssignedException, 
			SQLException, NoResultsFoundException, 
			InvalidInputException{
		
		this.requireValidPositive(projectId);
		
		if(imageModel.findFirstBy("user_id", _currentUser.id + "") == null){
			ImageModel.Image image = imageModel.findFirstByClause(
				  "ifnull(images.user_id, '-1') = '-1' "
				+ "AND images.project_id = '" + projectId + "'");
			
			if(image == null)
				throw new API.NoResultsFoundException();
			
			image.user_id = _currentUser.id;
			imageModel.updateField(image, "user_id", _currentUser.id+"");
			
			return image;
			
		}else{
			throw new BatchAlreadyAssignedException();
		}
	}
}
