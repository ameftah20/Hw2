package passwordPopUpWindow;

import javafx.scene.paint.Color;

/*******
 * <p> Title: Model Class - establishes the required GUI data and the computations.
 * </p>
 *
 * <p> Description: This Model class is a major component of a Model View Controller (MVC)
 * application design that provides the user with a Graphical User Interface using JavaFX
 * widgets as opposed to a command line interface.
 * 
 * In this case the Model deals with an input from the user and checks to see if it conforms to
 * the requirements specified by a graphical representation of a finite state machine.
 * 
 * This is a purely static component of the MVC implementation.  There is no need to instantiate
 * the class.
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 *
 * @version 2.00	2025-07-30 Rewrite of this application for the Fall 2025 offering of CSE 360
 * and other ASU courses.
 */

public class Model {
		
	/*******
	 * <p> Title: updatePassword - Protected Method </p>
	 * 
	 * <p> Description: This method is called every time the user changes the password (e.g., with 
	 * every key pressed) using the GUI from the PasswordEvaluationGUITestbed.  It resets the 
	 * messages associated with each of the requirements and then evaluates the current password
	 * with respect to those requirements.  The results of that evaluation are display via the View
	 * to the user and via the console.</p>
	 */

	protected static void updatePassword() {
		View.resetAssessments();						// Reset the assessment flags to the
		String password = View.text_Password.getText();	// initial state and fetch the input
		
		// If the input is empty, clear the aspects of the user interface having to do with the
		// user input and tell the user that the input is empty.
		if (password.isEmpty()) {
			View.errPasswordPart1.setText("");
			View.errPasswordPart2.setText("");
			View.noInputFound.setText("No input text found!");
		}
		else
		{
			// There is user input, so evaluate it to see if it satisfies the requirements
			String errMessage = evaluatePassword(password);
			
			// Based on the evaluation, change the flag to green for each satisfied requirement
			updateFlags();
			
			// An empty string means there is no error message, which means the input is valid
			if (errMessage != "") {
				
				// Since the output is not empty, at least one requirement have not been satisfied.
				System.out.println(errMessage);			// Display the message to the console
				
				View.noInputFound.setText("");			// There was input, so no error message
				
				// Extract the input up to the point of the error and place it in Part 1
				View.errPasswordPart1.setText(password.substring(0, passwordIndexofError));
				
				// Place the red up arrow into Part 2
				View.errPasswordPart2.setText("\u21EB");
				
				// Tell the user about the meaning of the red up arrow
				View.errPasswordPart3.setText(
						"The red arrow points at the character causing the error!");
				
				// Tell the user that the password is not valid with a red message
				View.validPassword.setTextFill(Color.RED);
				View.validPassword.setText("Failure! The password is not valid.");
				
				// Ensure the button is disabled
				View.button_Finish.setDisable(true);
			}
			else {
				// All the requirements were satisfied - the password is valid
				System.out.println("Success! The password satisfies the requirements.");
				
				// Hide all of the error messages elements
				View.errPasswordPart1.setText("");
				View.errPasswordPart2.setText("");
				View.errPasswordPart3.setText("");
				
				// Tell the user that the password is valid with a green message
				View.validPassword.setTextFill(Color.GREEN);
				View.validPassword.setText("Success! The password satisfies the requirements.");
				
				// Enable the button so the user can accept this password or continue to add
				// more characters to the password and make it longer.
				View.button_Finish.setDisable(false);
			} 
		}
	}
	
	/*-********************************************************************************************
	 * 
	 * Attributes used by the Finite State Machine to inform the user about what was and was not
	 * valid and point to the character of the error.  This will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running

	/*
	 * This private method displays the input line and then on a line under it displays the input
	 * up to the point of the error.  At that point, a question mark is place and the rest of the 
	 * input is ignored. This method is designed to be used to display information to make it clear
	 * to the user where the error in the input can be found, and show that on the console 
	 * terminal.
	 * 
	 */

	private static void displayInputState() {
		// Display the entire input line
		System.out.println(inputLine);
		System.out.println(inputLine.substring(0,currentCharNdx) + "?");
		System.out.println("The password size: " + inputLine.length() + "  |  The currentCharNdx: " + 
				currentCharNdx + "  |  The currentChar: \"" + currentChar + "\"");
	}
	
	
	/*
	 * This private method checks each of the requirements and if one is satisfied, it changes the
	 * the text to tell the user of this fact and changes the text color from red to green.
	 * 
	 */
	
	private static void updateFlags() {
		if (foundUpperCase) {
			View.label_UpperCase.setText("At least one upper case letter - Satisfied");
			View.label_UpperCase.setTextFill(Color.GREEN);
		}

		if (foundLowerCase) {
			View.label_LowerCase.setText("At least one lower case letter - Satisfied");
			View.label_LowerCase.setTextFill(Color.GREEN);
		}

		if (foundNumericDigit) {
			View.label_NumericDigit.setText("At least one numeric digit - Satisfied");
			View.label_NumericDigit.setTextFill(Color.GREEN);
		}

		if (foundSpecialChar) {
			View.label_SpecialChar.setText("At least one special character - Satisfied");
			View.label_SpecialChar.setTextFill(Color.GREEN);
		}

		if (foundLongEnough) {
			View.label_LongEnough.setText("At least eight characters - Satisfied");
			View.label_LongEnough.setTextFill(Color.GREEN);
		}
	}
	

