package client.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
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
	boolean _isVisible = false;
	
	// This has a list of rows on the left, and a text input for each field for each row
	public FormInputPanel(){
		super();
		this.setLayout(new BorderLayout(0, 0));
		
		refresh();
		rebuild();
		
		GlobalEventManager.getInstance().addListener(this);
	}
	
	// Builds a JList
	private JList<ActiveBatch.Record> _generateList(){
		JList<ActiveBatch.Record> list = new JList<ActiveBatch.Record>();
		list.getSelectionModel().addListSelectionListener(this);
		return list;
	}
	
	// Builds a form in agreement with the active cell in _model
	private JScrollPane _generateForm(){
		JScrollPane pane = new JScrollPane();
		JPanel form = new JPanel();
		pane.setViewportView(form);
		form.setLayout(new GridBagLayout());
		
		if(_model != null){
			// Start at 1, adjusting for Row Id
			for(int i = 1; i < _model.getColumnCount(); i++){
				
				// Build label
				JLabel label = new JLabel(_model.getColumnName(i));
				
				// Build input
				JTextField input = new JTextField(10);
				input.setMinimumSize(input.getPreferredSize());
				input.addFocusListener(this);
				
				// When someone right clicks an invalid cell, show a "see suggestions" menu
				input.addMouseListener(new MouseAdapter(){
		            @Override
		            public void mouseClicked(MouseEvent e){
		            	
		            	int row = _list.getSelectedIndex();
		            	int column = _inputs.indexOf(e.getSource()) + 1;
		                if(SwingUtilities.isRightMouseButton(e) && !_model.isValidCell(row, column)){
		                	_suggestionsWindow = new SuggestionsWindow(row, column);
		                	_suggestionsWindow.showPopup(e.getComponent(), e.getPoint());
		                }
		            }
		        });
				
				// When a key is pressed, save the data
				input.addKeyListener(new KeyAdapter() {
		            public void keyReleased(KeyEvent e) {
		                JTextField textField = (JTextField) e.getSource();
		        		String value = textField.getText();
		        		int columnIndex = _inputs.indexOf(textField);
		        		
		        		// + 1 for the row Id
		        		_model.setValueAt(value, _model.getActiveCell().row, columnIndex + 1);
		                
		            }
		 
		            public void keyTyped(KeyEvent e) {}
		            public void keyPressed(KeyEvent e) {}
		        });
				
				_inputs.add(input);
				_labels.add(label);
				
				// Build and set layout constraints
				// Label
				GridBagConstraints gridbagconstraints = new GridBagConstraints();
				gridbagconstraints.gridx = 0;
				gridbagconstraints.gridy = i;
				gridbagconstraints.insets = new Insets(5, 5, 5, 0);
				form.add(label, gridbagconstraints);
				
				// Input
				gridbagconstraints.gridx = 1;
				gridbagconstraints.gridy = i;
				gridbagconstraints.insets = new Insets(5, 5, 5, 0);
				form.add(input, gridbagconstraints);
				
			}
		}
		
		return pane;
		
	}
	
	// Reset the model
	public void refresh(){
		_model = (ActiveBatch) AppState.get().get("activeBatch");	
	}
	
	// Rebuild the form and list to agree with the _model, called after refresh()
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
	
	// When activeBach is loaded or modified
	public void onActiveBatchChanged(AppState appState, ActiveBatch activeBatch){
		refresh();
		rebuild();
	}
	
	// When activeBatch is nullified (generally when it is sumbitted)
	public void onActiveBatchChanged(AppState appState){
		// Active Batch Nullified
		refresh();
		rebuild();
	}

	// When a list element is selected
	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selected = _list.getSelectedIndex();
	    int previous = selected == e.getFirstIndex() ? e.getLastIndex() : e.getFirstIndex();
	    
		_model.setActiveRow(selected);
	}
	
	// Scroll through inputs, grabbing the data from the model and populating it
	// also check for validity and paint the textbox the appropriate color
	private void _populateInputs(){
		if(_model != null){
			for(int i = 1; i < _model.getColumnCount(); i++){
				// i-1 for Row Id
				_inputs.get(i - 1).setText((String) _model.getValueAt(_model.getActiveCell().row, i));
				
				if(!_model.isValidCell(_model.getActiveCell().row, i))
					_inputs.get(i - 1).setBackground(Color.RED);
				else
					_inputs.get(i - 1).setBackground(Color.WHITE);
			}
		}
	}
	
	// When a cell is selected on the image, or the table
	public void onCellSelected(ActiveBatch activeBatch, ActiveBatch.SelectedCell activeCell){
		// Loop through inputs, populating them with the right data
		_populateInputs();
		
		if(_list != null){
			_list.setSelectedIndex(activeCell.row);
		}
		
		if(activeCell.column > 0 && _inputs != null && _inputs.size() > 0)
			_inputs.get(activeCell.column - 1).requestFocus();
	}

	// When a textbox is selected, update the active cell in the model
	// which will in turn update the image highlight and the table and the field help
	@Override
	public void focusGained(FocusEvent e) {
		int columnIndex = _inputs.indexOf(e.getComponent());
		_model.setActiveColumn(columnIndex + 1);
	}

	// When a textbox is unselected - save the data
	@Override
	public void focusLost(FocusEvent e) { }
	
	// When a record is updated - usually cellSelection will repopulate the data
	// but this catches any other possible reasons
	public void onRecordUpdated(ActiveBatch activeBatch, SelectedCell cell){
		_populateInputs();
	}
	
	public void onWindowFocusChanged(AppWindow window){
		if(_model != null && _model.getActiveCell().column > 0 && _inputs != null && _inputs.size() > 0)
			_inputs.get(_model.getActiveCell().column - 1).requestFocus();
	}
	
}
