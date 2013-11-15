package client;

import java.util.HashMap;

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
		_map.put(key, value);
	}
	
	public Object get(String key){
		return _map.get(key);
	}
	
}
