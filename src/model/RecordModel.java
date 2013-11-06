package model;

import java.sql.SQLException;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import framework.Model;
import framework.ModelRecord;

/**
 * The corresponding model to Record records.
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
public class RecordModel extends Model<RecordModel.Record>{
	
	/**
	 * Abstract getName function to return the table name
	 * 
	 * @return				the table name for the model
	 */
	public String getName(){ return "records"; }
	
	// Make it a singleton
	private static RecordModel instance = null;
	protected RecordModel(){ }

	/**
	 * Create or get an instance of the Model
	 *
	 * @return	instance of the model
	 */
	public static RecordModel getInstance(){
		if(instance == null)
			instance = new RecordModel();
		
		return instance;
	}
	
	/**
	 * The RecordModel.Record class represents a Record record from the database
	 */
	public static class Record extends ModelRecord{
		public Integer image_id;
		public Integer lineindex;
		
		ArrayList<ValueModel.Value> values = new ArrayList<ValueModel.Value>();
		public Record(Element item, Integer index, ImageModel.Image parent) throws SQLException {
			
			image_id = parent.id;
			lineindex = index;
			RecordModel.getInstance().save(this);
			
			Element valuesParent = (Element) item.getElementsByTagName("values").item(0); 
			NodeList valueElements = valuesParent.getElementsByTagName("value");
			for(int i = 0; i < valueElements.getLength(); i++) {
				if(valueElements.item(i) != null)
					values.add(new ValueModel.Value((Element) valueElements.item(i), i, this));
			} 
		}
		public Record(){ }
	}

	/**
	 * Generates a new instance of Record given a map of keys and values.
	 * <p>
	 * This function avoids the problem with creating a new instance of a non-static template
	 * type in the parent Model{@literal <RecordType>} class. It might be possible to do this
	 * dynamically using the Reflection API
	 *
	 * @return	instance of Record
	 */
	@Override
	public Record generate(ResultRow genericMap) {
		Record record = new Record();
		
		//record.value = genericMap.get("records_value");
		
		return record;
	}
	
}
