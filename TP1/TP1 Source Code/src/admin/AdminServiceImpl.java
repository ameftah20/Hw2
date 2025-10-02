package admin;

import java.util.*;
import applicationMain.FoundationsMain;
import database.Database;

public class AdminServiceImpl implements AdminService {
    private final Database db;
    private final Map<String, UserSummary> users = new LinkedHashMap<>();

    
    //Sets up the admin service page
    public AdminServiceImpl() {
        this.db = FoundationsMain.database;
        List<String> userNames = db.getUserList(); //Get the list of all user names 
        for (int i = 0; i < userNames.size(); i ++) 
        {
        	String username = userNames.get(i);
        	String prefFirst = db.getPreferredFirstName(username); //Get Pref First Name
        	UserSummary newSummary = new UserSummary(
        			username, 
        			(prefFirst == "") ? db.getFirstName(username) : prefFirst, //If the user hasn't set a pref. First name then attempt to use norm first name.
        			db.getRoleByUser(username),
        			db.getActiveStatus(username));
        	users.put(username, newSummary);
        }
    }

    //Lists all of the current users in the database
    @Override
    public List<UserSummary> listUsers(Optional<Role> roleFilter, Optional<Boolean> activeOnly) {
        List<UserSummary> out = new ArrayList<>(users.values());
        roleFilter.ifPresent(r -> out.removeIf(u -> u.getRole() != r));
        activeOnly.ifPresent(a -> { if (a) out.removeIf(u -> !u.isActive()); });
        return out;
    }

    
    //Sets a user active
    @Override
    public boolean setActive(String userId, boolean active) {
        UserSummary u = users.get(userId);
        if (u == null) return false;
        if (!active && isLastActiveAdmin(userId)) return false; // protect last active ADMIN
        users.put(userId, new UserSummary(u.getUserId(), u.getDisplayName(), u.getRole(), active)); //Update List
        db.updateActiveStatus(u.getUserId(), active); //Update Database
        return true;
    }

    
    //Changes a current users role
    @Override
    public boolean changeRole(String userId, Role newRole) {
        UserSummary u = users.get(userId);
        Role currRole = u.getRole();
        if (currRole == Role.ADMIN && newRole != Role.ADMIN && countActiveAdmins() == 1) return false;
        db.updateUserRole(userId, currRole, "FALSE"); //Remove Current Role
        db.updateUserRole(userId, newRole, "TRUE"); //Update Role to New One
        users.put(userId, new UserSummary(u.getUserId(), u.getDisplayName(), newRole, u.isActive()));
        return true;
    }

    
    //Resets a password and generates a new one
    @Override
    public String resetPassword(String userId) {
        if (!users.containsKey(userId)) return null;   //If the user does not exist return nothing
        String newPass = PasswordUtil.generateStrongTemp(12); //Generate a random strong password
        db.updatePassword(userId, newPass); //Update the users password
        db.updateUserPasswordReset(userId, true);
        return newPass;    //Return the new password 
    }

    /* Helpers */

    //Counts the currently active admins
    private int countActiveAdmins() {
        int n = 0;
        for (UserSummary u : users.values()) {
            if (u.isActive() && u.getRole() == Role.ADMIN) n++;
        }
        return n;
    }

    //Determines if the last admin is active
    private boolean isLastActiveAdmin(String userId) {
        UserSummary u = users.get(userId);
        if (u == null) return false;
        if (u.getRole() != Role.ADMIN || !u.isActive()) return false;
        return countActiveAdmins() == 1;
    }
}
