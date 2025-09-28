package admin;

import java.util.*;
import applicationMain.FoundationsMain;
import database.Database;

public class AdminServiceImpl implements AdminService {
    private final Database db;
    private final Map<String, UserSummary> users = new LinkedHashMap<>();

    public AdminServiceImpl() {
        this.db = FoundationsMain.database;
        seedData();
    }

    @Override
    public List<UserSummary> listUsers(Optional<Role> roleFilter, Optional<Boolean> activeOnly) {
        List<UserSummary> out = new ArrayList<>(users.values());
        roleFilter.ifPresent(r -> out.removeIf(u -> u.getRole() != r));
        activeOnly.ifPresent(a -> { if (a) out.removeIf(u -> !u.isActive()); });
        return out;
    }

    @Override
    public boolean setActive(String userId, boolean active) {
        UserSummary u = users.get(userId);
        if (u == null) return false;
        if (!active && isLastActiveAdmin(userId)) return false; // protect last active ADMIN
        users.put(userId, new UserSummary(u.getUserId(), u.getDisplayName(), u.getRole(), active));
        return true;
    }

    @Override
    public boolean changeRole(String userId, Role newRole) {
        UserSummary u = users.get(userId);
        if (u == null) return false;
        if (u.getRole() == Role.ADMIN && newRole != Role.ADMIN && countActiveAdmins() == 1) return false;
        users.put(userId, new UserSummary(u.getUserId(), u.getDisplayName(), newRole, u.isActive()));
        return true;
    }

    @Override
    public String resetPassword(String userId) {
        if (!users.containsKey(userId)) return null;
        return PasswordUtil.generateStrongTemp(12);
    }

    /* Helpers */

    private void seedData() {
        add(new UserSummary("admin@demo", "Admin User", Role.ADMIN, true));
        add(new UserSummary("alex@demo",  "Alex",       Role.USER,  true));
        add(new UserSummary("ta@demo",    "TA Person",  Role.TA,    false));
    }

    private void add(UserSummary u) {
        users.put(u.getUserId(), u);
    }

    private int countActiveAdmins() {
        int n = 0;
        for (UserSummary u : users.values()) {
            if (u.isActive() && u.getRole() == Role.ADMIN) n++;
        }
        return n;
    }

    private boolean isLastActiveAdmin(String userId) {
        UserSummary u = users.get(userId);
        if (u == null) return false;
        if (u.getRole() != Role.ADMIN || !u.isActive()) return false;
        return countActiveAdmins() == 1;
    }
}
