package client.framework;

public class EventController {
	
	GlobalEventManager eventManager = GlobalEventManager.getInstance();
	
	public void addListener(Object listener){
		
		eventManager.addListener(listener);
	}
	
	// Helper function for cleanliness
	public void fireEvent(String eventName){
		eventManager.fireEvent(this, eventName);
	}
	
	public void fireEvent(String eventName, Object params){
		eventManager.fireEvent(this, eventName, params);
	}
	
}
