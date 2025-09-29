package fieldCheckTools;

/*******
 * <p>
 * Title: nameChecker Class
 * </p>
 * 
 * <p>
 * Description: This class is part of the field checker package and allows for consistent
 * checking of name fields throughout the application. When calling the main function it will
 * return an error message if the password is invalid.
 * </p>
 * 
 * 
 * @author Lucas Conklin, 
 * 
 * 
 * 
 */

public class nameChecker {
	
	
	//Adapted function from the email checker class, returns true if the input contains a non alphabetical character
	public static boolean containsNonAlpha(String Input) 
	{
		char currChar;
		
		for(int i = 0; i < Input.length(); i++) 
		{
			currChar = Input.charAt(i);
			
			if (!((currChar >= 'A' && currChar <= 'Z') || // Check for A-Z
				(currChar >= 'a' && currChar <= 'z')))    // Check for a-z
				return true; //Non Alphanumeric character found
		}
		
		return false; //No non alphanumeric characters found
	}
	
	//Checks if input is valid
	public String evaluateName(String name) 
	{
		String newName = name;
		
		//Check if the name has double dashes, or has one at beginning/end
		if(newName.contains("--") || newName.charAt(0) == '-' || newName.charAt(newName.length() - 1) == '-') 
		{
			return "Invalid Use of Dashes"; //Return Error Message
		}
		
		newName = name.replaceAll("-", ""); //Remove all dashes
		
		//Check if non alphabetical characters are used
		if(containsNonAlpha(newName)) 
			return "Contains Non-Alphabetical Characters"; //Return Error Message
		else
			return "";
	}
}
