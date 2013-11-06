package model;

import java.sql.SQLException;

import model.RecordModel.Record;

import org.w3c.dom.Element;

import server.DataImporter;
import framework.Model;
import framework.ModelRecord;


public class ValueModel extends Model<ValueModel.Value>{
	
	/**
	 * Abstract getName function to return the table name
	 * 
	 * @return				the table name for the model
	 */
	public String getName(){ return "values"; }
	
	// Make it a singleton
	private static ValueModel instance = null;
	protected ValueModel(){ }

	/**
	 * Create or get an instance of the Model
	 *
	 * @return	instance of the model
	 */
	public static ValueModel getInstance(){
		if(instance == null)
			instance = new ValueModel();
		
		return instance;
	}
	/**
	 * The RecordModel.Value class represents a value record from the database
	 */
	public static class Value extends ModelRecord{
		public String value;
		public Integer columnindex;
		public Integer record_id;
		
		public Value(Element item, int index, Record parent) throws SQLException {
			columnindex = index;
			value = DataImporter.getValue((Element) item);
			record_id = parent.id;
			
			ValueModel.getInstance().save(this);
		}
		public Value(){ }
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
	public Value generate(ResultRow genericMap) {
		Value record = new Value();
		
		record.value = genericMap.get("values_value");
		
		return record;
	}
}
