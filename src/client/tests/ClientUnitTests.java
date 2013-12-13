package client.tests;

import org.junit.*;

import client.quality.SpellCorrector;
import static org.junit.Assert.*;

public class ClientUnitTests {
	
	@Before
	public void setup() {
	}
	
	@Test
	public void testClient(){
		assertTrue(true);
	}
	
	@After
	public void teardown() {
	}
	
	public static void main(String[] args) {

		String[] testClasses = new String[] {
				"client.tests.ClientUnitTests",
				"client.tests.QualityCheckerTests"
		};

		org.junit.runner.JUnitCore.main(testClasses);
	}
}

