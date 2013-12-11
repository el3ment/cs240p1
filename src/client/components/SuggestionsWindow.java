package client.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
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
		_row = row;
		_column = column;
		_model = (ActiveBatch) AppState.get().get("activeBatch");
		
		_frame = new JDialog();
		_frame.setModal(true);
		_frame.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		_frame.setAlwaysOnTop(true);
		
		_frame.setTitle("Use Suggestions");
		_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		_frame.getContentPane().setLayout(new BorderLayout());
		
		JPanel container = new JPanel();
		
		JScrollPane listContainer = new JScrollPane();
		container.add(listContainer);
		
		_list = new SuggestionList(_model.getSuggestionList(row, column));
		listContainer.setViewportView(_list.getList());
		
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
		
		_frame.add(container);
		
		_frame.setMinimumSize(_frame.getPreferredSize());
		_frame.setMaximumSize(_frame.getPreferredSize());
		_frame.pack();
		_frame.setLocationRelativeTo(null);
	}
	
	public void finish(String selectedWord){
		_model.setValueAt(selectedWord, _row, _column);
		_frame.dispose();
	}
	
	public void show(){
		_frame.setVisible(true);
	}
	
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
		
		_popup.show(component, location.x, location.y);
	}
	
	public class SuggestionList implements ListSelectionListener{
		
		JList _list;
		
		public SuggestionList(HashSet<String> words){
			_list = new JList(words.toArray());
			_list.getSelectionModel().addListSelectionListener(this);
			_list.setSelectionMode(0);
		}
		
		public JList getList(){
			return _list;
		}
		
		public String getSelectedValue(){
			return (String) _list.getSelectedValue();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			_useSuggestion.setEnabled(true);
		}
	}
}
