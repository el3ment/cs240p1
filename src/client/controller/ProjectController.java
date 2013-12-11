package client.controller;

import client.framework.Controller;
import shared.Request;
import client.AppState;

public class ProjectController extends Controller{
	
	
	// Returns list of projects for BatchWindow
	public Request.GetProjectsResponse getAllProjects(){
		Request.GetProjectsRequest request = new Request.GetProjectsRequest();
		request.username = (String) AppState.get().get("username");
		request.password = (String) AppState.get().get("password");
		
		Object response = this.submitRequest("getProjects", request);
		
		if(response != null && response.getClass().getName() != "java.lang.Boolean")
			return (Request.GetProjectsResponse) response;
		else
			return null;
	}
	
	// Returns sample image for a particular project
	public Request.GetSampleImageResponse getSampleImage(Integer projectId){
		Request.GetSampleImageRequest request = new Request.GetSampleImageRequest();
		request.username = (String) AppState.get().get("username");
		request.password = (String) AppState.get().get("password");
		request.projectId = projectId.toString();
		
		Object response = this.submitRequest("getSampleImage", request);
		
		if(response != null && response.getClass().getName() != "java.lang.Boolean")
			return (Request.GetSampleImageResponse) response;
		else
			return null;
	}
	
}
