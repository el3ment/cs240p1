package server;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import shared.Request;
import controller.FieldController;
import controller.ImageController;
import controller.ImageController.InvalidFieldCountOrFormatException;
import controller.ProjectController;
import controller.UserController;
import controller.UserController.BatchAlreadyAssignedException;
import controller.UserController.InvalidUsernameOrPasswordException;	
import framework.Controller.InvalidInputException;
import framework.Database;
import model.*;

/**
 * The Public API for RecordIndexer containing methods
 * that will interact with the RecordIndexer Database.
 */
public class API {
	
	@SuppressWarnings("serial")
	public static class NoResultsFoundException extends Exception{ }
	
	private UserController userController = new UserController();
	private ProjectController projectController = new ProjectController();
	private ImageController imageController = new ImageController();
	private FieldController fieldController = new FieldController();
	
	
	
	
	/**
	 * Validates a user against a database of usernames and passwords, returns "TRUE" 
	 * followed by the user's data.
	 * <p>
	 * Does not throw a InvalidUsernameOrPasswordException, but rather returns "FALSE"
	 * in the event of a login error.
	 * <p>
	 * <code>
	 * validateUser("testusername1", "password123") = "TRUE\nTest\nUser\n12";
	 * </code>
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @return				string a serialization of first name, lastName 
	 * 						and number of indexed records separated by newlines
	 * @throws InvalidInputException 
	 */
	public Request.ValidateUserResponse validateUser(
			Request.ValidateUserRequest params) 
			throws SQLException, InvalidInputException{
		
		UserModel.User user = userController.login(params.username, params.password);
		Request.ValidateUserResponse response = new Request.ValidateUserResponse();
		
		if(user == null || user.username.isEmpty())
			response.isValid = false;
		else{
			response.isValid = true;
			response.firstName = user.firstname;
			response.lastName = user.lastname;
			response.numRecords = user.indexedrecords;
		}
		
		return response;
				
	}
	
	/**
	 * Returns a list of all projects in the database.
	 * <p>
	 * <code>
	 * getProjects("testusername1", "password123") = "1\nProject1\n2\nProject2";
	 * </code>
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @return				a serialization of id, and title for each project returned
	 * @throws InvalidInputException 
	 */
	public Request.GetProjectsResponse getProjects(
			Request.GetProjectsRequest params)
			throws SQLException, InvalidUsernameOrPasswordException, InvalidInputException{
		
		userController.requireLogin(params.username, params.password);
		Request.GetProjectsResponse response = new Request.GetProjectsResponse();
		
		ProjectModel.ResultList projects = projectController.projectModel.findAll();
		
		if(projects != null && projects.size() > 0){		
			for(int i = 0; i < projects.size(); i++){
				Request.GetProjectsResponse.ProjectResponse project 
					= new Request.GetProjectsResponse.ProjectResponse();
				project.projectId = projects.get(i).id;
				project.title = projects.get(i).title;
				response.projects.add(project);
			}
		
		}
		
		return response;
	}

	/**
	 * Gets the first image in a project to display as a sample.
	 * <p>
	 * <code>
	 * getSampleImage("testusername1", "password123", 23) = "http://google.com/sample1.jpg\n";
	 * </code>
	 * <p>
	 * In the event that the project does not exist, the username or 
	 * password are incorrect or the project does not contain any images,
	 * <code>getSampleImage</code> will throw an exception
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @param	projectId	internal id of the project
	 * @return				URL of the first image in the project
	 * @throws Exception 
	 */
	public Request.GetSampleImageResponse getSampleImage(
			Request.GetSampleImageRequest params)
			throws Exception{
		
		userController.requireLogin(params.username, params.password);
		Request.GetSampleImageResponse response = new Request.GetSampleImageResponse();
		
		
		ImageModel.Image sampleImage 
			= imageController.getSampleImageForProject(
				Integer.parseInt(params.projectId));
		if(sampleImage != null){
			response.url = sampleImage.file;
		}else{
			throw new Exception("Invalid project id");
		}
		
		return response;
	}
	
