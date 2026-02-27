package bizstock.service;

import bizstock.dao.InventoryMovementDAO;
import bizstock.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InventoryService {

  private final InventoryMovementDAO movementDAO = new InventoryMovementDAO();

  public void registerIn(int productId, int qty, int userId, String note) throws Exception {
    if (qty <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");

    try (Connection cn = DatabaseConnection.getConnection()) {
      cn.setAutoCommit(false);

      try {
        int currentQty = getProductQtyForUpdate(cn, productId);
        int newQty = currentQty + qty;

        updateProductQty(cn, productId, newQty);
        movementDAO.insert(cn, productId, userId, "IN", qty, note);

        cn.commit();
      } catch (Exception ex) {
        cn.rollback();
        throw ex;
      } finally {
        cn.setAutoCommit(true);
      }
    }
  }

  public void registerOut(int productId, int qty, int userId, String note) throws Exception {
    if (qty <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");

    try (Connection cn = DatabaseConnection.getConnection()) {
      cn.setAutoCommit(false);

      try {
        int currentQty = getProductQtyForUpdate(cn, productId);
        if (currentQty - qty < 0) {
          throw new IllegalArgumentException("No puedes sacar mas de lo disponible. Disponible: " + currentQty);
        }

        int newQty = currentQty - qty;

        updateProductQty(cn, productId, newQty);
        movementDAO.insert(cn, productId, userId, "OUT", qty, note);

        cn.commit();
      } catch (Exception ex) {
        cn.rollback();
        throw ex;
      } finally {
        cn.setAutoCommit(true);
      }
    }
  }

  public int getCurrentQty(int productId) throws Exception {
    String sql = "SELECT quantity FROM product WHERE id = ? AND is_active = 1";
    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setInt(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new IllegalArgumentException("Producto no existe o esta inactivo.");
        return rs.getInt("quantity");
      }
    }
  }

  private int getProductQtyForUpdate(Connection cn, int productId) throws Exception {
    String sql = "SELECT quantity FROM product WHERE id = ? AND is_active = 1 FOR UPDATE";

    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setInt(1, productId);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new IllegalArgumentException("Producto no existe o esta inactivo.");
        return rs.getInt("quantity");
      }
    }
  }

  private void updateProductQty(Connection cn, int productId, int newQty) throws Exception {
    String sql = "UPDATE product SET quantity = ? WHERE id = ? AND is_active = 1";

    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setInt(1, newQty);
      ps.setInt(2, productId);
      int updated = ps.executeUpdate();
      if (updated != 1) throw new IllegalStateException("No se pudo actualizar la cantidad del producto.");
    }
  }
}
