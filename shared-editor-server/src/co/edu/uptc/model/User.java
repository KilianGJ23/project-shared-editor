package co.edu.uptc.model;

public class User {
    private String username;
    private int colorId;
    private UserRole role;
    private long joinOrder;

    public User(String username, int colorId, UserRole role, long joinOrder) {
        this.username = username;
        this.colorId = colorId;
        this.role = role;
        this.joinOrder = joinOrder;
    }

    public String getUsername() {
        return this.username;
    }

    public int getColorId() {
        return this.colorId;
    }

    public UserRole getRole() {
        return this.role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public long getJoinOrder() {
        return this.joinOrder;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}
