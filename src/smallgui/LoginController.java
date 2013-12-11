package smallgui;

import javax.swing.JOptionPane;

import shared.Request;

public class LoginController extends Controller{
	
	public void login(String username, String password){
		Request.ValidateUserRequest request = new Request.ValidateUserRequest();
		
		request.username = username;
		request.password = password;
		
		this.submitRequest("validateUser", request);
	}
	
	// Fired by _controller after a server response failure
	public void onValidateUserFailure(LoginController controller){
		JOptionPane.showMessageDialog(null, "There was an issue logging you in, please check the port, host, username and password");
	}
	
}
