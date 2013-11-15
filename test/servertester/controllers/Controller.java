package servertester.controllers;

import java.util.*;

import com.thoughtworks.xstream.XStream;

import shared.Request.*;
import shared.Request;
import servertester.views.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Controller implements IController {

	private IView _view;
	
	public Controller() {
		return;
	}
	
	public IView getView() {
		return _view;
	}
	
	public void setView(IView value) {
		_view = value;
	}
	
	// IController methods
	//
	
	@Override
	public void initialize() {
		getView().setHost("localhost");
		getView().setPort("39640");
		operationSelected();
	}

	@Override
	public void operationSelected() {
		ArrayList<String> paramNames = new ArrayList<String>();
		paramNames.add("User");
		paramNames.add("Password");
		
		switch (getView().getOperation()) {
		case VALIDATE_USER:
			break;
		case GET_PROJECTS:
			break;
		case GET_SAMPLE_IMAGE:
			paramNames.add("Project");
			break;
		case DOWNLOAD_BATCH:
			paramNames.add("Project");
			break;
		case GET_FIELDS:
			paramNames.add("Project");
			break;
		case SUBMIT_BATCH:
			paramNames.add("Batch");
			paramNames.add("Record Values");
			break;
		case SEARCH:
			paramNames.add("Fields");
			paramNames.add("Search Values");
			break;
		default:
			assert false;
			break;
		}
		
		getView().setRequest("");
		getView().setResponse("");
		getView().setParameterNames(paramNames.toArray(new String[paramNames.size()]));
	}

	@Override
	public void executeOperation() {
		switch (getView().getOperation()) {
		case VALIDATE_USER:
			validateUser();
			break;
		case GET_PROJECTS:
			getProjects();
			break;
		case GET_SAMPLE_IMAGE:
			getSampleImage();
			break;
		case DOWNLOAD_BATCH:
			downloadBatch();
			break;
		case GET_FIELDS:
			getFields();
			break;
		case SUBMIT_BATCH:
			submitBatch();
			break;
		case SEARCH:
			search();
			break;
		default:
			assert false;
			break;
		}
	}
	
	private void validateUser() {
		String[] params = getView().getParameterValues();
		
		Request.ValidateUserRequest request = new Request.ValidateUserRequest();
		request.username = params[0];
		request.password = params[1];
		
		String xml = Request.buildRequest(request);
		getView().setRequest(Request.buildRequest(request));
		getView().setResponse(
			parseForPassoff(Request.submitRequest(getView().getHost(), getView().getPort(), "validateUser", request))
		);
	}
	
	private void getProjects() {
		String[] params = getView().getParameterValues();
		
		Request.GetProjectsRequest request = new Request.GetProjectsRequest();
		request.username = params[0];
		request.password = params[1];
		
		String xml = Request.buildRequest(request);
		getView().setRequest(Request.buildRequest(request));
		getView().setResponse(
			parseForPassoff(Request.submitRequest(getView().getHost(), getView().getPort(), "getProjects", request))
		);
		
	}
	
	private void getSampleImage() {
		String[] params = getView().getParameterValues();
		
		Request.GetSampleImageRequest request = new Request.GetSampleImageRequest();
		request.username = params[0];
		request.password = params[1];
		request.projectId = params[2];
		
		String xml = Request.buildRequest(request);
		getView().setRequest(Request.buildRequest(request));
		getView().setResponse(
			parseForPassoff(Request.submitRequest(getView().getHost(), getView().getPort(), "getSampleImage", request))
		);

	}
	
	private void downloadBatch() {
		String[] params = getView().getParameterValues();
		
		Request.DownloadBatchRequest request = new Request.DownloadBatchRequest();
		request.username = params[0];
		request.password = params[1];
		request.projectId = params[2];
		
		String xml = Request.buildRequest(request);
		getView().setRequest(Request.buildRequest(request));
		getView().setResponse(
			parseForPassoff(Request.submitRequest(getView().getHost(), getView().getPort(), "downloadBatch", request)
		));

	}
	
	private void getFields() {
		String[] params = getView().getParameterValues();
		
		Request.GetFieldsRequest request = new Request.GetFieldsRequest();
		request.username = params[0];
		request.password = params[1];
		request.projectId = params[2];
		
		String xml = Request.buildRequest(request);
		getView().setRequest(Request.buildRequest(request));
		getView().setResponse(
			parseForPassoff(Request.submitRequest(getView().getHost(), getView().getPort(), "getFields", request))
		);

	}
	
	private void submitBatch() {
		String[] params = getView().getParameterValues();
		
		Request.SubmitBatchRequest request = new Request.SubmitBatchRequest();
		request.username = params[0];
		request.password = params[1];
		request.batchId = params[2];
		request.fieldValues = params[3];
		
		String xml = Request.buildRequest(request);
		getView().setRequest(Request.buildRequest(request));
		getView().setResponse(
			parseForPassoff(Request.submitRequest(getView().getHost(), getView().getPort(), "submitBatch", request))
		);

	}
	
	private void search() {
		String[] params = getView().getParameterValues();
		
		Request.SearchRequest request = new Request.SearchRequest();
		request.username = params[0];
		request.password = params[1];
		request.fields = params[2];
		request.values = params[3];
		
		String xml = Request.buildRequest(request);
		getView().setRequest(Request.buildRequest(request));
		getView().setResponse(
			parseForPassoff(Request.submitRequest(getView().getHost(), getView().getPort(), "search", request))
		);

	}
	
	public String parseForPassoff(String xml){
		XStream xmlStream = new XStream(new DomDriver());
		
		try{
			xml = xml.trim();
			Object o = xmlStream.fromXML(xml.replace("\n\r", ""));
			if(o == false){
				return "FAILED";
			}else{
				return o.toString().replaceAll("<URL>", "http://" + getView().getHost() + ":" + getView().getPort());
			}
		}catch(Exception e){
			return "FAILED";
		}
	}

}

