package client.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import client.AppState;
import client.GUI;
import client.controller.LoginController;

public class LoginWindow {
	
	JFrame _frame; // Instance of the frame
	FormPanel _formPanel; // Instance of the panel (class found below)
	LoginController _controller = new LoginController();
	
	// Builds window, populates with the LoginPanel (defined below)
	public void create(){
		_frame = new JFrame();
		_frame.setTitle("Login");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.getContentPane().setLayout(new BorderLayout());
		
		// Create the content panel and add it
		_formPanel = new FormPanel();
		_frame.getContentPane().add(_formPanel);
		
		_frame.setMinimumSize(_frame.getPreferredSize());
		_frame.setMaximumSize(_frame.getPreferredSize());
		_frame.setResizable(false);
		_frame.pack();
		_frame.setLocationRelativeTo(null);

	}
	
	public void hide(){
		_frame.setVisible(false);
	}
	
	public void show(){
		_frame.setVisible(true);
	}
	
	// Reset the panel
	public void clear(){
		_formPanel._usernameField.setText("");
		_formPanel._passwordField.setText("");
		_formPanel._usernameField.requestFocus();
	}
	
	public void close(){
		_frame.dispose();
	}
	
	// The content panel, houses the fields and buttons, etc for the window
	@SuppressWarnings("serial")
	public class FormPanel extends JPanel{
		
		JTextField _hostField;
		JTextField _portField;
		JTextField _usernameField;
		JTextField _passwordField;
		JButton _loginButton;
		
		public FormPanel() {
			super();
			
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			// Create GridBag variables
			GridBagLayout gbl_contentPanel = new GridBagLayout();
			gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
			gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
			gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			this.setLayout(gbl_contentPanel);
			
			// Username label
			JLabel lblUsername = new JLabel("Username");
			GridBagConstraints gbc_lblUsername = new GridBagConstraints();
			gbc_lblUsername.anchor = GridBagConstraints.EAST;
			gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
			gbc_lblUsername.gridx = 0;
			gbc_lblUsername.gridy = 0;
			this.add(lblUsername, gbc_lblUsername);

			// Username field
			_usernameField = new JTextField();
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.insets = new Insets(0, 0, 5, 0);
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.gridx = 1;
			gbc_textField.gridy = 0;
			this.add(_usernameField, gbc_textField);
			_usernameField.setColumns(20);

			// Password label
			JLabel lblPassword = new JLabel("Password");
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.anchor = GridBagConstraints.EAST;
			gbc_lblPassword.insets = new Insets(0, 0, 0, 5);
			gbc_lblPassword.gridx = 0;
			gbc_lblPassword.gridy = 1;
			this.add(lblPassword, gbc_lblPassword);

			// Password field
			_passwordField = new JPasswordField();
			GridBagConstraints gbc_passwordField = new GridBagConstraints();
			gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
			gbc_passwordField.gridx = 1;
			gbc_passwordField.gridy = 1;
			this.add(_passwordField, gbc_passwordField);
			
			// Buttons
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			GridBagConstraints gbc_buttonPane = new GridBagConstraints();
			gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
			gbc_buttonPane.gridx = 0;
			gbc_buttonPane.gridy = 3;
			gbc_buttonPane.gridwidth = 2;
			this.add(buttonPane, gbc_buttonPane);
			
			// Exit
			JButton exitButton = new JButton("Exit");
			exitButton.setActionCommand("Exit");
			buttonPane.add(exitButton);
			
			// Login
			JButton okButton = new JButton("Login");
			okButton.setActionCommand("Login");
			buttonPane.add(okButton);
			_frame.getRootPane().setDefaultButton(okButton);
			
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					// Store the username and password for subsequent server calls
					AppState.get().put("username", _usernameField.getText());
					AppState.get().put("password", _passwordField.getText());
					
					// Login!
					_controller.login(
						_usernameField.getText(),
						_passwordField.getText());
				}
			});
			
			// If exit button is pressed
			exitButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					GUI.exit();
				}
			});
			
		}
	}
	
	
}

