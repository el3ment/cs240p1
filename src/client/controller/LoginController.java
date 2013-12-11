package client.controller;

import client.framework.Controller;
import shared.Request;

public class LoginController extends Controller{
	
	public Request.ValidateUserResponse login(String username, String password){
		Request.ValidateUserRequest request = new Request.ValidateUserRequest();
		
		request.username = username;
		request.password = password;
		
		return (Request.ValidateUserResponse) this.submitRequest("validateUser", request);
	}
	
	public void logout(){
		this.fireEvent("logout");
	}
}
