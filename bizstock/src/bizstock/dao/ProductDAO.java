package bizstock.dao;

import bizstock.model.Product;
import bizstock.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

  public List<Product> findAllActive() throws SQLException {
    String sql = """
      SELECT id, name, description, price, quantity, reorder_level, critical_level,
             category_id, brand_id, is_active
      FROM product
      WHERE is_active = 1
      ORDER BY name
    """;

    List<Product> list = new ArrayList<>();

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        list.add(map(rs));
      }
    }
    return list;
  }

  public List<Product> findCriticalAlerts() throws SQLException {
    String sql = """
      SELECT id, name, description, price, quantity, reorder_level, critical_level,
             category_id, brand_id, is_active
      FROM product
      WHERE is_active = 1
        AND quantity <= critical_level
      ORDER BY quantity ASC, name
    """;

    List<Product> list = new ArrayList<>();

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        list.add(map(rs));
      }
    }
    return list;
  }

  public List<Product> findLowAlerts() throws SQLException {
    String sql = """
      SELECT id, name, description, price, quantity, reorder_level, critical_level,
             category_id, brand_id, is_active
      FROM product
      WHERE is_active = 1
        AND quantity <= reorder_level
        AND quantity > critical_level
      ORDER BY quantity ASC, name
    """;

    List<Product> list = new ArrayList<>();

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        list.add(map(rs));
      }
    }
    return list;
  }

  public int insert(Product p) throws SQLException {
    String sql = """
      INSERT INTO product
      (name, description, price, quantity, reorder_level, critical_level, category_id, brand_id, is_active)
      VALUES
      (?, ?, ?, ?, ?, ?, ?, ?, 1)
    """;

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      ps.setString(1, p.getName());
      ps.setString(2, p.getDescription());
      ps.setBigDecimal(3, p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO);
      ps.setInt(4, p.getQuantity());
      ps.setInt(5, p.getReorderLevel());
      ps.setInt(6, p.getCriticalLevel());
      ps.setInt(7, p.getCategoryId());
      ps.setInt(8, p.getBrandId());

      ps.executeUpdate();

      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getInt(1);
      }
    }
    return 0;
  }

  public boolean update(Product p) throws SQLException {
    String sql = """
      UPDATE product
      SET name = ?, description = ?, price = ?, quantity = ?, reorder_level = ?, critical_level = ?,
          category_id = ?, brand_id = ?
      WHERE id = ? AND is_active = 1
    """;

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setString(1, p.getName());
      ps.setString(2, p.getDescription());
      ps.setBigDecimal(3, p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO);
      ps.setInt(4, p.getQuantity());
      ps.setInt(5, p.getReorderLevel());
      ps.setInt(6, p.getCriticalLevel());
      ps.setInt(7, p.getCategoryId());
      ps.setInt(8, p.getBrandId());
      ps.setInt(9, p.getId());

      return ps.executeUpdate() == 1;
    }
  }

  public boolean softDelete(int id) throws SQLException {
    String sql = "UPDATE product SET is_active = 0 WHERE id = ? AND is_active = 1";

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() == 1;
    }
  }

  private Product map(ResultSet rs) throws SQLException {
    Product p = new Product();
    p.setId(rs.getInt("id"));
    p.setName(rs.getString("name"));
    p.setDescription(rs.getString("description"));
    p.setPrice(rs.getBigDecimal("price"));
    p.setQuantity(rs.getInt("quantity"));
    p.setReorderLevel(rs.getInt("reorder_level"));
    p.setCriticalLevel(rs.getInt("critical_level"));
    p.setCategoryId(rs.getInt("category_id"));
    p.setBrandId(rs.getInt("brand_id"));
    p.setActive(rs.getInt("is_active") == 1);
    return p;
  }
}
