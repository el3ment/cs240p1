package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;

import shared.Request;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Server {

	public static String getFilesLocation(){
    	return "./demo/files/";
    }
	
    public static void main(String[] args) throws Exception {
    	int port = args.length > 0 ? Integer.parseInt(args[0]) : 39640;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
    
	/**
	 * Handles all requests for web-services and routes 
	 * requests to the appropriate function in API.java
	 * 
	 * TODO Use reflection to call the function dynamically
	 * 		on the API class based on the method passed in the URL
	 */
    static class MyHandler implements HttpHandler {
    	
    	/**
    	 * The handling function for all contexts
    	 *
    	 * @param	httpexchange	the HTTPExchange object
    	 */
        public void handle(HttpExchange t) throws IOException {
        	
        	XStream xmlStream = new XStream(new DomDriver());
        	
            String method = t.getRequestURI().getPath().replaceFirst("/", "");
            
            API api = new API();
            String response = "";
            String request = "";
            
            // Parse request body into a string, to be processed into XML
            int c;
            BufferedReader br = new BufferedReader(
            	new InputStreamReader(t.getRequestBody(), "UTF-8"));
            while ((c = br.read()) != -1) {
            	request += (char) c;
            }
            
            // Interpret the result as the appropriate class
            try{
	            if(method.equals("validateUser")){;
	            	response = xmlStream.toXML(
	            		api.validateUser(
	            			(Request.ValidateUserRequest) xmlStream.fromXML(request)));
	            	
	            }else if(method.equals("getProjects")){
	            	response = xmlStream.toXML(
	            		api.getProjects((Request.GetProjectsRequest) xmlStream.fromXML(request)));
	                
	            }else if(method.equals("getSampleImage")){
	            	response = xmlStream.toXML(
	            		api.getSampleImage(
	            			(Request.GetSampleImageRequest) xmlStream.fromXML(request)));
	                            	
	            }else if(method.equals("downloadBatch")){
	            	response = xmlStream.toXML(
	            		api.downloadBatch(
	            			(Request.DownloadBatchRequest) xmlStream.fromXML(request)));
	                            	
	            }else if(method.equals("submitBatch")){
	            	response = xmlStream.toXML(
	            		api.submitBatch((Request.SubmitBatchRequest) xmlStream.fromXML(request)));
	                            	
	            }else if(method.equals("getFields")){
	            	response = xmlStream.toXML(
	            		api.getFields((Request.GetFieldsRequest) xmlStream.fromXML(request)));
	                            	
	            }else if(method.equals("search")){
	            	response = xmlStream.toXML(
	            		api.search((Request.SearchRequest) xmlStream.fromXML(request)));
	                            	
	            }else{
	            	// Requesting file
	            	String path = Server.getFilesLocation() + t.getRequestURI().getPath();
	            	Scanner scanner = new Scanner( new File(path)).useDelimiter("\\A");
	            	response = scanner.next();
	            	scanner.close();
	            }
            }catch(Exception e){
            	response = xmlStream.toXML(false);
            }
            
            // Send the headers, and the response
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            
            os.write(response.getBytes());
            os.close();
        }
    }

}