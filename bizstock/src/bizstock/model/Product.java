package bizstock.model;

import java.math.BigDecimal;

public class Product {
  private int id;
  private String name;
  private String description;
  private BigDecimal price;
  private int quantity;
  private int reorderLevel;
  private int criticalLevel;
  private int categoryId;
  private int brandId;
  private boolean active;

  public Product() {}

  public Product(int id, String name, String description, BigDecimal price, int quantity,
                 int reorderLevel, int criticalLevel, int categoryId, int brandId, boolean active) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.quantity = quantity;
    this.reorderLevel = reorderLevel;
    this.criticalLevel = criticalLevel;
    this.categoryId = categoryId;
    this.brandId = brandId;
    this.active = active;
  }

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }

  public int getQuantity() { return quantity; }
  public void setQuantity(int quantity) { this.quantity = quantity; }

  public int getReorderLevel() { return reorderLevel; }
  public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

  public int getCriticalLevel() { return criticalLevel; }
  public void setCriticalLevel(int criticalLevel) { this.criticalLevel = criticalLevel; }

  public int getCategoryId() { return categoryId; }
  public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

  public int getBrandId() { return brandId; }
  public void setBrandId(int brandId) { this.brandId = brandId; }

  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