	/**
	 * Simultaneously assigns the user specified to the particular image 
	 * (or batch) and downloads the data needed.
	 * <code>
	 * downloadBatch("testuser1", "password123", 2) = TODO
	 * </code>
	 * If the user has already been assigned a batch, requested an invalid projectId, or
	 * username/password combination - downloadBatch will throw an exception.
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @return				string a serialization of TODO
	 * @throws NoResultsFoundException 
	 * @throws NumberFormatException 
	 * @throws InvalidInputException 
	 */
	public Request.DownloadBatchResponse downloadBatch(
			Request.DownloadBatchRequest params)
			throws SQLException, 
				InvalidUsernameOrPasswordException,
				BatchAlreadyAssignedException,
				NumberFormatException,
				NoResultsFoundException, InvalidInputException{
		
		userController.requireLogin(params.username, params.password);
		Request.DownloadBatchResponse response = new Request.DownloadBatchResponse();
		
		ImageModel.Image batch = userController.assignBatch(Integer.parseInt(params.projectId));
		ProjectModel.Project project = ProjectModel.getInstance().find(batch.project_id);
		ArrayList<FieldModel.Field> fields 
			= FieldModel
				.getInstance()
				.findAllBy("project_id", batch.project_id + "");
		
		response.batchId = batch.id;
		response.projectId = project.id;
		response.imageURL = batch.file;
		response.firstYCoord = project.firstycoord;
		response.recordHeight = project.recordheight;
		response.numRecords = project.recordsperimage;
		response.numFields = fields.size();
		
		for(int i = 0; i < fields.size(); i++){
			Request.DownloadBatchResponse.FieldResponse field =
				new Request.DownloadBatchResponse.FieldResponse();
			
			field.fieldId =  fields.get(i).id;
			field.fieldNum = fields.get(i).columnindex;
			field.fieldTitle = fields.get(i).title;
			field.helpUrl = fields.get(i).helphtml;
			field.xCoord = fields.get(i).xcoord;
			field.pixelWidth = fields.get(i).width;
			field.knownValuesUrl = fields.get(i).knowndata;
			
			response.fields.add(field);
		}
		
		return response;
	}
	
	/**
	 * Returns all of the fields for a given project, or if projectId is left blank,
	 * the entire database
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @return				string a serialization of projectId, fieldId, and
	 * 						fieldTitle for each field returned
	 * @throws InvalidInputException 
	 */
	public Request.GetFieldsResponse getFields(
			Request.GetFieldsRequest params)
			throws SQLException, InvalidUsernameOrPasswordException, InvalidInputException{
		
		userController.requireLogin(params.username, params.password);
		Request.GetFieldsResponse response = new Request.GetFieldsResponse();

		FieldModel.ResultList fields;
		
		if(params.projectId.isEmpty())
			fields = fieldController.fieldModel.findAll();
		else
			fields = fieldController.findAllByProjectId(Integer.parseInt(params.projectId));
		
		if(fields != null && fields.size() > 0){
		
			for(int i = 0; i < fields.size(); i++){
				Request.GetFieldsResponse.FieldResponse field = 
					new Request.GetFieldsResponse.FieldResponse();
				
				field.projectId = fields.get(i).project_id;
				field.fieldId = fields.get(i).id;
				field.title = fields.get(i).title;
				
				response.fields.add(field);
			}
		
		}
		
		return response;
	}
	
	/**
	 * Save a completed batch file.
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @param	batchId		the id of the batch
	 * @param	fieldValues	a combo of fields followed by values (ex. "FirstName, LastName, 
	 * 						OtherField;SecondRecordFirstName...etc")
	 * @return				string a serialization of first name, lastName and number of 
	 * 						indexed records separated by newlines
	 * @throws InvalidFieldCountOrFormatException 
	 * @throws InvalidInputException 
	 */
	public Request.SubmitBatchResponse submitBatch(
			Request.SubmitBatchRequest params)
			throws SQLException, 
				InvalidUsernameOrPasswordException, 
				InvalidFieldCountOrFormatException, InvalidInputException{
		userController.requireLogin(params.username, params.password);
		// Split fieldValues by ; then again by ,
		
		ArrayList<ArrayList<String>> formatted = imageController.formatBatch(params.fieldValues);
		
		imageController.submitBatch(
				Integer.parseInt(params.batchId), formatted, userController._currentUser);
		
		return new Request.SubmitBatchResponse();
		
	}
	
	/**
	 * Searches the specified field titles for any matching value from a list
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @param	fields		comma separated list of fields to be searched
	 * @param	values		comma separated list of values to be searched
	 * @return				string a serialization of image id, image url, 
	 * 						record number, and field id
	 * @throws InvalidUsernameOrPasswordException 
	 * @throws SQLException 
	 * @throws NoResultsFoundException 
	 * @throws InvalidInputException 
	 */
	public Request.SearchResponse search(
			Request.SearchRequest params)
			throws NoResultsFoundException, 
				SQLException, 
				InvalidUsernameOrPasswordException, InvalidInputException{
		
		userController.requireLogin(params.username, params.password);
		
		ArrayList<Request.SearchResponse.SearchResult> results = 
			fieldController.search(params.fields, params.values);
		
		Request.SearchResponse response = new Request.SearchResponse();
		
		for(int i = 0; i < results.size(); i++){
			response.results.add(results.get(i));
		}
		
		return response;
		
	}
	
	/**
	 * Searches records for a particular value
	 *
	 * @param	username	a string of the username
	 * @param	password	a string of the password
	 * @param	url			a url for an external image
	 * @return				File() reference for the local 
	 * 						cached version of the file	
	 * @throws InvalidInputException 
	 */
	public File downloadFile(
			String username, String password, String url)
			throws SQLException, InvalidUsernameOrPasswordException, InvalidInputException{
		userController.requireLogin(username, password);
		// Split fieldValues by ; then again by ,
		// send to ImageController.submitBatch(batchId, ArrayList<ArrayList<String>> fieldValues);
		return null;
		
	}
}
