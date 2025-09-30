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
        	UserSummary newSummary = new UserSummary(
        			username, 
        			db.getPreferredFirstName(username),
        			Role.ADMIN,  //TO DO: The database stores roles strangely find a way to implement this
        			true);
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
        users.put(userId, new UserSummary(u.getUserId(), u.getDisplayName(), u.getRole(), active));
        return true;
    }

    
    //Changes a current users role
    @Override
    public boolean changeRole(String userId, Role newRole) {
        UserSummary u = users.get(userId);
        if (u == null) return false;
        if (u.getRole() == Role.ADMIN && newRole != Role.ADMIN && countActiveAdmins() == 1) return false;
        users.put(userId, new UserSummary(u.getUserId(), u.getDisplayName(), newRole, u.isActive()));
        
        //TO DO: When you change a users role it should update it in the db
        return true;
    }

    
    //Resets a password and generates a new one
    @Override
    public String resetPassword(String userId) {
        if (!users.containsKey(userId)) return null;
        return PasswordUtil.generateStrongTemp(12);
        
        //TO DO: When you update a password it should also update it in the database
    }

    /* Helpers */

    //Test function to populate the list
    private void seedData() {
        add(new UserSummary("admin@demo", "Admin User", Role.ADMIN, true));
        add(new UserSummary("alex@demo",  "Alex",       Role.USER,  true));
        add(new UserSummary("ta@demo",    "TA Person",  Role.TA,    false));
    }

    //Adds a user given the user summary
    private void add(UserSummary u) {
        users.put(u.getUserId(), u);
    }

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
