import java.util.HexFormat;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.Arrays.copyOfRange;

import java.awt.Rectangle;

import static java.lang.String.format;

public class Main {

  public static void main(String[] args) {
    if (init("lo"))
      System.out.println("Socket created");
    else
      return;

    Stream.generate(Main::recvFrame)
        .map(HexFormat.of()::formatHex)
        .limit(3)
        .forEach(System.out::println);
  }

  static {
    System.load("/home/fizlrock/code2/university/interfaces/lab34/clib/build/nativeLib.o");
  }

  public static native boolean init(String interfaceName);

  public static native void deinit();

  public static native int sendTo(byte[] buf);

  /**
   * Принять первый пакет с совпадающим фрагментом по смещению.
   * 
   * @param buf
   * @param offset
   * @return
   */
  public static native byte[] recvFrame();

  static class EthernetPackage {

    public byte[] src, dst, type;

    public EthernetPackage(byte[] data) {
      src = copyOfRange(data, 0, 7);
      dst = copyOfRange(data, 7, 14);
      System.out.println();
      System.out.println(HexFormat.of().formatHex(dst));
    }

    @Override
    public String toString() {

      StringJoiner sj = new StringJoiner("\n");

      String srcAddr, dstAddr, typeF;
      srcAddr = HexFormat.of().formatHex(src);
      dstAddr = HexFormat.of().formatHex(dst);

      sj.add(format("src", dstAddr));
      sj.add(format("dst", dstAddr));

      return sj.toString();

    }

  }
}
