package org.example;

import static java.util.Arrays.copyOfRange;

import java.util.HexFormat;


import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class EthernetFrame implements Bytes {

  final String srcMacHex, dstMacHex, typeHex;
  @ToString.Exclude
  final byte[] payload, srcMac, dstMac, type;

  public EthernetFrame(byte[] frame) {
    dstMac = copyOfRange(frame, 0, 6);
    srcMac = copyOfRange(frame, 6, 12);
    type = copyOfRange(frame, 12, 14);

    dstMacHex = copyAndToHex(frame, 0, 6);
    srcMacHex = copyAndToHex(frame, 6, 12);
    typeHex = copyAndToHex(frame, 12, 14);
    payload = copyOfRange(frame, 14, frame.length);
  }

  static String copyAndToHex(byte[] data, int s, int e) {

    return HexFormat.of().formatHex(copyOfRange(data, s, e));
  }



  public EthernetFrame(String macDst, String macSrc, IpPackage ip_package ){
      this(macDst, macSrc, "0800", ip_package.toBytes());
  }

  public EthernetFrame(String macDst, String macSrc, String type, byte[] payload) {
    this.srcMacHex = macSrc;
    this.dstMacHex = macDst;
    this.typeHex = type;
    this.payload = payload;

    srcMac = HexFormat.of().parseHex(macSrc);
    dstMac = HexFormat.of().parseHex(macDst);
    this.type = HexFormat.of().parseHex(type);
  }

  @Override
  public byte[] toBytes() {
    byte[] result = new byte[14 + payload.length];

    for (int i = 0; i < result.length; i++) {

      if (i < 6)
        // Copy dst mac
        result[i] = srcMac[i];
      else if (i < 12)
      // Copy src mac
        result[i] = dstMac[i - 6];
      else if (i < 14)
      // Copy nested type flag
        result[i] = type[i - 12];
      else
        result[i] = payload[i - 14];

    }

    return result;
  }

}
