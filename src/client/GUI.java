package client;

import javax.swing.JOptionPane;

import client.components.AppWindow;
import client.components.LoginWindow;
import client.controller.LoginController;
import client.framework.GlobalEventManager;
import shared.Request;

public class GUI {
	
	public static LoginWindow _loginWindow;
	public static AppWindow _appWindow;
	public static String host;
	public static String port;
	
	public static void prepAppState(){
		AppState.get().clear();
		AppState.get().put("hostname", host);
		AppState.get().put("port", port);
	}
	
	public static void main(String[] args) {
		
		host = args[0];
		port = args[1];
		
		prepAppState();
		
		_loginWindow = new LoginWindow();
		_loginWindow.create();
		_loginWindow.show();

		GlobalEventManager.getInstance().addListener(new GlobalEventHandlers());
		
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
		    @Override
		    public void run()
		    {
		        AppState.get().save();
		    }
		});
		
	}
	
	public static void exit(){
		AppState.get().save();
		
		_loginWindow.close();
		
		if(_appWindow != null)
			_appWindow.close();
		
		System.exit(0);
	}
	
	public static void logout(){
		
		AppState.get().save();
		
		if(_appWindow != null)
			_appWindow.close();
		
		_loginWindow.clear();
		_loginWindow.show();
		prepAppState();
	}
	
	public static void globalErrorMessage(String message){
		JOptionPane.showMessageDialog(null,
			    message,
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
	}
	
	public static class GlobalEventHandlers{
		
		public void onValidateUserSuccess(LoginController controller, Request.ValidateUserResponse response){
			
			JOptionPane.showMessageDialog(null,
				    "Hello " + response.firstName + " " + response.lastName 
				    + ", you have completed " + response.numRecords + " batches",
				    "Success",
				    JOptionPane.PLAIN_MESSAGE);
			
			
			_loginWindow.hide();
			
			_appWindow = new AppWindow();
			_appWindow.create();
			AppState.get().load();
			_appWindow.show();
		}
		
		public void onValidateUserFailure(LoginController controller){
			GUI.globalErrorMessage("There was an error logging in");
		}
		
	}
}
