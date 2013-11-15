package model;

import java.sql.SQLException;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import server.DataImporter;
import framework.Model;
import framework.ModelRecord;

/**
 * The corresponding model to Image records.
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
public class ImageModel extends Model<ImageModel.Image>{
	
	/**
	 * Abstract getName function to return the table name
	 * 
	 * @return				the table name for the model
	 */
	public String getName(){ return "images"; }
	
	// Make it a singleton
	private static ImageModel instance = null;
	protected ImageModel(){ }

	/**
	 * Create or get an instance of the Model
	 *
	 * @return	instance of the model
	 */
	public static ImageModel getInstance(){
		if(instance == null)
			instance = new ImageModel();
		
		return instance;
	}
	
	/**
	 * The ImageModel.Image class represents an Image record from the database
	 */
	public static class Image extends ModelRecord{

		public String file;
		public Integer user_id = -1;
		public Boolean processed = false;
		
		// Parents
		public Integer project_id;
		
		// Children
		ArrayList<RecordModel.Record> records = new ArrayList<RecordModel.Record>();
		
		
		public Image(Element item, ProjectModel.Project parent) throws SQLException {
			file = DataImporter.getValue((Element) item.getElementsByTagName("file").item(0)); 
			project_id = parent.id;
			
			ImageModel.getInstance().save(this);
			
			Element recordList = (Element) item.getElementsByTagName("records").item(0); 
			
			// Records are optional
			if(recordList == null) return;
			NodeList recordElements = recordList.getElementsByTagName("record"); 
			for(int i = 0; i < recordElements.getLength(); i++) { 
				this.records.add(new RecordModel.Record(
					(Element) recordElements.item(i), i, this)); 
			} 
		}
		
		public Image(){ }
		
	}

	/**
	 * Generates a new instance of Image given a map of keys and values.
	 * <p>
	 * This function avoids the problem with creating a new instance of a non-static template
	 * type in the parent Model{@literal <RecordType>} class. It might be possible to do this
	 * dynamically using the Reflection API
	 *
	 * @return	instance of Image
	 */
	@Override
	public Image generate(ResultRow genericMap) {
		Image image = new Image();
		image.id = Integer.parseInt(genericMap.get("images_id"));
		image.file = genericMap.get("images_file");
		image.project_id = Integer.parseInt(genericMap.get("images_project_id"));
		image.processed = Boolean.parseBoolean(genericMap.get("images_processed"));
		
		try{
			image.user_id = Integer.parseInt(genericMap.get("images_user_id"));
		}catch(Exception e){
			image.user_id = null;
		}
		
		return image;
	}
}