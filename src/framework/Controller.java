package framework;

public abstract class Controller {
	@SuppressWarnings("serial")
	public class InvalidInputException extends Exception{
		public InvalidInputException(String message){
			super(message);
		}
	}
	
	protected boolean requireValidNotEmpty(String string) throws InvalidInputException{
		if(string != null && !string.isEmpty())
			return true;
		else
			throw new InvalidInputException("Null or empty input");
	}
	
	protected boolean requireValidPositive(int num) throws InvalidInputException{
		if(num > 0)
			return true;
		else
			throw new InvalidInputException(num + " is negative");
	}
	
	protected boolean requireEquals(int a, int b) throws InvalidInputException{
		if(a == b)
			return true;
		else{
			throw new InvalidInputException(a + " does not equal " + b);
		}
	}
	
	protected boolean requireNonNull(Object o) throws InvalidInputException{
		if(o != null)
			return true;
		else
			throw new InvalidInputException("Object is null");
	}
	protected boolean requireValidNumeric(String s) throws InvalidInputException{
		try{  
			Double.parseDouble(s);
			return true;
		}catch(NumberFormatException e){  
			throw new InvalidInputException(s + " is not numeric");
		}	
	}
}
