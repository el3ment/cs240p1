package server;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import framework.Database;
import model.ProjectModel;
import model.UserModel;

/**
 * Processes an XML file, saving data into database
 */
public class DataImporter {
	
	public static class IndexerData {
		private ArrayList<UserModel.User> users = new ArrayList<UserModel.User>(); 
		private ArrayList<ProjectModel.Project> projects = new ArrayList<ProjectModel.Project>(); 

		public IndexerData(Element root) throws SQLException { 
			ArrayList<Element> rootElements = DataImporter.getChildElements(root); 
			
			ArrayList<Element> userElements = DataImporter.getChildElements(rootElements.get(0)); 
			for(Element userElement : userElements) { 
				users.add(new UserModel.User(userElement)); 
			} 

			ArrayList<Element> projectElements = DataImporter
					.getChildElements(rootElements.get(1)); 
			for(Element projectElement : projectElements) { 
				projects.add(new ProjectModel.Project(projectElement));
			} 
		} 
	}
	
	/**
	 * Reads XML file, parses into data structure, and saves it to 
	 * corresponding data structures, then saves them to the database.
	 *
	 * @param	xmlFilename	the filename of the XML file to import to the 
	 * 						database
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) 
			throws ParserConfigurationException, 
			SAXException, 
			IOException, 
			SQLException{
		
		extractTo(args[1], Server.getFilesLocation());
		
		importXML(findXMLFile(Server.getFilesLocation()));

	}
	
	private static String findXMLFile(String location) {
		// loop through files, find xml, return path to file
		//return "Records.xml";
		
		return location + "/Records/Records.xml";
	}

	public static void extractTo(String zipFilename, String destinationLocation) throws IOException{
		// extract zip file, save to other location
		
		Runtime.getRuntime().exec(String.format("unzip %s -d %s", zipFilename, destinationLocation));
	}
	
	public static void importXML(String filename)
			throws ParserConfigurationException,
			SAXException, 
			IOException,
			SQLException{
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); 
		Document doc = dBuilder.parse(new File(filename)); 
		
		doc.getDocumentElement().normalize(); 
		Element root = doc.getDocumentElement();
		
		// Empty Database
		Database.getInstance().empty();
				
		new IndexerData(root); 
	}
	
	/**
	 * Helper function to return a list of elements from a 
	 * given node
	 * TODO : look for ways to move this into the Node
	 * 	object returned so we can make these calls directly
	 *
	 * @param	node		a given node
	 * 
	 * @return				an ArrayList of elements
	 */
	public static ArrayList<Element> getChildElements(Node node) { 
		ArrayList<Element> result = new ArrayList<Element>(); 
		 
		NodeList children = node.getChildNodes(); 
		for(int i = 0; i < children.getLength(); i++) { 
			Node child = children.item(i); 
			if(child.getNodeType() == Node.ELEMENT_NODE){ 
				result.add((Element)child); 
			} 
		} 
		 
		return result; 
	}
	
	/**
	 * A helper function to extract the contents of the element
	 *
	 * @param	element		an element
	 * 
	 * @return				a string of the contents
	 */
	public static String getValue(Element element) { 
		String result = "";
		Node child = null;
		if(element != null)
			child = element.getFirstChild(); 
		
		if(child != null)
			result = child.getNodeValue(); 
		
		return result; 
	}
	
	public static String getFieldValue(Element element, String field){
		return DataImporter
   			 .getValue((Element) element.getElementsByTagName(field).item(0));
	}

	
}
