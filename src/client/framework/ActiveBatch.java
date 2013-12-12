package client.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.DefaultListModel;
import javax.swing.table.AbstractTableModel;
import client.quality.SpellCorrector;
import shared.Request;

// Active Batch holds all the data for the current batch -- it's generally 
// called _model in the components
@SuppressWarnings("serial")
public class ActiveBatch{
	
	private ArrayList<Record> _records = new ArrayList<Record>();

	public String imageUrl;
	public Integer batchId;
	public Integer recordHeight;
	public Integer firstYCoord;
	
	transient ActiveBatch _parent = this;
	transient private ListModel listModel;
	transient private TableModel tableModel;
	transient private int _activeRow = -1;
	transient private int _activeColumn = -1;
	transient private boolean[][] _invalidCells;
	
	HashMap<Integer, SpellCorrector> _dictionaries = new HashMap<Integer, SpellCorrector>();
	 
	// Initialize transient variables when deserializing
	private Object readResolve() {
		_activeRow = 1;
	    _activeColumn = 1;
	    _invalidCells = new boolean[this.getRowCount()][this.getColumnCount()];
	    _parent = this;
		return this;
	}
	
	// Create an active batch from a downloadBatch response
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
				// Initialize the values to empty strings
				record.values.add("");
			}

			_records.add(record);
		}
		
		// Load suggestions from known values text files
		for(int j = 0; j < downloadBatch.numFields; j++){
			if(!downloadBatch.fields.get(j).knownValuesUrl.equals("")){
				_dictionaries.put(j, new SpellCorrector(AppUtilities.urlFromFragment(downloadBatch.fields.get(j).knownValuesUrl)));
			}
		}
		
		// Initialize invalid cells - has to be done after records have been
		// loaded so we know how many rows and columns
		_invalidCells = new boolean[this.getRowCount()][this.getColumnCount()];
		
	}

	// Simple record holder
	public class Record{
		public int id;
		public ArrayList<Request.DownloadBatchResponse.FieldResponse> fields;
		public ArrayList<String> values;
		
		// This is what is displayed by the form list
		public String toString(){
			return "" + id;
		}
	}
	
	// Updates the _invalidCells array
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
	
	// Returns the corrosponding _invalidCells value - says if a cell is invalid or not
	public boolean isValidCell(int row, int column){
		return !_invalidCells[row][column];
	}
	
	// The active cell packaged in one unit
	public static class SelectedCell{
		public int row;
		public int column;
		public SelectedCell(int row, int column){
			this.row = row;
			this.column = column;
		}
	}
	
	// A handy interpretation of activeBatch as a list model
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

	// A handy interpretation of activeBatch as a table model
	// as the data is naturally 2d like a table - most of the actual
	// code is here and the public methods on active batch
	// just call these functions as a pass-through
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
				
				// Update the invalidCell array
				determineValidCell(rowIndex, columnIndex);
				
				// Tell everyone it's been updated
				GlobalEventManager.getInstance().fireEvent(_parent, "recordUpdated", new SelectedCell(rowIndex, columnIndex));
			}
		}
		
		@Override
	    public boolean isCellEditable(int row, int column) {
			// Everything is editable except Row Number
		    return column != 0;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			// Call the first column Row Number
			if(columnIndex == 0)
				return "Row Number";
			else
				return _records.get(0).fields.get(columnIndex - 1).fieldTitle;
		}
	}
	
	// Gets the pixel width for a field - used by ImageWindow for drawing highlights
	public int getFieldWidth(int columnIndex){
		if(columnIndex == 0)
			return 0;
		else
			return _records.get(0).fields.get(columnIndex - 1).pixelWidth;
	}
	
	// Gets the xcoord of a cell - also used by ImageWindow to draw highlights
	public int getXCoord(int columnIndex){
		if(columnIndex == 0)
			return 0;
		else
			return _records.get(0).fields.get(columnIndex - 1).xCoord;
	}
	
	// Sets the active cell 
	public void setActiveCell(int rowIndex, int columnIndex){
		
		// Double check everything is on the up-and-up with the indexes
		if((_activeRow != rowIndex || _activeColumn != columnIndex) && (columnIndex > -1 && rowIndex > -1)){
			_activeRow = rowIndex;
			_activeColumn = columnIndex;
			GlobalEventManager.getInstance().fireEvent(this, "cellSelected", this.getActiveCell());
		}
	}
	
	// Just update the row
	public void setActiveRow(int rowIndex){
		if(_activeRow != rowIndex && rowIndex > -1){
			_activeRow = rowIndex;
			if(_activeColumn == -1)
				_activeColumn = 0;
			GlobalEventManager.getInstance().fireEvent(this, "cellSelected", this.getActiveCell());
		}
	}
	
	// Just update the column
	public void setActiveColumn(int columnIndex){
		
		if(_activeColumn != columnIndex && columnIndex > -1){
			_activeColumn = columnIndex;
			if(_activeRow == -1)
				_activeRow = 0;
			GlobalEventManager.getInstance().fireEvent(this, "cellSelected", this.getActiveCell());
		}
	}
	
	//  Returns a single object representation of the active row and column
	public SelectedCell getActiveCell(){
		return new SelectedCell(_activeRow, _activeColumn);
	}
	
	// Gets the field for the active column
	public Request.DownloadBatchResponse.FieldResponse getActiveField(){
		// If it's valid and not Row Id
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

	// Simple pass-through for methods that are useful
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
	
	// Creates if needed, then returns a list model
	public ListModel getListModel(){
		listModel = listModel == null ? new ListModel() : listModel;
		return listModel;
	}
	
	// Creates if needed, then returns a table model
	public TableModel getTableModel(){
		tableModel = tableModel == null ? new TableModel() : tableModel;
		return tableModel;
	}

	// Returns the suggestion list for a given cell - used by SuggestionWindow
	public HashSet<String> getSuggestionList(int row, int column) {
		String word = (String) this.getValueAt(row, column);
		return _dictionaries.get(column - 1).getSuggestedWords(word);
	}
	
}
