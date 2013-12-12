package client.framework;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

// The global event manager - takes event names and stores listeners
// it uses reflection to dynamically call the proper method on each listener
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
				
				// If params was passed - find a method that can accept params
				if(params != null){
					signature = new Class[2];
					signature[0] = source.getClass();
					signature[1] = params.getClass();
					
				// If null was passed (generally a failure - but not always) find the method without a second argument
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
				
				// Not every listener has every method - so we get lots of these errors
				// but none of them are important after you have identified the proper signiture
				// I didn't realize when I started how this was going to cause some confusion - but the entire
				// app is built on this event system and it works great, if a little difficult to debug.
				
				// if(params != null)
				//	System.err.println(e.getCause() + " : " + handler.getClass().getName() + " does not have a method with proper signature for " + eventName + "(" + source.getClass().getName() + ", " + params.getClass().getName() + ")");
				// else
				//	System.err.println(e.getCause() + " : " + handler.getClass().getName() + " does not have a method with proper signature for " + eventName + "(" + source.getClass().getName() + ")");
				
				continue;
			}catch(Exception e){
				// Other exceptions caused by the methods invoked -- null pointer errors, etc
				System.err.println(e + " " + e.getCause());
				e.printStackTrace();
			}
		}
	}
	
}
