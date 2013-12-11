package client.components;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.GUI;
import client.controller.BatchController;
import client.controller.ProjectController;
import client.framework.ActiveBatch;
import client.framework.AppUtilities;
import client.framework.GlobalEventManager;
import shared.Request;
import client.AppState;


public class DownloadBatchWindow{
	// dropdown with projects
	// be sure it's modal
	
	JDialog _frame;
	BatchController _batchController = new BatchController();
	ProjectController _projectController = new ProjectController();
	SampleImageDialog _imageDialog;
	
	public void create(){
		
		_frame = new JDialog();
		_frame.setModal(true);
		_frame.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		
		_frame.setTitle("Download Batch");
		_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		_frame.getContentPane().setLayout(new BorderLayout());
		
		// Create the panel, attach the listener then add it to the dialog
		DownloadBatchPanel downloadBatchPanel = new DownloadBatchPanel();
		GlobalEventManager.getInstance().addListener(downloadBatchPanel);
		_frame.add(downloadBatchPanel);
		
		// Create the sample image holder
		_imageDialog = new SampleImageDialog(_frame);
		
		_frame.setMinimumSize(_frame.getPreferredSize());
		_frame.setMaximumSize(_frame.getPreferredSize());
		_frame.setResizable(false);
		_frame.pack();
		_frame.setLocationRelativeTo(null);
		_frame.setAlwaysOnTop(true);
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
	
	@SuppressWarnings("serial")
	public class DownloadBatchPanel extends JPanel implements ActionListener{
		
		JComboBox _projectListing;
		ProjectController _projectController = new ProjectController();
		
		public DownloadBatchPanel(){
			super();

			this.setBorder(new EmptyBorder(5, 5, 5, 5));
			GridBagLayout gbl_contentPanel = new GridBagLayout();
			gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
			gbl_contentPanel.rowHeights = new int[]{0, 0};
			gbl_contentPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			this.setLayout(gbl_contentPanel);
		
			// List of projects
			_projectListing = new JComboBox();
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 0, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 0;
			gbc_comboBox.gridy = 0;
			this.add(_projectListing, gbc_comboBox);
			
			// Request Projects
			GlobalEventManager.getInstance().addListener(this);
			_projectController.getAllProjects();

			// View sample image button
			JButton viewSampleImage = new JButton("View Sample Image");
			GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
			gbc_btnNewButton.gridx = 1;
			gbc_btnNewButton.gridy = 0;
			this.add(viewSampleImage, gbc_btnNewButton);
			viewSampleImage.addActionListener(this);
			
			// Action buttons
			JPanel buttonPane = new JPanel();
			GridBagConstraints gbc_buttonPane = new GridBagConstraints();
			gbc_buttonPane.gridx = 0;
			gbc_buttonPane.gridy = 1;
			gbc_buttonPane.gridwidth = 2;
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			this.add(buttonPane, gbc_buttonPane);
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
			cancelButton.addActionListener(this);
			
			JButton downloadButton = new JButton("Download Batch");
			downloadButton.setActionCommand("Download Batch");
			buttonPane.add(downloadButton);
			_frame.getRootPane().setDefaultButton(downloadButton);
			downloadButton.addActionListener(this);
			
		}
		
		@Override
		public void actionPerformed(ActionEvent ae) {
		    String action = ae.getActionCommand();
		    switch(action){
			    case "Cancel":
			    	_frame.dispose();
			    	break;
			    case "Download Batch":
			    	Request.DownloadBatchResponse assignedBatch = _batchController.assignAndDownloadBatch(_getSelectedProjectId());
			    	if(assignedBatch == null)
			    		GUI.globalErrorMessage("There was an error downloading the batch");
			    	else
			    		_frame.dispose();
			    	break;
			    case "View Sample Image":
			    	
			    	Request.GetSampleImageResponse response = _projectController.getSampleImage(_getSelectedProjectId());
			    	String url = (response != null ? response.url : "http://www.commandercast.com/wp-content/uploads/2013/06/no_image_found.jpg");
			    	
			    	_imageDialog.dispose();
			    	_imageDialog = new SampleImageDialog(_frame);
			    	_imageDialog.add(AppUtilities.createImageLabel(url, 200, 200));
			    	_imageDialog.setVisible(true);
			    	
			    	break;
		    }
		    
		}
		
		private Integer _getSelectedProjectId(){
			Integer projectId = ((Request.GetProjectsResponse.ProjectResponse) _projectListing.getSelectedItem()).projectId;
	    	
			return projectId;
		}
		
		public void onGetProjectsSuccess(ProjectController controller, Request.GetProjectsResponse response){
			// Add projects to combo box
			_projectListing.setModel(new DefaultComboBoxModel(response.projects.toArray()));
			_projectListing.setSelectedIndex(0);
		}
		
		public void onDownloadBatchSuccess(BatchController controller, Request.DownloadBatchResponse response){
			AppState.get().put("activeBatch", new ActiveBatch(response));
			AppState.get().save();
		}
	}
	
	public class SampleImageDialog extends JDialog{
		public SampleImageDialog(JDialog parent){
			super(parent);
			this.setAlwaysOnTop(true);
			
			this.setMinimumSize(new Dimension(200, 200));
			this.setMaximumSize(new Dimension(200, 200));
			this.setResizable(false);
			this.pack();
			this.setLocationRelativeTo(null);
		}
	}
		
}
