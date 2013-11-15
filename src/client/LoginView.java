package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import static client.Constants.*;

public class LoginView extends View{
	
	private LoginController _controller = new LoginController();
	private JFrame _frame;
	private LoginPanel _loginPanel;
	
	public LoginView(Object listener){
		_controller.addListener(listener);
		init();
	}
	
	public LoginView(){
		init();
	}
	
	public void init(){
		_controller.addListener(_controller);
		_controller.setView(this);
	}
	
	public void create(){
		_frame = new JFrame();
		_frame.setTitle("Login");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setLayout(new BoxLayout(_frame.getContentPane(), BoxLayout.PAGE_AXIS));		
		
		_loginPanel = new LoginPanel();
		
		_frame.add(_loginPanel);
		
		_frame.setMinimumSize(_frame.getPreferredSize());
		_frame.pack();
		_frame.setLocationRelativeTo(null);
		_frame.setVisible(true);
	}
	
	public void hide(){
		_frame.setVisible(false);
	}
	
	public void show(){
		_frame.setVisible(true);
	}
	
	@SuppressWarnings("serial")
	public class LoginPanel extends JPanel{
		JTextField _hostField;
		JTextField _portField;
		JTextField _usernameField;
		JTextField _passwordField;
		JButton _loginButton;
		
		public LoginPanel() {
			super();
			
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			
			add(Box.createRigidArea(DOUBLE_HSPACE));
			add(new JLabel("HOST:"));
			
			add(Box.createRigidArea(SINGLE_HSPACE));
			
			_hostField = new JTextField(30);
			_hostField.setText("localhost");
			_hostField.setMinimumSize(_hostField.getPreferredSize());
			add(_hostField);
			
			add(Box.createRigidArea(TRIPLE_HSPACE));
			
			add(new JLabel("PORT:"));
			add(Box.createRigidArea(SINGLE_HSPACE));
			
			_portField = new JTextField(10);
			_portField.setText("39640");
			_portField.setMinimumSize(_portField.getPreferredSize());
			add(_portField);
			
			add(Box.createRigidArea(TRIPLE_HSPACE));
			
			add(new JLabel("Username:"));
			add(Box.createRigidArea(SINGLE_HSPACE));
			
			_usernameField = new JTextField(10);
			_usernameField.setText("sheila");
			_usernameField.setMinimumSize(_usernameField.getPreferredSize());
			add(_usernameField);
			
			add(Box.createRigidArea(TRIPLE_HSPACE));
			add(new JLabel("Password:"));
			
			add(Box.createRigidArea(SINGLE_HSPACE));
			
			_passwordField = new JTextField(10);
			_passwordField.setText("parker");
			_passwordField.setMinimumSize(_passwordField.getPreferredSize());
			add(_passwordField);
			
			add(Box.createRigidArea(TRIPLE_HSPACE));
			
			_loginButton = new JButton("Login");
			add(_loginButton);
			add(Box.createRigidArea(DOUBLE_HSPACE));

			setMaximumSize(getPreferredSize());
			
			_loginButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					AppState.get().put("hostname", _hostField.getText());
					AppState.get().put("port", _portField.getText());
					AppState.get().put("username", _usernameField.getText());
					AppState.get().put("password", _passwordField.getText());
					
					_controller.login(
						_usernameField.getText(),
						_passwordField.getText());
				}
			});
		}
	}
}
