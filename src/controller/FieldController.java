package controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import server.API.NoResultsFoundException;
import shared.Request;
import framework.Controller;
import framework.Database;
import model.FieldModel;

/**
 * The controller for Field logic.
 */
public class FieldController extends Controller{
	
	public FieldModel fieldModel = FieldModel.getInstance();
	
	/**
	 * Finds all fields for a given project
	 * 
	 * @param	projectId	the id of a project
	 * @return				{@code FieldModel.ResultList} which is essentially an ArrayList of 
	 * 						FieldModel.Fields
	 * @throws InvalidInputException 
	 */
	public FieldModel.ResultList findAllByProjectId(int projectId) 
			throws SQLException, InvalidInputException{
		this.requireValidPositive(projectId);
		
		return fieldModel.findAllBy("fields_project_id", projectId+"");
	}
	
	public ArrayList<Request.SearchResponse.SearchResult> 
			search(String fields, String values) 
			throws InvalidInputException{
		
		this.requireValidNotEmpty(fields);
		this.requireValidNotEmpty(values);
		this.requireEquals(fields.split(", *").length % 2, 1); // valid comma structure
		this.requireEquals(values.split(", *").length % 2, 1); // valid comma structure
		this.requireValidNumeric(fields.replaceAll(", *", "")); // fields is numeric
		
		ArrayList<Request.SearchResponse.SearchResult> results =
				new ArrayList<Request.SearchResponse.SearchResult>();
		
		String fieldClause = "fields.title = \"" + 
				fields.replaceAll(", *", "\" OR fields.id = \"")
				+ "\"";
		String valueClause = "UPPER('values'.value) = \"" + 
				values.toUpperCase().replaceAll(", *", "\" OR UPPER('values'.value) = \"")
				+ "\"";

		Database db = Database.getInstance();
		
		try{
			ResultSet rs = db.execute(
				"SELECT "
					+ "'values'.columnindex, "
					+ "fields.columnindex, "
					+ "'values'.value as value, "
					+ "images.file as image_url, "
					+ "images.id as batch_id, "
					+ "records.lineindex as record_num, "
					+ "fields.id as field_id "
				+ "FROM fields JOIN projects ON fields.project_id = projects.id "
					+ "JOIN images ON images.project_id = projects.id "
					+ "JOIN records ON records.image_id = images.id JOIN "
					+ "'values' ON records.id = 'values'.record_id "
				+ "WHERE fields.columnindex = 'values'.columnindex "
					+ "AND (" + fieldClause + ") AND (" + valueClause + ")");
		
			if(rs.isAfterLast())
				throw new NoResultsFoundException();
			
			while(rs.next()){
				Request.SearchResponse.SearchResult result = 
						new Request.SearchResponse.SearchResult();
				
				result.batchId = rs.getString("batch_id");
				result.imageUrl = rs.getString("image_url");
				result.recordNum = rs.getString("record_num");
				result.fieldId = rs.getString("field_id");
				
				results.add(result);
			}
			
			return results;
			
		}catch(Exception e){
			return null;
		}
	}
}
