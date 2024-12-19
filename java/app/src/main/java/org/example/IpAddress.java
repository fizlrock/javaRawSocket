package org.example;

/**
 * IpAddress
 */
public class IpAddress implements Bytes {

  public static IpAddress build(int a, int b, int c, int d) {
    return new IpAddress(a, b, c, d);
  }

  public static IpAddress build(String addr) {
    String[] words = addr.split("\\.");
    if (words.length != 4)
      throw new IllegalArgumentException("Не допустимый формат IP: " + addr);

    int a = Integer.parseInt(words[0]);
    int b = Integer.parseInt(words[1]);
    int c = Integer.parseInt(words[2]);
    int d = Integer.parseInt(words[3]);
    return new IpAddress(a, b, c, d);

  }

  public static IpAddress build(byte[] bytes) {
    return new IpAddress(bytes);
  }

  /**
   * Приведение к беззнакову 8 битному числу
   * 
   * @param a 0-255
   */
  private static byte mapToU8(int a) {
    if (a >= 0 && a < 256)
      return (byte) a;
    else
      throw new IllegalArgumentException("Illegal Ip addr octet: " + a);
  }

  byte[] addr = new byte[4];

  @SuppressWarnings("unused")
  private IpAddress(int a, int b, int c, int d) {

    addr[0] = mapToU8(a);
    addr[1] = mapToU8(b);
    addr[2] = mapToU8(c);
    addr[3] = mapToU8(d);
  }

  @SuppressWarnings("unused")
  private IpAddress(byte[] bytes) {
    if (bytes.length != 4)
      throw new IllegalArgumentException("Illegal IP addr size");
    addr = bytes;
  }

  @Override
  public String toString() {
    return String.format("%d.%d.%d.%d", addr[0], addr[1], addr[2], addr[3]);
  }

  @Override
  public byte[] toBytes() {
    return addr;
  }

}
