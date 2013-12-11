package smallgui;

import java.lang.reflect.Method;
import java.util.ArrayList;

import shared.Request;

import org.apache.commons.lang3.StringUtils;

public class Controller {
	
	View _view;
	ArrayList<Object> _eventListeners = new ArrayList<Object>();
	
	public void addListener(Object listener){
		_eventListeners.add(listener);
	}
	
	public void fireEvent(String eventName, Object params){

		// Reformat the eventName to match convention
		eventName = "on" + StringUtils.capitalize(eventName);
		
		for(Object handler : _eventListeners){
			
			//System.out.println((_eventListeners.indexOf(handler) + 1) + " / " + _eventListeners.size() + " " + eventName + " in " + handler.getClass().getCanonicalName());
			
			try {
				@SuppressWarnings("rawtypes")
				Class[] signature;
				
				if(params != null){
					signature = new Class[2];
					signature[0] = this.getClass();
					signature[1] = params.getClass();
				}else{
					signature = new Class[1];
					signature[0] = this.getClass();
				}
				
				// Grab the function
				Method eventFunction = handler.getClass().getMethod(eventName, signature);
				
				// If params are not null, just call onEventName(controller, params)
				// otherwise drop the params
				if(params != null)
					eventFunction.invoke(handler, this, params);
				else{
					eventFunction.invoke(handler, this);
				}
				
			} catch (Exception e) {
				if(params != null)
					System.err.println(handler.getClass().getName() + " does not have a method with proper signature for " + eventName + "(" + this.getClass().getName() + ", " + params.getClass().getName() + ")");
				else
					System.err.println(handler.getClass().getName() + " does not have a method with proper signature for " + eventName + "(" + this.getClass().getName() + ")");
				continue;
			} 
		}
	}
	
	public void setView(View view){
		_view = view;
	}
	
	public void submitRequest(String method, Object request){
		String hostname = (String) AppState.get().get("hostname");
		String port = (String) AppState.get().get("port");
		
		Object response = Request.submitRequest(hostname, port, method, request);
		
		if(response != null && !response.getClass().getName().equals("java.lang.Boolean"))
			this.fireEvent(method + "Success", response);
		else
			this.fireEvent(method + "Failure", null);
	}
}
