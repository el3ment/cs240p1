package client;

import shared.Request;

public class SearchController extends Controller {
	public void search(String fields, String values){
		Request.SearchRequest request = new Request.SearchRequest();
		request.fields = fields;
		request.values = values;
		request.username = (String) AppState.get().get("username");
		request.password = (String) AppState.get().get("password");
		
		this.submitRequest("search", request);
	}
	
	public void loadFields(Integer projectId){
		Request.GetFieldsRequest request = new Request.GetFieldsRequest();
		request.username = (String) AppState.get().get("username");
		request.password = (String) AppState.get().get("password");
		request.projectId = projectId + "";
		
		System.out.println(projectId);
		
		this.submitRequest("getFields", request);
	}
	
	public void getAllProjects(){
		Request.GetProjectsRequest request = new Request.GetProjectsRequest();
		request.username = (String) AppState.get().get("username");
		request.password = (String) AppState.get().get("password");
		
		this.submitRequest("getProjects", request);
	}
	// public void onSearchSuccess(){}
}
