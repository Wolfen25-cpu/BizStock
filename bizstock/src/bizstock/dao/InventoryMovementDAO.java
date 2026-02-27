package bizstock.dao;

import bizstock.model.InventoryMovement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryMovementDAO {

  public void insert(Connection cn, int productId, int userId, String type, int qty, String note) throws SQLException {
    String sql = """
      INSERT INTO inventory_movement (product_id, user_id, movement_type, quantity, note)
      VALUES (?, ?, ?, ?, ?)
    """;

    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setInt(1, productId);
      ps.setInt(2, userId);
      ps.setString(3, type);
      ps.setInt(4, qty);
      ps.setString(5, note);
      ps.executeUpdate();
    }
  }

  public List<InventoryMovement> findByProduct(int productId, int limit) throws SQLException {
    String sql = """
      SELECT id, product_id, user_id, movement_type, quantity, note, created_at
      FROM inventory_movement
      WHERE product_id = ?
      ORDER BY created_at DESC, id DESC
      LIMIT ?
    """;

    List<InventoryMovement> list = new ArrayList<>();

    try (Connection cn = bizstock.util.DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setInt(1, productId);
      ps.setInt(2, limit);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          InventoryMovement m = new InventoryMovement();
          m.setId(rs.getInt("id"));
          m.setProductId(rs.getInt("product_id"));
          m.setUserId(rs.getInt("user_id"));
          m.setMovementType(rs.getString("movement_type"));
          m.setQuantity(rs.getInt("quantity"));
          m.setNote(rs.getString("note"));
          if (rs.getTimestamp("created_at") != null) {
            m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
          }
          list.add(m);
        }
      }
    }

    return list;
  }
}
