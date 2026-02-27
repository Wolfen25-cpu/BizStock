package bizstock.model;

import java.time.LocalDateTime;

public class InventoryMovement {
  private int id;
  private int productId;
  private int userId;
  private String movementType;
  private int quantity;
  private String note;
  private LocalDateTime createdAt;

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public int getProductId() { return productId; }
  public void setProductId(int productId) { this.productId = productId; }

  public int getUserId() { return userId; }
  public void setUserId(int userId) { this.userId = userId; }

  public String getMovementType() { return movementType; }
  public void setMovementType(String movementType) { this.movementType = movementType; }

  public int getQuantity() { return quantity; }
  public void setQuantity(int quantity) { this.quantity = quantity; }

  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