	/**********
	 * <p> Title: evaluatePassword - Public Method </p>
	 *
	 * <p> Description: This method is a mechanical transformation of a Directed Graph diagram
	 * into a Java method. This method is used by both the GUI version of the application as well
	 * as the testing automation version.
	 *
	 * The method evaluates the input against the following requirements:
	 *   1) At least one upper-case letter
	 *   2) At least one lower-case letter
	 *   3) At least one numeric digit
	 *   4) At least one special (non-alphanumeric) character
	 *   5) Length between 8 and 32 characters, inclusive
	 *
	 * If any requirement is not satisfied, this method prepares a helpful error message and
	 * records the index of the error in passwordIndexofError so the GUI can point to it with
	 * the red up arrow.
	 *
	 * @param inputText   The input string evaluated by the directed graph processing
	 * @return            An output string that is empty if everything is okay or it will be
	 *                    a string with a helpful description of the error(s).
	 */
	public static String evaluatePassword(String inputText) {
		/* Reset the stored input/error context and all requirement flags */
		passwordErrorMessage = "";
		passwordInput = inputText;
		passwordIndexofError = -1;

		foundUpperCase = false;
		foundLowerCase = false;
		foundNumericDigit = false;
		foundSpecialChar = false;
		foundLongEnough = false;

		inputLine = inputText;            // Console trace helper
		currentCharNdx = 0;               // Initialize the scanner index

		/* Handle empty input explicitly so the GUI can show a clear message */
		if (inputText == null || inputText.length() == 0) {
			foundLongEnough = false;      // Explicitly false for clarity

			// Point the arrow at the beginning to show "no characters"
			passwordIndexofError = 0;

			StringBuilder sb = new StringBuilder();
			sb.append("No input text found!\n");
			sb.append("Password must be at least 8 characters long and include: ")
			  .append("an uppercase letter, a lowercase letter, a digit, and a special character.\n");

			passwordErrorMessage = sb.toString();
			// Console trace to match the testing harness's "FSM execution trace:"
			displayInputState();
			return passwordErrorMessage;
		}

		/* Scan the input one character at a time and set the requirement flags */
		for (currentCharNdx = 0; currentCharNdx < inputText.length(); currentCharNdx++) {
			currentChar = inputText.charAt(currentCharNdx);

			if (Character.isUpperCase(currentChar))
				foundUpperCase = true;
			else if (Character.isLowerCase(currentChar))
				foundLowerCase = true;
			else if (Character.isDigit(currentChar))
				foundNumericDigit = true;
			else
				// Anything not a letter/digit is treated as a special character
				foundSpecialChar = true;
		}

		/* Length checks are performed after scanning */
		int len = inputText.length();
		if (len >= 8) foundLongEnough = true;

		/* Build a helpful error message if any requirement failed */
		StringBuilder err = new StringBuilder();

		// 1) Enforce the new maximum length rule first so the pointer is most useful
		if (len > 32) {
			// Point to the first character beyond the limit (index 32) so the user can see
			// where the overflow begins. (Index 32 is the 33rd character.)
			passwordIndexofError = 32;
			err.append("Password must not exceed 32 characters (found ").append(len).append(").\n");
			err.append("Delete characters beginning at the arrow to meet the limit.\n");
		}

		// 2) Too short: point at the end of the string since we need more characters
		if (!foundLongEnough) {
			// If no pointer has been set yet, point just past the last character
			if (passwordIndexofError == -1) passwordIndexofError = len;
			err.append("Password must be at least 8 characters long (found ").append(len).append(").\n");
		}

		// 3) Missing the required character classes
		if (!foundUpperCase) {
			if (passwordIndexofError == -1) passwordIndexofError = 0; // Beginning is a reasonable cue
			err.append("Password must include at least one uppercase letter.\n");
		}
		if (!foundLowerCase) {
			if (passwordIndexofError == -1) passwordIndexofError = 0;
			err.append("Password must include at least one lowercase letter.\n");
		}
		if (!foundNumericDigit) {
			if (passwordIndexofError == -1) passwordIndexofError = 0;
			err.append("Password must include at least one digit.\n");
		}
		if (!foundSpecialChar) {
			if (passwordIndexofError == -1) passwordIndexofError = 0;
			err.append("Password must include at least one special character.\n");
		}

		/* If we collected any errors, store and return them. Otherwise, success. */
		if (err.length() > 0) {
			passwordErrorMessage = err.toString();
			// Console trace to align with the testing harness output
			displayInputState();
			return passwordErrorMessage;
		}

		// No errors — accept the password
		return "";
	}
}