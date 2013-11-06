package model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import server.DataImporter;
import framework.Model;
import framework.ModelRecord;

/**
 * The corresponding model to Project records.
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
public class ProjectModel extends Model<ProjectModel.Project>{
	
	/**
	 * Abstract getName function to return the table name
	 * 
	 * @return				the table name for the model
	 */
	public String getName(){ return "projects"; }
	
	// Make it a singleton
	private static ProjectModel instance = null;
	protected ProjectModel(){ }

	/**
	 * Create or get an instance of the Model
	 *
	 * @return	instance of the model
	 */
	public static ProjectModel getInstance(){
		if(instance == null)
			instance = new ProjectModel();
		
		return instance;
	}
	
	/**
	 * The ProjectModel.Project class represents a Project record from the database
	 */
	public static class Project extends ModelRecord{
		
		public String title;
		public Integer recordsperimage;
		public Integer firstycoord;
		public Integer recordheight;
		
		// Children
		ArrayList<FieldModel.Field> fields = new ArrayList<FieldModel.Field>(); 
		ArrayList<ImageModel.Image> images = new ArrayList<ImageModel.Image>();
		
		public Project(Element projectElement) throws SQLException {
			title = DataImporter.getValue(
				(Element) projectElement.getElementsByTagName("title").item(0)); 
			recordsperimage = Integer.parseInt(DataImporter.getValue(
				(Element) projectElement.getElementsByTagName("recordsperimage").item(0))); 
			firstycoord = Integer.parseInt(DataImporter.getValue(
				(Element) projectElement.getElementsByTagName("firstycoord").item(0))); 
			recordheight = Integer.parseInt(DataImporter.getValue(
				(Element) projectElement.getElementsByTagName("recordheight").item(0))); 

			// Save the project to populate id, which is needed to save fields and images
			ProjectModel.getInstance().save(this);
			
			// Parse and save fields
			Element fieldsElement = (Element) projectElement
					.getElementsByTagName("fields")
					.item(0); 
			NodeList fieldElements = fieldsElement.getElementsByTagName("field");
			
			// Sort by xcoord low to high
			ArrayList<Element> elements = ProjectModel.sortByXCoord(fieldElements);
			
			// Create the fields and save them
			for(int i = 0; i < elements.size(); i++) {
				fields.add(new FieldModel.Field(elements.get(i), i, this));
			} 
			
			// Parse and save images
			Element imagesElement = (Element) projectElement
					.getElementsByTagName("images")
					.item(0); 
			NodeList imageElements = imagesElement.getElementsByTagName("image"); 
			for(int i = 0; i < imageElements.getLength(); i++) { 
				images.add(new ImageModel.Image((Element) imageElements.item(i), this));
			}
		}

		public Project() { }
	}

	
	ImageModel imageModel = new ImageModel();

	/**
	 * Generates a new instance of Project given a map of keys and values.
	 * <p>
	 * This function avoids the problem with creating a new instance of a non-static template
	 * type in the parent Model{@literal <RecordType>} class. It might be possible to do this
	 * dynamically using the Reflection API
	 *
	 * @return	instance of Project
	 */
	@Override
	public Project generate(ResultRow genericMap) {
		Project project = new Project();
		project.id = Integer.parseInt(genericMap.get("projects_id"));
		project.title = genericMap.get("projects_title");
		project.recordsperimage = Integer.parseInt(genericMap.get("projects_recordsperimage"));
		project.firstycoord = Integer.parseInt(genericMap.get("projects_firstycoord"));
		project.recordheight = Integer.parseInt(genericMap.get("projects_recordheight"));
		
		return project;
	}
	
	
	// If the order returned by fields is not in the right order, we need to format them here
	// the first argument is the list of nodes, the second is the name of the field to sort by
	private static ArrayList<Element> sortByXCoord(NodeList fieldElements) {
		
		ArrayList<Element> response = new ArrayList<Element>();
		
		for(int i = 0; i < fieldElements.getLength(); i++){
			response.add((Element) fieldElements.item(i));
		}
		
		Collections.sort(response, new Comparator<Element>(){
		     public int compare(Element o1, Element o2){
		    	 
		    	 Element e1 = (Element) o1;
		    	 Element e2 = (Element) o2;
		    	 int xcoord1 = Integer.parseInt(DataImporter.getFieldValue(e1, "xcoord"));
		    	 int xcoord2 = Integer.parseInt(DataImporter.getFieldValue(e2, "xcoord"));
		    	 
		         if(xcoord1 == xcoord2)
		             return 0;
		         return xcoord1 < xcoord2 ? -1 : 1;
		     }
		});
		
		return response;
	} 

}
