package client;

import shared.Request;
import shared.Request.ValidateUserResponse;

public class SearchGUI{

	// TODO : make event class for each controller
	
	public static void main(String[] args) {
		LoginView loginFrame = new LoginView(new LoginHandler());
		loginFrame.create();
	}
	
	public static class LoginHandler{
		//public void onLoginSuccess(LoginController controller, Request.ValidateUserResponse response){
		public void onValidateUserFailure(LoginController controller){
			Request.ValidateUserResponse response = new Request.ValidateUserResponse();
			SearchView searchFrame = new SearchView();
			searchFrame.create();
			// create and show list panel
			// ListPanel listPanel = new ListPanel();
		}
	}
	
	
}
