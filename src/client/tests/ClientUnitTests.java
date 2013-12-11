package client.tests;

import org.junit.*;
import static org.junit.Assert.*;

public class ClientUnitTests {
	
	@Before
	public void setup() {
	}
	
	@After
	public void teardown() {
	}
	
	
	
	
	// event manager tests
	// test if true singleton
	// test if add listener, fireEvent works
	
	
	
	
	
	
	@Test
	public void test_1() {		
		assertEquals("OK", "OK");
		assertTrue(true);
		assertFalse(false);
	}

	public static void main(String[] args) {

		String[] testClasses = new String[] {
				"client.ClientUnitTests"
		};

		org.junit.runner.JUnitCore.main(testClasses);
	}
}

