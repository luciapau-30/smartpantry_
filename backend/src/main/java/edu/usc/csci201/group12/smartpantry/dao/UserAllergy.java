// PORTED (zeqiang/database): DB row type for the USER_ALLERGIES table. No name collision.
package edu.usc.csci201.group12.smartpantry.dao;

import java.sql.Timestamp;

public class UserAllergy {
    private String id;
    private String userId;
    private String allergenName;
    private String severity;
    private Timestamp createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAllergenName() { return allergenName; }
    public void setAllergenName(String allergenName) { this.allergenName = allergenName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
