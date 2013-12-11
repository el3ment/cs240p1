package client.components;

import java.awt.Point;
import java.io.IOException;

import javax.swing.JEditorPane;

import client.AppState;
import client.framework.ActiveBatch;
import client.framework.AppUtilities;
import client.framework.GlobalEventManager;

@SuppressWarnings("serial")
public class FieldHelpPanel extends JEditorPane{
	
	JEditorPane _content = new JEditorPane();
	String _sourceUrl;
	ActiveBatch _model;
	
	public FieldHelpPanel(){
		super();
		
		this.setEditable(false);
		this.setContentType("text/html");
		
		GlobalEventManager.getInstance().addListener(this);
	}
	
	private void setSourceUrl(String url){
		
		if(url != _sourceUrl){
			_sourceUrl = url;
			try {
				this.setPage(_sourceUrl);
			} catch (IOException e) {
				// Noop.
			}
		}
	}
	public void refresh(){
		
		_model =  ((ActiveBatch) AppState.get().get("activeBatch"));
		
		if(_model != null && _model.getActiveField() != null){
			String helpUrlFragment = _model.getActiveField().helpUrl;
			this.setSourceUrl(AppUtilities.urlFromFragment(helpUrlFragment));
		}else{
			this.setText(" ");
			this.setSourceUrl("");
		}
	}
	
	// Respond to cell selection
	public void onCellSelected(ActiveBatch activeBatch, ActiveBatch.SelectedCell activeCell){
		refresh();
	}
	
	public void onActiveBatchChanged(AppState appState, ActiveBatch activeBatch){
		refresh();
	}
	public void onActiveBatchChanged(AppState appState){
		refresh();
	}
}
