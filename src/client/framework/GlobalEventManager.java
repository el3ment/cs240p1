package client.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class GlobalEventManager {

	// Event Manager is a singleton to allow us to ensure all instances
	// of any controller will respond to an event
	private static GlobalEventManager _instance = null;
	ArrayList<Object> _eventListeners = new ArrayList<Object>();
	
	protected GlobalEventManager(){ }

	public static GlobalEventManager getInstance(){
		if(_instance == null)
			_instance = new GlobalEventManager();
		
		return _instance;
	}
	
	public void addListener(Object listener){
		if(!_eventListeners.contains(listener))
			_eventListeners.add(listener);
	}
	
	// Helper function for cleanliness
	public void fireEvent(Object source, String eventName){
		this.fireEvent(source, eventName, null);
	}
	
	public void fireEvent(Object source, String eventName, Object params){

		// Reformat the eventName to match convention
		eventName = "on" + StringUtils.capitalize(eventName);
		ArrayList<Object> listeners = (ArrayList<Object>) _eventListeners.clone();
		for(Object handler : listeners){
			
			try {
				@SuppressWarnings("rawtypes")
				Class[] signature;
				
				if(params != null){
					signature = new Class[2];
					signature[0] = source.getClass();
					signature[1] = params.getClass();
				}else{
					signature = new Class[1];
					signature[0] = source.getClass();
				}
				
				// Grab the function
				Method eventFunction = handler.getClass().getMethod(eventName, signature);
				
				// If params are not null, just call onEventName(controller, params)
				// otherwise drop the params
				if(params != null)
						eventFunction.invoke(handler, source, params);
				else
					eventFunction.invoke(handler, source);
				
			}catch (NoSuchMethodException e) {
//				if(params != null)
//					System.err.println(e.getCause() + " : " + handler.getClass().getName() + " does not have a method with proper signature for " + eventName + "(" + source.getClass().getName() + ", " + params.getClass().getName() + ")");
//				else
//					System.err.println(e.getCause() + " : " + handler.getClass().getName() + " does not have a method with proper signature for " + eventName + "(" + source.getClass().getName() + ")");
//				
				continue;
			}catch(Exception e){
				System.err.println(e + " " + e.getCause());
				e.printStackTrace();
			}
		}
	}
	
}
