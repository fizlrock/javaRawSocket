package org.example;

/**
 * Обертка для нативной библиотеки 'FNETLib'
 * FNETLib
 */
public class FNETLib {

  static {
    System.load("/home/fizlrock/code2/university/interfaces/lab34/clib/build/nativeLib.o");
  }
  public static native boolean init(String interfaceName);
  public static native void deinit();
  public static native int sendTo(byte[] buf);
  public static native byte[] recvFrame();
}



