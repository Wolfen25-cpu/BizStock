package bizstock.dao;

import bizstock.model.User;
import bizstock.util.DatabaseConnection;
import bizstock.util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

  public User authenticate(String username, String plainPassword) throws Exception {
    if (username == null || username.trim().isEmpty()) return null;
    if (plainPassword == null) plainPassword = "";

    String sql = """
      SELECT id, username, password_hash, role, is_active
      FROM app_user
      WHERE username = ?
      LIMIT 1
    """;

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setString(1, username.trim());

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;

        int isActive = rs.getInt("is_active");
        if (isActive != 1) return null;

        String storedHash = rs.getString("password_hash");
        String inputHash = SecurityUtil.sha256Hex(plainPassword);

        if (storedHash == null || !storedHash.equalsIgnoreCase(inputHash)) return null;

        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(storedHash);
        u.setRole(rs.getString("role"));
        u.setActive(true);
        return u;
      }
    }
  }

  public int createUser(String username, String plainPassword, String role) throws Exception {
    String sql = """
      INSERT INTO app_user (username, password_hash, role, is_active)
      VALUES (?, ?, ?, 1)
    """;

    String hash = SecurityUtil.sha256Hex(plainPassword);

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

      ps.setString(1, username.trim());
      ps.setString(2, hash);
      ps.setString(3, role);

      ps.executeUpdate();

      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getInt(1);
      }
    }
    return 0;
  }
}
