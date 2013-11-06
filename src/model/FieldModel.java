package model;

import java.sql.SQLException;
import org.w3c.dom.Element;
import server.DataImporter;
import framework.Model;
import framework.ModelRecord;

/**
 * The corresponding model to Field records.
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
public class FieldModel extends Model<FieldModel.Field>{
	
	/**
	 * Abstract getName function to return the table name
	 * 
	 * @return				the table name for the model
	 */
	public String getName(){ return "fields"; }
	
	// Make it a singleton
	private static FieldModel instance = null;
	protected FieldModel(){ }

	/**
	 * Create or get an instance of the Model
	 *
	 * @return	instance of the model
	 */
	public static FieldModel getInstance(){
		if(instance == null)
			instance = new FieldModel();
		
		return instance;
	}
	
	/**
	 * The FieldModel.Field class represents a Field record from the database
	 */
	public static class Field extends ModelRecord{
		public int project_id;
		
		public String title;
		public Integer xcoord;
		public Integer width;
		public String helphtml;
		public String knowndata;
		public Integer columnindex;

		public Field(Element item, int index, ProjectModel.Project parent) throws SQLException {
			// TODO Auto-generated constructor stub
			helphtml = DataImporter.getValue(
				(Element) item.getElementsByTagName("helphtml").item(0)); 
			knowndata = DataImporter.getValue(
				(Element) item.getElementsByTagName("knowndata").item(0)); 
			title = DataImporter.getValue(
				(Element) item.getElementsByTagName("title").item(0)); 
			xcoord = Integer.parseInt(DataImporter.getValue(
				(Element) item.getElementsByTagName("xcoord").item(0))); 
			width = Integer.parseInt(DataImporter.getValue(
				(Element) item.getElementsByTagName("width").item(0))); 
			columnindex = index;
			project_id = parent.id;
			
			FieldModel.getInstance().save(this);
		}
		
		public Field(){ }
	}
	
	/**
	 * Generates a new instance of Record given a map of keys and values.
	 * <p>
	 * This function avoids the problem with creating a new instance of a non-static template
	 * type in the parent Model{@literal <RecordType>} class. It might be possible to do this
	 * dynamically using the Reflection API
	 *
	 * @return	instance of Field
	 */
	@Override
	public Field generate(ResultRow genericMap) {
		Field field = new Field();
		field.project_id = Integer.parseInt(genericMap.get("fields_project_id"));
		field.id = Integer.parseInt(genericMap.get("fields_id"));
		field.title = genericMap.get("fields_title");
		field.xcoord = Integer.parseInt(genericMap.get("fields_xcoord"));
		field.width = Integer.parseInt(genericMap.get("fields_width"));
		field.helphtml = genericMap.get("fields_helphtml");
		field.knowndata = genericMap.get("fields_knowndata");
		
		return field;
	}

}
