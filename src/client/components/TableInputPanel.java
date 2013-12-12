package client.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
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

@SuppressWarnings("serial")
public class TableInputPanel extends JScrollPane implements ListSelectionListener, MouseListener{
	
	JTable _table = new JTable();
	ActiveBatch _model;
	SuggestionsWindow _suggestionWindow;
	JPopupMenu _suggestionMenu;
	
	TableInputPanel(){
		
		refresh();
		rebuild();
		
        GlobalEventManager.getInstance().addListener(this);
	}
	
	public void refresh(){
		_model = (ActiveBatch) AppState.get().get("activeBatch");
		if(_model != null){
			_table.changeSelection(_model.getActiveCell().row, _model.getActiveCell().column, false, false);
		}
	}
	
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
	
	public void onActiveBatchChanged(AppState appState, ActiveBatch activeBatch){
		refresh();
		rebuild();
	}
	public void onActiveBatchChanged(AppState appState){
		// Active Batch Nullified
		refresh();
		rebuild();
	}
	
	public class Renderer extends DefaultTableCellRenderer implements TableCellRenderer{

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
			this.setText(value.toString());
			
			setBackground(Color.RED);
			if(!_model.isValidCell(row, column))
				setBackground(Color.RED);
			else if(hasFocus)
				setBackground(table.getSelectionBackground());
			else
				setBackground(Color.WHITE);
			
			return this;
		}
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e){
		_model.setActiveCell(_table.getSelectedRow(), _table.getSelectedColumn());
	}
	
	public void onCellSelected(ActiveBatch activeBatch, ActiveBatch.SelectedCell activeCell){
		refresh();
	}
	
	public void onRecordUpdated(ActiveBatch activeBatch, String value){
		refresh();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
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

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
