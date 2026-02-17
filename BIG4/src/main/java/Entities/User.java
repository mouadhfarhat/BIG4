package Entities;

/**
 * Represents an authenticated user with a role: DELIVERY_MAN, ADMIN, or CLIENT.
 * referenceId links to delivery_man_id when role is DELIVERY_MAN.
 */
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String role;       // DELIVERY_MAN, ADMIN, CLIENT
    private Long referenceId;  // delivery_man_id when role is DELIVERY_MAN, null otherwise
    private String fullName;
    private String phone;
    private String address;  // delivery address for client; optional for others

    public User() {
    }

    public User(String email, String role) {
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDeliveryMan() {
        return "DELIVERY_MAN".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isClient() {
        return "CLIENT".equals(role);
    }
}
