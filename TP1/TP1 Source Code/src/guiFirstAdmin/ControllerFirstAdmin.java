package guiFirstAdmin;

import java.sql.SQLException;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;
import javafx.scene.paint.Color; // for message color

// Username & Password recognizers (your existing classes)
import userNameRecognizerTestbed.UserNameRecognizer;
import passwordPopUpWindow.Model;

/*******
 * <p> Title: ControllerFirstAdmin Class </p>
 *
 * <p> Description: Controller for the First Admin setup page. This controller
 * collects the current values from the View, validates them using the FSM-based
 * recognizers, drives the status message (single line), and enables/disables
 * the Setup button accordingly. Empty string from a recognizer means “valid”. </p>
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 *
 * @version 1.01    2025-09-09 Add live validation (username/password + match),
 *                              button enable/disable, and helpful messages.
 */
public class ControllerFirstAdmin {

    /*-********************************************************************************************
     * Controller attributes for this page (kept as simple cached fields)
     **********************************************************************************************/
    private static String adminUsername = "";
    private static String adminPassword1 = "";
    private static String adminPassword2 = "";

    // Database is provided by the application’s singleton
    protected static Database theDatabase = applicationMain.FoundationsMain.database;

    /*-********************************************************************************************
     * UI actions (called by the View’s text listeners and buttons)
     **********************************************************************************************/

    /**********
     * <p> Method: setAdminUsername() </p>
     *
     * <p> Description: Called when the username field changes. A private local copy is saved
     * and the form is validated to provide immediate feedback and toggle the button. </p>
     */
    protected static void setAdminUsername() {
        adminUsername = ViewFirstAdmin.text_AdminUsername.getText();
        validateForm();
    }

    /**********
     * <p> Method: setAdminPassword1() </p>
     *
     * <p> Description: Called when the first password field changes. Saves a local copy and
     * re-validates. </p>
     */
    protected static void setAdminPassword1() {
        adminPassword1 = ViewFirstAdmin.text_AdminPassword1.getText();
        validateForm();
    }

    /**********
     * <p> Method: setAdminPassword2() </p>
     *
     * <p> Description: Called when the second password field changes. Saves a local copy and
     * re-validates. </p>
     */
    protected static void setAdminPassword2() {
        adminPassword2 = ViewFirstAdmin.text_AdminPassword2.getText();
        validateForm();
    }

    /**********
     * <p> Method: doSetupAdmin(Stage, int) </p>
     *
     * <p> Description: Called when the user presses “Setup Admin Account”.
     * Defensively validates again; if valid, creates the admin user and navigates. </p>
     */
    protected static void doSetupAdmin(Stage ps, int r) {

        // Defensive validation (button should already be enabled only when valid)
        if (!validateForm()) return;

        // Create the user and store in DB
        User user = new User(adminUsername, adminPassword1, "", "", "", "", "", true, false, false);

        try {
            theDatabase.register(user);
        } catch (SQLException e) {
            System.err.println("*** ERROR *** Database error trying to register a user: " + e.getMessage());
            e.printStackTrace();
            // Show a friendly message in the same status label
            ViewFirstAdmin.showFormMessage("Database error while creating the admin user.\nPlease try again.", true);
            return;
        }

        // Success → move to User Update page
        guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewFirstAdmin.theStage, user);
    }

    /**********
     * <p> Method: performQuit() </p>
     *
     * <p> Description: Terminates the application. </p>
     */
    protected static void performQuit() {
        System.out.println("Perform Quit");
        System.exit(0);
    }

    /*-********************************************************************************************
     * Private helpers
     **********************************************************************************************/

    /**
     * Validate the three inputs using the FSM recognizers and simple equality check.
     * Drives the single-line message and toggles the button. Returns true iff valid.
     */
    private static boolean validateForm() {
        // Username validator (empty string means OK)
        String uErr = UserNameRecognizer.checkForValidUserName(adminUsername);
        if (!uErr.isEmpty()) {
            ViewFirstAdmin.showFormMessage(uErr, true);
            ViewFirstAdmin.setAdminSetupEnabled(false);
            return false;
        }

        // Password validator (8–32, at least one of each class; empty string means OK)
        String pErr = Model.evaluatePassword(adminPassword1);
        if (!pErr.isEmpty()) {
            ViewFirstAdmin.showFormMessage(pErr, true);
            ViewFirstAdmin.setAdminSetupEnabled(false);
            return false;
        }

        // Confirm match (only after base password is valid to avoid noisy messages)
        if (adminPassword1 == null || !adminPassword1.equals(adminPassword2)) {
            ViewFirstAdmin.showFormMessage("The two passwords must match. Please try again!\n", true);
            ViewFirstAdmin.setAdminSetupEnabled(false);
            return false;
        }

        // Everything looks good
        ViewFirstAdmin.showFormMessage("Success! Username and password are valid.", false);
        ViewFirstAdmin.setAdminSetupEnabled(true);
        return true;
    }
}
