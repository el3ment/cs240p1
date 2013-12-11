package smallgui;

import shared.Request;

public class SearchGUI{

	// TODO : make event class for each controller
	public static LoginView loginFrame;
	
	public static void main(String[] args) {
		loginFrame = new LoginView(new LoginHandler());
		loginFrame.create();
	}
	
	public static class LoginHandler{
		//public void onLoginSuccess(LoginController controller, Request.ValidateUserResponse response){
		public void onValidateUserSuccess(LoginController controller, Request.ValidateUserResponse response){
			SearchView searchFrame = new SearchView();
			searchFrame.create();
			loginFrame.hide();
		}
	}
	
	
}
