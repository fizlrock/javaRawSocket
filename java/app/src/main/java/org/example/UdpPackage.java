package org.example;

import static java.util.Arrays.copyOfRange;

import java.util.HexFormat;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UdpPackage implements Bytes {
  // final String srcMAC, dstMAC, type;
  final int srcPort, dstPort;
  @ToString.Exclude
  IpPackage ip;
  @ToString.Exclude
  final byte[] payload;

  public UdpPackage(IpPackage ip) {
    this.ip = ip;
    srcPort = Integer.parseInt(copyAndToHex(ip.payload, 0, 2), 16);
    dstPort = Integer.parseInt(copyAndToHex(ip.payload, 2, 4), 16);
    payload = copyOfRange(ip.payload, 8, ip.payload.length);
  }

  static String copyAndToHex(byte[] data, int s, int e) {

    return HexFormat.of().formatHex(copyOfRange(data, s, e));
  }

  public UdpPackage(int srcPort, int dstPort, byte[] payload) {
    this.srcPort = srcPort;
    this.dstPort = dstPort;
    this.payload = payload;
  }

  @Override
  public byte[] toBytes() {
    byte[] result = new byte[8 + payload.length];

    for (int i = 0; i < payload.length; i++)
      result[i + 8] = payload[i];

    // ports
    result[0] = (byte) (srcPort& 0xFF);
    result[1] = (byte) ((srcPort>> 8) & 0xFF);
    result[2] = (byte) (dstPort& 0xFF);
    result[3] = (byte) ((dstPort>> 8) & 0xFF);

    // datagram length
    result[4] = (byte) ((result.length>> 8) & 0xFF);
    result[5] = (byte) ((result.length>> 8) & 0xFF);

    // Checksum (Optional)
    result[6] = 0x00;
    result[7] = 0x00;
    

    return result;
  }
}
