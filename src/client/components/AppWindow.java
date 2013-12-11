package client.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import client.AppState;
import client.GUI;
import client.controller.BatchController;
import client.framework.ActiveBatch;
import client.framework.GlobalEventManager;
import shared.Request;
import shared.Request.*;

public class AppWindow implements PropertyChangeListener, ComponentListener{
	JFrame _frame;
	TableInputPanel _tableView;
	FormInputPanel _formView;
	FieldHelpPanel _fieldHelp;
	ImageNavigatorPanel _imageNavigator;
	ImageWindowPanel _imageWindow;
	JMenuBar _menu;
	JMenuItem _downloadBatchMenuItem;
	JSplitPane _verticalSplit;
	JSplitPane _horizontalSplit;
	
	private JMenuBar _buildMenu(){
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		_downloadBatchMenuItem = new JMenuItem("Download Batch");
		mnNewMenu.add(_downloadBatchMenuItem);
		_downloadBatchMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// When the Download Batch menu option is selected
				// create a DownloadBatchWindow dialog and show it
				DownloadBatchWindow downloadBatchDialog = new DownloadBatchWindow();
				downloadBatchDialog.create();
				downloadBatchDialog.show();
			}
		});
		
		JMenuItem mntmLogout = new JMenuItem("Logout");
		mnNewMenu.add(mntmLogout);
		mntmLogout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GUI.logout();
			}
		});
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnNewMenu.add(mntmExit);
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GUI.exit();
			}
		});
		
		return menuBar;
	}
	
	public void create(){
		_frame = new JFrame();
		_frame.setName("window");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		_menu = this._buildMenu();
		_frame.setJMenuBar(_menu);
		
		_verticalSplit = new JSplitPane();
		_verticalSplit.setName("vertical");
		_verticalSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		_frame.getContentPane().add(_verticalSplit, BorderLayout.CENTER);
		
		_horizontalSplit = new JSplitPane();
		_horizontalSplit.setName("horizontal");
		_verticalSplit.setRightComponent(_horizontalSplit);
		
		//Set percentage weights for split panes
		_verticalSplit.setResizeWeight(.6d);
		_horizontalSplit.setResizeWeight(.5d);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		_horizontalSplit.setLeftComponent(tabbedPane);
		
		_tableView = new TableInputPanel();
		tabbedPane.addTab("Table View", null, _tableView, null);
		
		_formView = new FormInputPanel();
		tabbedPane.addTab("Form View", null, _formView, null);
		
		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		_horizontalSplit.setRightComponent(tabbedPane_1);
		
		// Field Help
		_fieldHelp = new FieldHelpPanel();
		tabbedPane_1.addTab("Field Help", null, new JScrollPane(_fieldHelp), null);
		
		_imageNavigator = new ImageNavigatorPanel();
		tabbedPane_1.addTab("Image Navigator", null, _imageNavigator, null);
		
		_imageWindow = new ImageWindowPanel();
		_verticalSplit.setLeftComponent(_imageWindow);
		
		// Set up window property events
		_frame.addComponentListener(this);
		_verticalSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
		_horizontalSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
		
		
		// Set up global event responders
		GlobalEventManager.getInstance().addListener(this);
		
		_frame.pack();
		//_frame.setMinimumSize(_frame.getPreferredSize());
		_frame.setSize(new Dimension(800, 600));
		_frame.setLocationRelativeTo(null);
		
		GlobalEventManager.getInstance().addListener(this);
		
		refresh();
	}
	
	public void show(){
		_frame.setVisible(true);
	}
	
	public void hide(){
		_frame.setVisible(false);
	}
	
	public void close(){
		_frame.dispose();
	}
	
	public void refresh(){
		if(AppState.get().get("activeBatch") != null)
			_downloadBatchMenuItem.setEnabled(false);
		else
			_downloadBatchMenuItem.setEnabled(true);
	}
	
	public void onActiveBatchChanged(AppState appState, ActiveBatch activeBatch){
		refresh();
	}
	public void onActiveBatchChanged(AppState appState){
		refresh();
	}
	
	public void onSubmitBatchSuccess(BatchController controller, Request.SubmitBatchResponse response){
		AppState.get().put("activeBatch", null);
		AppState.get().save();
	}
	public void onSubmitBatchFailure(BatchController controller){
		GUI.globalErrorMessage("There was an error submitting the batch");
	}
	public void onWindowPositionChanged(AppState appState, Point position){
		_frame.setLocation(position);
	}
	
	public void onVerticalDividerLocationChanged(AppState appState, Integer position){
		_verticalSplit.setDividerLocation(position);
	}
	public void onHorizontalDividerLocationChanged(AppState appState, Integer position){
		_horizontalSplit.setDividerLocation(position);
	}
	public void onWindowHeightChanged(AppState appState, Integer height){
		_frame.setSize(_frame.getWidth(), height);
	}
	public void onWindowWidthChanged(AppState appState, Integer width){
		_frame.setSize(width, _frame.getHeight());
	}

	@Override
	public void componentResized(ComponentEvent e) { 
		AppState.get().put("windowHeight", _frame.getHeight());
		AppState.get().put("windowWidth", _frame.getWidth());
		_verticalSplit.firePropertyChange(JSplitPane.DIVIDER_LOCATION_PROPERTY, 0, _verticalSplit.getDividerLocation());
		_horizontalSplit.firePropertyChange(JSplitPane.DIVIDER_LOCATION_PROPERTY, 0, _horizontalSplit.getDividerLocation());
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		AppState.get().put("windowPosition", _frame.getLocation());
	}

	@Override
	public void componentShown(ComponentEvent e) { }

	@Override
	public void componentHidden(ComponentEvent e) { }

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String divider = ((JSplitPane) e.getSource()).getName();
		AppState.get().put(divider + "DividerLocation", ((JSplitPane) e.getSource()).getDividerLocation());
		
	}
}
