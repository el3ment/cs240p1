package server;

import java.sql.SQLException;
import java.util.ArrayList;

import model.FieldModel;
import model.UserModel;

import org.junit.* ;

import controller.FieldController;
import controller.ImageController;
import controller.UserController;
import framework.Controller.InvalidInputException;
import framework.Database;
import server.API;
import server.DataImporter;
import static org.junit.Assert.* ;
import framework.Controller;

public class ServerUnitTests {
	
	API apiInstance;
	UserController userController;
	ImageController imageController;
	FieldController fieldController;
	DataImporter importer;
	Database database;
	
	@Before
	public void setup() {
		apiInstance = new server.API();
		userController = new UserController();
		imageController = new ImageController();
		fieldController = new FieldController();
		database = Database.getInstance();
		
		//
		// Possible import of test database here
		//
	}

	
	@Test
	public void databaseConnection(){
		try{
			database.execute("SELECT 1");
		}catch(Exception e){
			fail("Database connection failure");
		}
	}
	
	private FieldModel.Field validField(){
		FieldModel.Field field = new FieldModel.Field();
		
		field.helphtml = "";
		field.knowndata = "";
		field.project_id = 1;
		field.xcoord = 1;
		field.width = 1;
		field.columnindex = 1;
		
		return field;
	}
	
	@Test
	public void basicCRUD(){
		try{
			FieldModel.Field field = validField();
			assertNotNull(FieldModel.getInstance().save(field));
			
		}catch(Exception e){
			fail("SQL Error on basicCRUD : " + e);
		}
	}
	
	
	@Test
	public void validateUser(){
		
		try{
			assertEquals("Invalid user/password", userController.login("test", "test"), null);
			assertNotNull("Valid User returning null", userController.login("sheila", "parker"));
		}catch(Exception e){
			fail("SQL Error " + e);
		}

		try{			
			userController.login("test", null);
			userController.login(null, "test");
			userController.login(null, null);
			fail("validateUser - Invalid input does not throw exception");
		}catch(Controller.InvalidInputException e){ 
		}catch(SQLException e){
			fail("SQL Exception " + e);
		}
	}
	
	@Test
	public void assignBatch(){
		try{
			userController.login("sheila", "parker");
			assertNotNull("Batch should be assigned, returned null", 
					userController.assignBatch(6)); 
			userController.assignBatch(6);
			fail("Subsequent assignBatch call should throw exception, but does not");
		}catch(UserController.BatchAlreadyAssignedException e){
			// This is intended
		}catch(Exception e){
			fail("SQL Error on assignBatch : " + e);
		}
	}
	
	@Test
	public void batchFormatter(){
		
		try{
			assertNotNull("Valid format returning null", 
					imageController.formatBatch( 
						"Robert, Pottorff, 25, White, 1;"
						+ "James, Dean, 40, Black, 1;"
						+ "Robert, Pottorff, 25, White, 1;"
						+ "James, Dean, 40, Black, 1;"
						+ "Robert, Pottorff, 25, White, 1;"
						+ "James, Dean, 40, Black, 1;"
						+ "Robert, Pottorff, 25, White, 1;"
						+ "James, Dean, 40, Black, 1;"
						+ "Robert, Pottorff, 25, White, 1;"
						+ "James, Dean, 40, Black, 1;"));
			assertNotNull("Valid format returning null", imageController.formatBatch("a,b;a,,"));
		}catch(Exception e){
			fail("Invalid exception thrown for batch formatter : " + e);
		}
	}
	
	@Test
	public void submitBatch() throws SQLException, InvalidInputException{
		UserModel.User currentUser = userController.login("sheila", "parker");
		ArrayList<ArrayList<String>> values = imageController.formatBatch(
			  "Robert, Pottorff, 25, White, 1;"
				+ "James, Dean, 40, Black, 1;"
				+ "Robert, Pottorff, 25, White, 1;"
				+ "James, Dean, 40, Black, 1;"
				+ "Robert, Pottorff, 25, White, 1;"
				+ "James, Dean, 40, Black, 1;"
				+ "Robert, Pottorff, 25, White, 1;"
				+ "James, Dean, 40, Black, 1;"
				+ "Robert, Pottorff, 25, White, 1;"
				+ "James, Dean, 40, Black, 1;");
		
		try{
			assertTrue("SubmitBatch should return true for valid input but does not", 
				imageController.submitBatch(62, values, currentUser));
			//TODO :  look for Robert Pottorff, fail if not found 
			// 		  (batch returned true but did not add to database)
			
		}catch(SQLException e){
			fail("SQL Error on submitBatch : " + e.getMessage());
		}
		
		try{
			imageController.submitBatch(-1, values, currentUser);
			imageController.submitBatch(62, null, currentUser);
			imageController.submitBatch(62, values, null);
			imageController.submitBatch(62, imageController.formatBatch("a,b;c,d"), currentUser);
			
			fail("Uncaught invalid exception for submitBatch");
		}catch(Controller.InvalidInputException e){ }
	}
	
	@Test
	public void getSampleImageForProject(){
		try{
			assertEquals(
				imageController.getSampleImageForProject(6).file, 
				"images/1890_image0.png");
			
		}catch(Exception e){
			fail("Invalid exception thrown for getSampleImageForProject : " + e);
		}
		
		// Expect exceptions
		try{
			assertEquals(imageController.getSampleImageForProject(-1), null);
			assertEquals(imageController.getSampleImageForProject(Integer.parseInt("test")), null);	
			
			fail("getSampleImageForProject - Invalid input does not throw exception");
		}catch(Controller.InvalidInputException e){ 
			
		}catch(Exception e){
			fail("Invalid exception thrown for getSampleImageForProject : " + e);
		}

	}
	
	@Test
	public void searchFields(){
		try{
			assertNotNull("Results should be found but returning null", 
					fieldController.search(
							"1,2,3,4,5,6,7,8,9,10,11,12,13", 
							"ACOSTA")
						.get(0).fieldId);
			assertNull("No results should be found retuning non-null", 
					fieldController.search("-1", "test"));			
		}catch(Exception e){
			fail("Invalid exception thrown searchFields : " + e);
		}
		
		try{			
			// Expect exceptions
			fieldController.search("1,", "ACOSTA");
			fieldController.search(null, "ACOSTA");
			fieldController.search("1,", null);
			fieldController.search(null, null);
			
			fail("searchFields - Invalid input does not throw exception");
		}catch(Controller.InvalidInputException e){ }
	}
	
	@Test
	public void findAllFields(){
		
		try{
			assertNotNull("Results should be found but returning null", 
					fieldController.findAllByProjectId(6));
			assertNull("No results should be found retuning non-null", 
					fieldController.findAllByProjectId(999999));			
		}catch(Exception e){
			fail("Invalid exception thrown findAllFields : " + e);
		}
		
		try{			
			// Expect exceptions
			fieldController.findAllByProjectId(-1);
			fieldController.findAllByProjectId(Integer.parseInt("-1"));
			
			fail("searchFields - Invalid input does not throw exception");
		}catch(Controller.InvalidInputException e){ 
			
		}catch(Exception e){
			fail("Invalid exception thrown findAllFields : " + e);
		}
	}
	
	
	@After
	public void teardown() { }
	
	public static void main(String[] args) {
		
		String[] testClasses = new String[] {
				"server.ServerUnitTests"
		};

		org.junit.runner.JUnitCore.main(testClasses);
	}
	
}

