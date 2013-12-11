package client.controller;

import shared.Request;
import client.AppState;
import client.framework.ActiveBatch;
import client.framework.Controller;

public class BatchController extends Controller {
	
	// downloadBatch
	public Request.DownloadBatchResponse assignAndDownloadBatch(Integer projectId){
		Request.DownloadBatchRequest request = new Request.DownloadBatchRequest();
		request.projectId = projectId.toString();
		request.username = (String) AppState.get().get("username");
		request.password = (String) AppState.get().get("password");
		request.projectId = projectId.toString();
			
		Object response = this.submitRequest("downloadBatch", request);
		
		if(response != null && response.getClass().getName() != "java.lang.Boolean")
			return (Request.DownloadBatchResponse) response;
		else
			return null;
	}
	
	public Request.SubmitBatchResponse submitBatch(ActiveBatch activeBatch){
		Request.SubmitBatchRequest request = new Request.SubmitBatchRequest();
		request.batchId = activeBatch.batchId + "";
		request.username = (String) AppState.get().get("username");
		request.password = (String) AppState.get().get("password");
		request.fieldValues = "";
		for(int i = 0; i < activeBatch.getRowCount(); i++){
			for(int j = 1; j < activeBatch.getColumnCount(); j++){
				request.fieldValues += activeBatch.getValueAt(i, j) + ((i + 1 == activeBatch.getColumnCount()) ? "" : ",");
			}
			request.fieldValues += (i + 1 == activeBatch.getRowCount() ? "" : ";");
		}
		
		Object response = this.submitRequest("submitBatch", request);
		if(response != null && response.getClass().getName() != "java.lang.Boolean")
			return (Request.SubmitBatchResponse) response;
		else
			return null;
	}
}
