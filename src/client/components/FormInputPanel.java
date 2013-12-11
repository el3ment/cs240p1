package client.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import client.AppState;
import client.framework.ActiveBatch;
import client.framework.ActiveBatch.SelectedCell;
import client.framework.GlobalEventManager;

@SuppressWarnings("serial")
public class FormInputPanel extends JPanel implements ListSelectionListener, FocusListener{
	
	JScrollPane _formContainer;
	JList<ActiveBatch.Record> _list;
	ActiveBatch _model;
	SuggestionsWindow _suggestionsWindow;
	
	ArrayList<JLabel> _labels = new ArrayList<JLabel>();
	ArrayList<JTextField> _inputs = new ArrayList<JTextField>();
	
	// this has a list of rows on the left, and a text input for each field for each row. when you
	// select a field it highlights on the image viewer.
	// so when a field is selected, be sure to fireEvent()
	public FormInputPanel(){
		super();
		this.setLayout(new BorderLayout(0, 0));
		
		refresh();
		rebuild();
		
		GlobalEventManager.getInstance().addListener(this);
	}
	
	private JList<ActiveBatch.Record> _generateList(){
		JList<ActiveBatch.Record> list = new JList<ActiveBatch.Record>();
		list.getSelectionModel().addListSelectionListener(this);
		return list;
	}
	
	private JScrollPane _generateForm(){
		JScrollPane pane = new JScrollPane();
		JPanel form = new JPanel();
		pane.setViewportView(form);
		form.setLayout(new GridBagLayout());
		
		if(_model != null){
			
			// Start at 1 for Row Id#
			for(int i = 1; i < _model.getColumnCount(); i++){
				JLabel label = new JLabel(_model.getColumnName(i));
				JTextField input = new JTextField(10);
				input.setMinimumSize(input.getPreferredSize());
				input.addFocusListener(this);
				input.addMouseListener(new MouseAdapter(){
		            @Override
		            public void mouseClicked(MouseEvent e){
		            	
		            	int row = _list.getSelectedIndex();
		            	int column = _inputs.indexOf(e.getSource()) + 1;
		            	
		                if(SwingUtilities.isRightMouseButton(e) && _model.isValidCell(row, column)){
		                	_suggestionsWindow = new SuggestionsWindow(row, column);
		                	_suggestionsWindow.showPopup(e.getComponent(), e.getPoint());
		                }
		            }
		        });
				_inputs.add(input);
				_labels.add(label);
				
				GridBagConstraints gridbagconstraints = new GridBagConstraints();
				gridbagconstraints.gridx = 0;
				gridbagconstraints.gridy = i;
				gridbagconstraints.insets = new Insets(5, 5, 5, 0);
				form.add(label, gridbagconstraints);
				
				gridbagconstraints.gridx = 1;
				gridbagconstraints.gridy = i;
				gridbagconstraints.insets = new Insets(5, 5, 5, 0);
				form.add(input, gridbagconstraints);
				
			}
		}
		
		return pane;
		
	}
	
	public void refresh(){
		_model = (ActiveBatch) AppState.get().get("activeBatch");	
	}
	
	public void rebuild(){
		if(_model != null){
			// List of rows
			_list = _generateList();
			_list.setModel(_model.getListModel());
			_list.setSelectionMode(0);
			_list.setPreferredSize(new Dimension(80, 800));
			JScrollPane listPane = new JScrollPane(_list);
			this.add(listPane, BorderLayout.WEST);
	
			// Form
			_formContainer = _generateForm();
			this.add(_formContainer, BorderLayout.CENTER);
			
			// Select the active row
			_list.setSelectedIndex(_model.getActiveCell().row);
		}else{
			if(_list != null)
				this.removeAll();
		}
		
	}
	
	public void onActiveBatchChanged(AppState appState, ActiveBatch activeBatch){
		refresh();
		rebuild();
	}
	
	public void onActiveBatchChanged(AppState appState){
		// Active Batch Nullified
		refresh();
		rebuild();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		_model.setActiveRow(_list.getSelectedIndex());
	}
	
	private void _populateInputs(){
		for(int i = 1; i < _model.getColumnCount(); i++){
			// i-1 for Row Id
			_inputs.get(i - 1).setText((String) _model.getValueAt(_model.getActiveCell().row, i));
			
			if(!_model.isValidCell(_model.getActiveCell().row, i))
				_inputs.get(i - 1).setBackground(Color.RED);
			else
				_inputs.get(i - 1).setBackground(Color.WHITE);
		}
	}
	
	public void onCellSelected(ActiveBatch activeBatch, ActiveBatch.SelectedCell activeCell){
		// Loop through inputs, populating them with the right data
		_populateInputs();
		
		_list.setSelectedIndex(activeCell.row);
		
		if(activeCell.column > 0)
			_inputs.get(activeCell.column - 1).requestFocus();
	}

	@Override
	public void focusGained(FocusEvent e) {
		int columnIndex = _inputs.indexOf(e.getComponent());
		_model.setActiveColumn(columnIndex + 1);
	}

	@Override
	public void focusLost(FocusEvent e) {
		String value = ((JTextField) e.getComponent()).getText();
		int columnIndex = _inputs.indexOf(e.getComponent());
		
		// + 1 for the row Id
		_model.setValueAt(value, _model.getActiveCell().row, columnIndex + 1);
	}
	
	public void onRecordUpdated(ActiveBatch activeBatch, SelectedCell cell){
		_populateInputs();
	}
	
}
