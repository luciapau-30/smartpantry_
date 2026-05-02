// PORTED (zeqiang/database): Internal DTO representing one row from the USERS table.
// Package-private — only UserDao and JdbcUserStore need it.
// Renamed from Zeqiang's User POJO to avoid collision with model.User (Archit's abstract class).
package edu.usc.csci201.group12.smartpantry.dao;

import java.sql.Timestamp;

class UserRow {
    String id;
    String username;
    String email;
    String passwordHash;
    String profilePictureUrl;
    String bio;
    Timestamp createdAt;
    Timestamp lastLogin;
    boolean active;
    boolean guest;
}
