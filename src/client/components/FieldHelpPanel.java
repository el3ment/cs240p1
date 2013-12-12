package client.components;
import java.io.IOException;
import javax.swing.JEditorPane;
import client.AppState;
import client.framework.ActiveBatch;
import client.framework.AppUtilities;
import client.framework.GlobalEventManager;

// Field Help Panel shows an html document for each field that
// gives users instructions
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
	
	// Sets the pane content
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
	
	// Resets the form to be in accordance with the _model (Active Batch)
	public void refresh(){
		
		_model =  ((ActiveBatch) AppState.get().get("activeBatch"));
		
		if(_model != null && _model.getActiveField() != null){
			// Get the returned help url and display it
			String helpUrlFragment = _model.getActiveField().helpUrl;
			this.setSourceUrl(AppUtilities.urlFromFragment(helpUrlFragment));
		}else{
			// Clear the content
			this.setText(" ");
			this.setSourceUrl("");
		}
	}
	
	// Respond to cell selection
	public void onCellSelected(ActiveBatch activeBatch, ActiveBatch.SelectedCell activeCell){
		refresh();
	}
	
	// Called when activeBatch is changed (usually when it is loaded)
	public void onActiveBatchChanged(AppState appState, ActiveBatch activeBatch){
		refresh();
	}
	
	// Called when activeBatch is set to null (basically when it is submitted)
	public void onActiveBatchChanged(AppState appState){
		refresh();
	}
}
