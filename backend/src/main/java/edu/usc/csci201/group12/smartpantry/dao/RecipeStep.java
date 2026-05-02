// PORTED (zeqiang/database): DB row type for the RECIPE_STEPS table. No name collision.
package edu.usc.csci201.group12.smartpantry.dao;

public class RecipeStep {
    private String id;
    private String recipeId;
    private int stepNumber;
    private String instruction;
    private Integer timerSeconds;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }

    public Integer getTimerSeconds() { return timerSeconds; }
    public void setTimerSeconds(Integer timerSeconds) { this.timerSeconds = timerSeconds; }
}
