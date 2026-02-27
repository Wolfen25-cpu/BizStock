package bizstock.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

  private static final String URL =
    "jdbc:mysql://localhost:3306/bizstock?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASS = "Acf121203";

  private DatabaseConnection() {}

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASS);
  }
}
