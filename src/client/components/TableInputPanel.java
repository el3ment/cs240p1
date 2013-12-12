package client.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import client.AppState;
import client.framework.ActiveBatch;
import client.framework.GlobalEventManager;

// Table input view!
@SuppressWarnings("serial")
public class TableInputPanel extends JScrollPane implements ListSelectionListener, MouseListener{
	
	JTable _table = new JTable();
	ActiveBatch _model;
	SuggestionsWindow _suggestionWindow;
	JPopupMenu _suggestionMenu;
	
	// On creation refresh the model, build the view
	TableInputPanel(){
		
		refresh();
		rebuild();
		
        GlobalEventManager.getInstance().addListener(this);
	}
	
	// Called whenever the model changes
	public void refresh(){
		_model = (ActiveBatch) AppState.get().get("activeBatch");
		if(_model != null){
			_table.changeSelection(_model.getActiveCell().row, _model.getActiveCell().column, false, false);
		}
	}
	
	// Builds the actual JTable, scrollpane settings, etc.
	public void rebuild(){
		
		// Clean up a bit
		if(_table != null)
			this.getViewport().remove(_table);
		
		// If model exists - build the table
		if(_model != null){
			_table = new JTable();
			_table.setGridColor(Color.black);
			_table.setDefaultRenderer(Object.class, new Renderer());
	        _table.setSelectionMode(0);
	        _table.setModel(_model.getTableModel());
	        _table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        _table.getTableHeader().setReorderingAllowed(false);
	        _table.addMouseListener(this);
	        _table.getSelectionModel().addListSelectionListener(this);
	        _table.getColumnModel().getSelectionModel().addListSelectionListener(this);
	        _table.setPreferredScrollableViewportSize(null);
	        this.getViewport().add(_table);
	        _table.changeSelection(0, 1, false, false);
	        _table.getSelectionModel().setSelectionInterval(1, 1);
	        _table.setVisible(false);
	        _table.setVisible(true);
		}
	}
	
	// When activeBatch is updated by AppState
	public void onActiveBatchChanged(AppState appState, ActiveBatch activeBatch){
		refresh();
		rebuild();
	}
	
	// When activeBatch is nullified, generally happens onSubmit of a batch
	public void onActiveBatchChanged(AppState appState){
		// Active Batch Nullified
		refresh();
		rebuild();
	}
	
	// The renderer handles painting the cells red if they are invalid
	public class Renderer extends DefaultTableCellRenderer implements TableCellRenderer{

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
			this.setText(value.toString());
			
			// If invalid, paint it red, if it's selected, paint it blue
			// otherwise, paint it white
			if(!_model.isValidCell(row, column))
				setBackground(Color.RED);
			else if(hasFocus)
				setBackground(table.getSelectionBackground());
			else
				setBackground(Color.WHITE);
			
			return this;
		}
		
	}

	// When a selection is made (a cell is selected)
	@Override
	public void valueChanged(ListSelectionEvent e){
		_model.setActiveCell(_table.getSelectedRow(), _table.getSelectedColumn());
	}
	
	// Fired by ActiveBatch - this could be another form pushing a cellselection
	public void onCellSelected(ActiveBatch activeBatch, ActiveBatch.SelectedCell activeCell){
		refresh();
	}
	
	// When a record is updated outside of a cellSelected - usually by the quality checker
	public void onRecordUpdated(ActiveBatch activeBatch, String value){
		refresh();
	}


	// Handles showing the Use Suggestions menu if needed
	@Override
	public void mousePressed(MouseEvent e) {
		
		// Get the clicked cell
		int row = _table.rowAtPoint(e.getPoint());
		int column = _table.columnAtPoint(e.getPoint());
		
		// If it's a right click, and the cell is invalid, show the suggestion window
		if(SwingUtilities.isRightMouseButton(e) && row >= 0 && column >= 0){
			if(!_model.isValidCell(row, column)){
				_suggestionWindow = new SuggestionsWindow(row, column);
				_suggestionWindow.showPopup(e.getComponent(), e.getPoint());
			}
		}
		
	}

	// Unused listeners
	@Override public void mouseReleased(MouseEvent e) { }

	@Override public void mouseEntered(MouseEvent e) { }

	@Override public void mouseExited(MouseEvent e) { }
	
	@Override public void mouseClicked(MouseEvent e) { }
}
