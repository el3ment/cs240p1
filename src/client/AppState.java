package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import client.framework.GlobalEventManager;

public class AppState {
	
	private static AppState _instance;
	private HashMap<String, Object> _map = new HashMap<String, Object>();
	
	protected AppState(){}
	
	public static AppState get(){
		if(_instance == null)
			_instance = new AppState();
		
		return _instance;
	}
	
	public void put(String key, Object value){
		Object previousValue = _map.get(key);
		_map.put(key, value);
		if(value == null || !value.equals(previousValue))
			GlobalEventManager.getInstance().fireEvent(this, key + "Changed", value);
	}
	
	public Object get(String key){
		return _map.get(key);
	}
	
	private String _getSettingsFileName(){
		return _map.get("username") + "-settings.xml";
	}
	
	public void save(){
		
		// save _map to file
		XStream xmlStream = new XStream(new DomDriver());
		try {
			xmlStream.toXML(_map, new FileOutputStream(this._getSettingsFileName()));
		} catch (FileNotFoundException e) {
			GUI.globalErrorMessage("There was an error saving your settings, sorry about that");
		}
	}
	
	public void clear(){
		_map.clear();
	}
	
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
