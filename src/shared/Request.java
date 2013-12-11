package shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import server.API.NoResultsFoundException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import controller.UserController.InvalidUsernameOrPasswordException;


/**
 * A collection of Request and Response formats for 
 * the API. Each function has a unique class for both 
 * response and request.
 * This class also houses the shared request builder
 * and submit-er.
 */
public class Request {

	public static class ValidateUserRequest{
		public String username;
		public String password;
	}
	
	public static class ValidateUserResponse{
		public boolean isValid;
		public String firstName;
		public String lastName;
		public Integer numRecords;
		
		public String toString(){
			return (isValid ? "TRUE" : "FALSE");
		}
	}
	
	public static class GetProjectsRequest{
		public String username;
		public String password;
	}
	public static class GetProjectsResponse{
		public ArrayList<ProjectResponse> projects = new ArrayList<ProjectResponse>();
		
		public static class ProjectResponse{
			public Integer projectId;
			public String title;
			
			public String toString(){
				return (projectId > 0 ? projectId + " - " : "") + title;
			}
		}
		
		public String toString(){
			String response = "";
			for(int i = 0; i < projects.size(); i++){
				response += projects.get(i).projectId + "\n"
						+ projects.get(i).title + "\n";
			}
			return response;
		}
	}
	
	public static class GetSampleImageRequest{
		public String username;
		public String password;
		public String projectId;
	}
	public static class GetSampleImageResponse{
		public String url;
		
		public String toString(){
			return url;
		}
	}
	
	public static class DownloadBatchRequest{
		public String username;
		public String password;
		public String projectId;
	}
	
	public static class DownloadBatchResponse{
		public Integer batchId;
		public Integer projectId;
		public String imageURL;
		public Integer firstYCoord;
		public Integer recordHeight;
		public Integer numRecords;
		public Integer numFields;
		public ArrayList<FieldResponse> fields = new ArrayList<FieldResponse>();
		
		public static class FieldResponse{
			public Integer fieldId;
			public Integer fieldNum;
			public String fieldTitle;
			public String helpUrl;
			public Integer xCoord;
			public Integer pixelWidth;
			public String knownValuesUrl;
		}
		
		public String toString(){
			String response = batchId + "\n"
				+ projectId + "\n"
				+ imageURL + "\n"
				+ firstYCoord + "\n"
				+ recordHeight + "\n"
				+ numRecords + "\n"
				+ numFields + "\n";
			
			for(int i = 0; i < fields.size(); i++){
				response += fields.get(i).fieldId + "\n"
					+ fields.get(i).fieldNum + "\n"
					+ fields.get(i).fieldTitle + "\n"
					+ fields.get(i).helpUrl + "\n"
					+ fields.get(i).xCoord + "\n"
					+ fields.get(i).pixelWidth + "\n"
					+ (fields.get(i).knownValuesUrl != null 
						? fields.get(i).knownValuesUrl + "\n" 
						: "");
			}
			
			return response;
		}
	}
	
	public static class GetFieldsRequest{
		public String username;
		public String password;
		public String projectId;
	}
	public static class GetFieldsResponse{
		
		public ArrayList<FieldResponse> fields = new ArrayList<FieldResponse>();
		
		public static class FieldResponse{
			public Integer projectId;
			public Integer fieldId;
			public String title;
			
			public String toString(){
				return (fieldId > 0 ? fieldId + " - " : "") + title;
			}
		}
		
		public String toString(){
			String response = "";
			
			for(int i = 0; i < fields.size(); i++){
				response += fields.get(i).projectId + "\n"
					+ fields.get(i).fieldId + "\n"
					+ fields.get(i).title;
			}
			
			return response;
		}
	}
	
	public static class SubmitBatchRequest{
		public String username;
		public String password;
		public String batchId;
		public String fieldValues;
	}
	public static class SubmitBatchResponse{
		public boolean isValid;
		
		public String toString(){
			return (isValid ? "TRUE" : "FALSE");
		}
	}
	
	public static class SearchRequest{
		public String username;
		public String password;
		public String fields;
		public String values;
	}
	
	public static class SearchResponse{
		public ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		
		public static class SearchResult{
			public String batchId;
			public String imageUrl;
			public String recordNum;
			public String fieldId;
			
			public String toString(){
				return batchId + " - " + imageUrl;
			}
		}
		
		public String toString(){
			String response = "";
			for(int i = 0; i < results.size(); i++){
				response += results.get(i).batchId + "\n"
					+ results.get(i).imageUrl + "\n"
					+ results.get(i).recordNum + "\n"
					+ results.get(i).fieldId + "\n";
			}
			
			return response;
		}
	}

	/**
	 * Helper function, builds an XML string for requests from the params
	 *
	 * @param	params		an object, generally from the Request class 
	 * 						matching the request parameters
	 * 
	 * @return				XML representation of the object
	 */
	public static String buildRequest(Object params){
		XStream xmlStream = new XStream(new DomDriver());
		
		return xmlStream.toXML(params);
	}

	/**
	 * Builds an XML request, submits it to the host, and returns the server response
	 *
	 * @param	host		the host name of the web service
	 * @param	port		the port of the web service
	 * @param	method		the method name to be executed
	 * @param	params		an object, generally from the Request class
	 * 						matching the request parameters
	 * 
	 * @return				string response from the server, usually XML
	 */
	public static Object submitRequest(String host, String port, String method, Object params){
		// make connection
		String response = "";
		
		try{
			HttpURLConnection connection = 
				(HttpURLConnection) new URL("http", host, Integer.parseInt(port), "/" + method)
				.openConnection();
			connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible )");
			connection.setRequestProperty("Accept","*/*");
			connection.setRequestProperty("accept-charset", "UTF-8");
			connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			
			OutputStreamWriter writer = new OutputStreamWriter(
				connection.getOutputStream(), "UTF-8");
	        writer.write(String.format("%s", buildRequest(params)));
	        writer.close();
			
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), "UTF-8"));
		    
			try {
		        for (String line; (line = reader.readLine()) != null;) {
		            response += line + "\n";
		        }
		    } finally {
		        reader.close();
		    }
		}catch(Exception e){
			return null;
		}
		
		try{
			XStream xmlStream = new XStream(new DomDriver());
			response = response.trim();
			return xmlStream.fromXML(response.replace("\n\r", ""));
		}catch(Exception e){
			return null;
		}
	}	
}
