package bizstock.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SecurityUtil {

  private SecurityUtil() {}

  public static String sha256Hex(String input) {
    if (input == null) input = "";

    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return toHex(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 no disponible", e);
    }
  }

  private static String toHex(byte[] data) {
    StringBuilder sb = new StringBuilder(data.length * 2);
    for (byte b : data) {
      String hex = Integer.toHexString(b & 0xff);
      if (hex.length() == 1) sb.append('0');
      sb.append(hex);
    }
    return sb.toString();
  }
}
