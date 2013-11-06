package controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import antlr.StringUtils;
import framework.Controller;
import model.FieldModel;
import model.ImageModel;
import model.ProjectModel;
import model.RecordModel;
import model.UserModel;
import model.ValueModel;

/**
 * Handles the controller logic for images/batches including formatting and 
 * sanitation of data entry.
 */
public class ImageController extends Controller{
	
	public ImageModel imageModel = ImageModel.getInstance();
	public ProjectModel projectModel = ProjectModel.getInstance();
	public FieldModel fieldModel = FieldModel.getInstance();
	public UserModel userModel = UserModel.getInstance();
	
	@SuppressWarnings("serial")
	public class InvalidFieldCountOrFormatException extends Exception{ }
	
	/**
	 * Finds a sample image for a given project
	 * 
	 * @param	projectId	the id of a project
	 * @return				{@code ImageModel.Image} of the sample image
	 * @throws InvalidInputException 
	 */
	public ImageModel.Image getSampleImageForProject(int projectId) 
			throws SQLException, InvalidInputException{
		
		this.requireValidPositive(projectId);
		
		ImageModel.Image image = imageModel.findFirstBy("project_id", projectId + "");
		
		return image;
	}
	
	/**
	 * Verifies, then saves a batch assignment for a project.
	 * 
	 * @param	batchId		the id of an image
	 * @param	values		a 2d array of fields and values
	 * @return				true if successful, or throws an exception
	 * @throws InvalidInputException 
	 */
	public boolean submitBatch(int batchId,
			ArrayList<ArrayList<String>> values, UserModel.User currentUser) 
			throws SQLException, InvalidInputException{
		
		this.requireValidPositive(batchId);
		this.requireNonNull(values);
		this.requireNonNull(currentUser);
		
		ImageModel.Image image = imageModel.find(batchId);
		
		if(image != null){
			ProjectModel.Project project = projectModel.find(image.project_id);
			FieldModel.ResultList fields = fieldModel.findAllBy("project_id", project.id + "");
			if(project != null && values.size() == project.recordsperimage
					&& values.get(0) != null && fields.size() == values.get(0).size()){
				
				for(int i = 0; i < values.size(); i++){
					RecordModel.Record rec = new RecordModel.Record();
					rec.lineindex = i;
					rec.image_id = batchId;
					int id = RecordModel.getInstance().save(rec);
					for(int j = 0; j < values.get(i).size(); j++){
						ValueModel.Value val = new ValueModel.Value();
						val.columnindex = j;
						val.record_id = id;
						val.value = values.get(i).get(j);
						
						ValueModel.getInstance().save(val);
					}
					
				}
				
				imageModel.updateField(image, "user_id", null);
				userModel.updateField(currentUser, 
						"indexedrecords", 
						(currentUser.indexedrecords == null 
							? 0 
							: currentUser.indexedrecords) + values.get(0).size() + "");
				
				return true;
			}
			
			if(project.recordsperimage != values.size()) {
				throw new InvalidInputException("Did not pass enough records (" 
						+ values.size() + " of " + project.recordsperimage + ")");
			} else if(values.get(0) == null || fields.size() != values.get(0).size()) {
				throw new InvalidInputException
					("Did not pass enough fields (" + (values.get(0) != null 
						? values.get(0).size() 
						: "null") + " of " + fields.size() + ")");
			} else {
				// This should be impossible with foreign key checking in database
				throw new InvalidInputException("Invalid project from image record");
			}
		}
		
		throw new InvalidInputException("Invalid image id " + batchId);
	}
	
	public ArrayList<ArrayList<String>> formatBatch(String input) throws InvalidInputException{
		
		this.requireValidNotEmpty(input);
		
		// Split them up by ;
		ArrayList<String> items =
				new ArrayList<String>(
					Arrays.asList(
						input.split("\\s*;\\s*")));

			ArrayList<ArrayList<String>> formatted = new ArrayList<ArrayList<String>>();
			for(int i = 0; i < items.size(); i++){
				
				// The element returned is empty
				if(items.get(i).length() == 0){
					throw new InvalidInputException("Invalid input in " + input);
				}
				
				// Now split each group by ,
				ArrayList<String> entry = 
					new ArrayList<String>(
						Arrays
							.asList(items.get(i)
							.split("\\s*,\\s*")));
				formatted.add(entry);
			}
			
		return formatted;
	}
}
