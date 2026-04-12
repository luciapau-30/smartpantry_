package edu.usc.csci201.group12.smartpantry.api.auth;

/**
 * @param accountType {@code "guest"} (default) or {@code "member"} — must match team policy for who may register as member.
 */
public record RegisterRequest(String username, String email, String password, String accountType) {
    public RegisterRequest {
        if (accountType == null || accountType.isBlank()) {
            accountType = "guest";
        }
    }
}
