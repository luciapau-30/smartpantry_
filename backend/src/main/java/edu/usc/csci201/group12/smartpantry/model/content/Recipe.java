package edu.usc.csci201.group12.smartpantry.model.content;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recipe extends Post {
    public static class RecipeIngredient {
        private String id;
        private String ingredientId;
        private String ingredientName;
        private BigDecimal quantity;
        private String unit;
        private String notes;
        private boolean optional;

        public RecipeIngredient(String ingredientId, BigDecimal quantity, String unit) {
            this(UUID.randomUUID().toString(), ingredientId, null, quantity, unit, "", false);
        }

        public RecipeIngredient(
                String id,
                String ingredientId,
                String ingredientName,
                BigDecimal quantity,
                String unit,
                String notes,
                boolean optional) {
            setId(id);
            setIngredientId(ingredientId);
            setIngredientName(ingredientName);
            setQuantity(quantity);
            setUnit(unit);
            setNotes(notes);
            setOptional(optional);
        }

        public String getId() {
            return id;
        }

        public final void setId(String id) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("ingredient id cannot be blank");
            }
            this.id = id.trim();
        }

        public String getIngredientId() {
            return ingredientId;
        }

        public final void setIngredientId(String ingredientId) {
            if (ingredientId == null || ingredientId.isBlank()) {
                throw new IllegalArgumentException("ingredientId cannot be blank");
            }
            this.ingredientId = ingredientId.trim();
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public final void setIngredientName(String ingredientName) {
            this.ingredientName = ingredientName == null ? null : ingredientName.trim();
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public final void setQuantity(BigDecimal quantity) {
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("quantity must be non-negative");
            }
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public final void setUnit(String unit) {
            this.unit = unit == null ? "" : unit.trim();
        }

        public String getNotes() {
            return notes;
        }

        public final void setNotes(String notes) {
            this.notes = notes == null ? "" : notes.trim();
        }

        public boolean isOptional() {
            return optional;
        }

        public final void setOptional(boolean optional) {
            this.optional = optional;
        }
    }

    private String title;
    private String description;
    private String imageUrl;
    private int prepTimeMinutes;
    private int cookTimeMinutes;
    private int servings;
    private boolean isPublic;
    private String cuisineType;
    private List<RecipeIngredient> ingredientsList;
    private List<String> instructions;

    public Recipe(String authorId, String title, List<RecipeIngredient> ingredientsList, List<String> instructions, String cuisineType) {
        super(authorId);
        setTitle(title);
        setDescription("");
        setImageUrl("");
        setPrepTimeMinutes(0);
        setCookTimeMinutes(0);
        setServings(1);
        setPublic(true);
        setCuisineType(cuisineType);
        setIngredientsList(ingredientsList);
        setInstructions(instructions);
    }

    public Recipe(
            String id,
            String authorId,
            Instant createdAt,
            Instant updatedAt,
            String title,
            String description,
            String imageUrl,
            int prepTimeMinutes,
            int cookTimeMinutes,
            int servings,
            boolean isPublic,
            String cuisineType,
            List<RecipeIngredient> ingredientsList,
            List<String> instructions) {
        super(id, authorId, createdAt, updatedAt);
        setTitle(title);
        setDescription(description);
        setImageUrl(imageUrl);
        setPrepTimeMinutes(prepTimeMinutes);
        setCookTimeMinutes(cookTimeMinutes);
        setServings(servings);
        setPublic(isPublic);
        setCuisineType(cuisineType);
        setIngredientsList(ingredientsList);
        setInstructions(instructions);
    }

    public String getTitle() {
        return title;
    }

    public final void setTitle(String title) {
        this.title = requireNonBlank(title, "title");
    }

    public String getDescription() {
        return description;
    }

    public final void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public final void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
    }

    public int getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public final void setPrepTimeMinutes(int prepTimeMinutes) {
        if (prepTimeMinutes < 0) {
            throw new IllegalArgumentException("prepTimeMinutes cannot be negative");
        }
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public int getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public final void setCookTimeMinutes(int cookTimeMinutes) {
        if (cookTimeMinutes < 0) {
            throw new IllegalArgumentException("cookTimeMinutes cannot be negative");
        }
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public int getServings() {
        return servings;
    }

    public final void setServings(int servings) {
        if (servings <= 0) {
            throw new IllegalArgumentException("servings must be positive");
        }
        this.servings = servings;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public final void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public final void setCuisineType(String cuisineType) {
        this.cuisineType = requireNonBlank(cuisineType, "cuisineType");
    }

    public List<RecipeIngredient> getIngredientsList() {
        return new ArrayList<>(ingredientsList);
    }

    public final void setIngredientsList(List<RecipeIngredient> ingredientsList) {
        if (ingredientsList == null || ingredientsList.isEmpty()) {
            throw new IllegalArgumentException("ingredientsList cannot be empty");
        }
        this.ingredientsList = new ArrayList<>(ingredientsList);
    }

    public List<String> getInstructions() {
        return new ArrayList<>(instructions);
    }

    public final void setInstructions(List<String> instructions) {
        if (instructions == null || instructions.stream().allMatch(step -> step == null || step.isBlank())) {
            throw new IllegalArgumentException("instructions cannot be empty");
        }
        this.instructions = new ArrayList<>();
        for (String step : instructions) {
            if (step != null && !step.isBlank()) {
                this.instructions.add(step.trim());
            }
        }
    }

    public void addIngredient(RecipeIngredient ingredientLine) {
        this.ingredientsList.add(ingredientLine);
    }

    public void addInstruction(String instruction) {
        if (instruction != null && !instruction.isBlank()) {
            this.instructions.add(instruction.trim());
        }
    }
}
