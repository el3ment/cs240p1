package client.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

import client.quality.Dictionary;
import client.quality.SpellCorrector;
import shared.Request;

@SuppressWarnings("serial")
public class ActiveBatch{
	private ArrayList<Record> _records = new ArrayList<Record>();
	transient private int _activeRow = -1;
	transient private int _activeColumn = -1;
	
	public String imageUrl;
	public Integer batchId;
	public Integer recordHeight;
	public Integer firstYCoord;
	
	transient private ListModel listModel;
	transient private TableModel tableModel;
	
	transient private boolean[][] _invalidCells;
	HashMap<Integer, SpellCorrector> _dictionaries = new HashMap<Integer, SpellCorrector>();
	transient ActiveBatch _parent = this;
	 
	// Initialize transient variables
	private Object readResolve() {
		_activeRow = -1;
	    _activeColumn = -1;
	    _invalidCells = new boolean[this.getRowCount()][this.getColumnCount()];
	    _parent = this;
		return this;
	}
			
	public ActiveBatch(Request.DownloadBatchResponse downloadBatch){
		
		imageUrl = downloadBatch.imageURL;
		firstYCoord = downloadBatch.firstYCoord;
		batchId = downloadBatch.batchId;
		recordHeight = downloadBatch.recordHeight;
		
		for(int i = 0; i < downloadBatch.numRecords; i++){
			Record record = new Record();
			record.id = i;
			record.fields = downloadBatch.fields;
			record.values = new ArrayList<String>();
			for(int j = 0; j < downloadBatch.numFields; j++){
				record.values.add("");
			}

			_records.add(record);
		}
		
		// Load suggestions
		for(int j = 0; j < downloadBatch.numFields; j++){
			if(!downloadBatch.fields.get(j).knownValuesUrl.equals("")){
				_dictionaries.put(j, new SpellCorrector(AppUtilities.urlFromFragment(downloadBatch.fields.get(j).knownValuesUrl)));
			}
		}
		
		// Initialize invalid cells
		_invalidCells = new boolean[this.getRowCount()][this.getColumnCount()];
		
	}

	public class Record{
		public int id;
		public ArrayList<Request.DownloadBatchResponse.FieldResponse> fields;
		public ArrayList<String> values;
		
		public String toString(){
			return "" + id;
		}
	}
	
	
	public void determineValidCell(int row, int column){
		if(column > 0){
			String value = (String) this.getValueAt(row, column);
			if(!_records.get(row).fields.get(column - 1).knownValuesUrl.equals("") && !value.isEmpty()){
				_invalidCells[row][column] = !_dictionaries.get(column - 1).contains(value);
			}else{
				// Assume if there is no known values the data is valid
				_invalidCells[row][column] = false;
			}
		}
	}
	
	public boolean isValidCell(int row, int column){
		return !_invalidCells[row][column];
	}
	
	public static class SelectedCell{
		public int row;
		public int column;
		public SelectedCell(int row, int column){
			this.row = row;
			this.column = column;
		}
	}
	
	public class ListModel extends DefaultListModel<Record>{
		@Override
		public int getSize() {
			return _records.size();
		}

		@Override
		public Record getElementAt(int index) {
			return _records.get(index);
		}
	}

	public class TableModel extends AbstractTableModel{

		@Override
		public int getRowCount() {
			return _records.size();
		}

		@Override
		public int getColumnCount() {
			if(_records.get(0) != null)
				return _records.get(0).fields.size() + 1;
			else
				return 0;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex){
			if(columnIndex == 0)
				return rowIndex + "";
			else
				return _records.get(rowIndex).values.get(columnIndex - 1);
		}
		
		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex){
			if(columnIndex > 0){
				_records.get(rowIndex).values.set(columnIndex - 1, (String) value);
				determineValidCell(rowIndex, columnIndex);
				GlobalEventManager.getInstance().fireEvent(_parent, "recordUpdated", new SelectedCell(rowIndex, columnIndex));
			}
		}
		
		@Override
	    public boolean isCellEditable(int row, int column) {
		    return column != 0;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if(columnIndex == 0)
				return "Row Number";
			else
				return _records.get(0).fields.get(columnIndex - 1).fieldTitle;
		}
	}
	
	public int getFieldWidth(int columnIndex){
		if(columnIndex == 0)
			return 0;
		else
			return _records.get(0).fields.get(columnIndex - 1).pixelWidth;
	}
	public int getXCoord(int columnIndex){
		if(columnIndex == 0)
			return 0;
		else
			return _records.get(0).fields.get(columnIndex - 1).xCoord;
	}
	
	public void setActiveCell(int rowIndex, int columnIndex){
		
		if((_activeRow != rowIndex || _activeColumn != columnIndex) && (columnIndex > -1 && rowIndex > -1)){
			_activeRow = rowIndex;
			_activeColumn = columnIndex;
			GlobalEventManager.getInstance().fireEvent(this, "cellSelected", this.getActiveCell());
		}
	}
	
	public void setActiveRow(int rowIndex){
		if(_activeRow != rowIndex && rowIndex > -1){
			_activeRow = rowIndex;
			if(_activeColumn == -1)
				_activeColumn = 0;
			GlobalEventManager.getInstance().fireEvent(this, "cellSelected", this.getActiveCell());
		}
	}
	
	public void setActiveColumn(int columnIndex){
		if(_activeColumn != columnIndex && columnIndex > -1){
			_activeColumn = columnIndex;
			if(_activeRow == -1)
				_activeRow = 0;
			GlobalEventManager.getInstance().fireEvent(this, "cellSelected", this.getActiveCell());
		}
	}
	
	public SelectedCell getActiveCell(){
		return new SelectedCell(_activeRow, _activeColumn);
	}
	
	public Request.DownloadBatchResponse.FieldResponse getActiveField(){
		if(_isValidRow(_activeRow) && _isValidColumn(_activeColumn) && _activeColumn != 0)
			return _records.get(_activeRow).fields.get(_activeColumn - 1);
		else
			return null;
	}
	
	private boolean _isValidRow(Integer row){
		return row < this.getRowCount() && row >= 0;
	}
	
	private boolean _isValidColumn(int column){
		return column < this.getColumnCount() && column >= 0;
	}

	public int getRowCount() {
		return this.getTableModel().getRowCount();
	}

	public int getColumnCount() {
		return this.getTableModel().getColumnCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex){
		return this.getTableModel().getValueAt(rowIndex, columnIndex);
	}
	
	public void setValueAt(Object value, int rowIndex, int columnIndex){
		this.getTableModel().setValueAt(value, rowIndex, columnIndex);
	}
	
	public String getColumnName(int columnIndex) {
	    return this.getTableModel().getColumnName(columnIndex);
	}
	
	public ListModel getListModel(){
		listModel = listModel == null ? new ListModel() : listModel;
		return listModel;
	}
	
	public TableModel getTableModel(){
		tableModel = tableModel == null ? new TableModel() : tableModel;
		return tableModel;
	}

	public HashSet<String> getSuggestionList(int row, int column) {
		String word = (String) this.getValueAt(row, column);
		return _dictionaries.get(column - 1).getSuggestedWords(word);
	}
	
}
