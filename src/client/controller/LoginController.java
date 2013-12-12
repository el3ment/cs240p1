package client.controller;

import client.framework.Controller;
import shared.Request;

public class LoginController extends Controller{
	
	// Logins a user
	public Request.ValidateUserResponse login(String username, String password){
		Request.ValidateUserRequest request = new Request.ValidateUserRequest();
		
		request.username = username;
		request.password = password;
		
		return (Request.ValidateUserResponse) this.submitRequest("validateUser", request);
	}
	
	// Logs out a user - no data is sent to the server
	public void logout(){
		this.fireEvent("logout");
	}
}
