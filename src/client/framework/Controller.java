package client.framework;

import org.apache.commons.lang3.StringUtils;

import shared.Request;
import client.AppState;

// The general Controller class has a few functions all controllers need
public class Controller extends EventController{
	
	// A helper function to facilitate making the request and handling the dependent events
	// fires onMethodSuccess or onMethodFailure events
	public Object submitRequest(String method, Object request){
		
		String hostname = (String) AppState.get().get("hostname");
		String port = (String) AppState.get().get("port");
		
		Object response = Request.submitRequest(hostname, port, method, request);
		
		// After the response is called - call the Success and afterSuccess versions of the
		// event
		if(response != null && !response.getClass().getName().equals("java.lang.Boolean")){
			this.fireEvent(method + "Success", response);
			this.fireEvent("after"+ StringUtils.capitalize(method) + "Success", response);
		}else{
			this.fireEvent(method + "Failure");
			this.fireEvent("after"+ StringUtils.capitalize(method) + "Failure");
		}
		
		return response;
	}
	
	
}
