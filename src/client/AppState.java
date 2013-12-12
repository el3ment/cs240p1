package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import client.framework.GlobalEventManager;

// App State is a global repository of variables
// any object can request a dynamic global variable by calling
// AppState.get().get("variableName");
public class AppState {
	
	// AppState is a singleton
	private static AppState _instance;
	private HashMap<String, Object> _map = new HashMap<String, Object>();
	
	// As a singleton, don't allow anyone to instantiate it
	protected AppState(){}
	
	// Get an instance of the singleton
	public static AppState get(){
		if(_instance == null)
			_instance = new AppState();
		
		return _instance;
	}
	
	// Save a variable and call the global event for changing
	public void put(String key, Object value){
		Object previousValue = _map.get(key);
		_map.put(key, value);
		if(value == null || !value.equals(previousValue))
			GlobalEventManager.getInstance().fireEvent(this, key + "Changed", value);
	}
	
	// Get a variable
	public Object get(String key){
		return _map.get(key);
	}
	
	// A helper function
	private String _getSettingsFileName(){
		return _map.get("username") + "-settings.xml";
	}
	
	// Save the variables to disk
	public void save(){
		
		// save _map to file
		XStream xmlStream = new XStream(new DomDriver());
		try {
			xmlStream.toXML(_map, new FileOutputStream(this._getSettingsFileName()));
		} catch (FileNotFoundException e) {
			GUI.globalErrorMessage("There was an error saving your settings, sorry about that");
		}
	}
	
	// Empty the entire variable mapping
	public void clear(){
		_map.clear();
	}
	
	// Load saved settings from disk
	@SuppressWarnings("unchecked")
	public void load(){
		
		File settingsFile = new File(this._getSettingsFileName());
		
		if(settingsFile.isFile()){
			XStream xmlStream = new XStream(new DomDriver());
			_map = (HashMap<String, Object>) xmlStream.fromXML(settingsFile);
			
			// Fire load event for each variable
			for(String key : _map.keySet()){
				GlobalEventManager.getInstance().fireEvent(this, key + "Changed", _map.get(key));
			}
		}
	}
	
}
