package edu.usc.csci201.group12.smartpantry.api.member;

public record PostCommentRequest(String recipeId, String textBody, String parentCommentId) {
}
