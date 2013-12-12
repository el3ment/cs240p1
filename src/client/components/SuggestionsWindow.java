package client.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import client.AppState;
import client.GUI;
import client.components.DownloadBatchWindow.DownloadBatchPanel;
import client.components.DownloadBatchWindow.SampleImageDialog;
import client.framework.ActiveBatch;
import client.framework.GlobalEventManager;

public class SuggestionsWindow {
	
	int _row;
	int _column;
	JDialog _frame;
	ActiveBatch _model;
	JButton _useSuggestion;
	SuggestionList _list;
	JPopupMenu _popup;
	
	public SuggestionsWindow(int row, int column){
		
		// Set variables used to set the suggestion after finish
		_row = row;
		_column = column;
		_model = (ActiveBatch) AppState.get().get("activeBatch");
		
		// Create the dialog
		_frame = new JDialog();
		_frame.setModal(true);
		_frame.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		_frame.setAlwaysOnTop(true);
		_frame.setTitle("Use Suggestions");
		_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		_frame.getContentPane().setLayout(new BorderLayout());
		
		// Create a content-holding panel and scrollpane for list
		JPanel container = new JPanel();
		JScrollPane listContainer = new JScrollPane();
		container.add(listContainer);
		
		// Create a list
		_list = new SuggestionList(_model.getSuggestionList(row, column));
		listContainer.setViewportView(_list.getList());
		
		// Add a use button
		_useSuggestion = new JButton("Use");
		_useSuggestion.setActionCommand("Use Suggestion");
		container.add(_useSuggestion);
		_useSuggestion.setEnabled(false);
		_useSuggestion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				finish(_list.getSelectedValue());
			}
		});
		
		// Add the overall container (has both list and buttons)
		_frame.add(container);
		
		_frame.setMinimumSize(new Dimension(300, 100));
		_frame.setMaximumSize(new Dimension(300, 100));
		_frame.setResizable(false);
		_frame.pack();
		_frame.setLocationRelativeTo(null);
	}
	
	// When the use button has been pressed
	public void finish(String selectedWord){
		_model.setValueAt(selectedWord, _row, _column);
		_frame.dispose();
	}
	
	public void show(){
		_frame.setVisible(true);
	}
	
	// Shows the popup
	// Called by the right click event listeners on different components
	public void showPopup(Component component, Point location){

		_popup = new JPopupMenu();
		JMenuItem seeSuggestions = new JMenuItem();
		seeSuggestions.setText("See Suggestions");
		_popup.add(seeSuggestions);
		_popup.setLocation(location);
		
		seeSuggestions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				show();
			}
		});
		
		// Using .show not .setVisible to avoid really buggy behavior
		_popup.show(component, location.x, location.y);
	}
	
	// A custom "list" for suggestions
	public class SuggestionList implements ListSelectionListener{
		
		JList _list;
		
		public SuggestionList(HashSet<String> words){
			_list = new JList(words.toArray());
			_list.getSelectionModel().addListSelectionListener(this);
			_list.setSelectionMode(0);
			_list.setMinimumSize(new Dimension(100, 100));
		}
		
		public JList getList(){
			return _list;
		}
		
		public String getSelectedValue(){
			return (String) _list.getSelectedValue();
		}

		// When an option is selected
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// Enable the use button
			_useSuggestion.setEnabled(true);
		}
	}
}
