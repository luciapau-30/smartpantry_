// PORTED (zeqiang/database): DB row type for the INGREDIENTS table. No name collision.
package edu.usc.csci201.group12.smartpantry.dao;

public class Ingredient {
    private String id;
    private String name;
    private String category;
    private String defaultUnit;
    private String imageUrl;

    public Ingredient() {}

    public Ingredient(String id, String name, String category, String defaultUnit, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.defaultUnit = defaultUnit;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String defaultUnit) { this.defaultUnit = defaultUnit; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
