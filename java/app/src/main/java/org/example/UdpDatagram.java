package org.example;

import static java.util.Arrays.copyOfRange;

import java.util.HexFormat;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UdpDatagram implements Bytes {
  // final String srcMAC, dstMAC, type;
  final int srcPort, dstPort;
  long checksum = 0;

  @ToString.Exclude
  IpPackage ip;

  @ToString.Exclude
  final byte[] payload;

  /**
   * Parse UpdDatagram from IP package
   * 
   * @param ip
   */
  public UdpDatagram(IpPackage ip) {
    this.ip = ip;
    srcPort = Integer.parseInt(copyAndToHex(ip.payload, 0, 2), 16);
    dstPort = Integer.parseInt(copyAndToHex(ip.payload, 2, 4), 16);
    payload = copyOfRange(ip.payload, 8, ip.payload.length);
  }

  private static String copyAndToHex(byte[] data, int s, int e) {
    return HexFormat.of().formatHex(copyOfRange(data, s, e));
  }

  public UdpDatagram(int srcPort, int dstPort, IpAddress srcIp, IpAddress dstIp, byte[] payload) {
    // check package size TODO
    // check ports values
    this.srcPort = srcPort;
    this.dstPort = dstPort;
    this.payload = payload;
    calculateCheckSum(srcIp, dstIp);
  }

  public void calculateCheckSum(IpAddress srcIpObj, IpAddress dstIpObj) {
    byte[] header = new byte[12];

    byte[] srcIp = srcIpObj.toBytes();
    byte[] dstIp = dstIpObj.toBytes();

    for (int i = 0; i < 8; i++)
      if (i < 4)
        header[i] = srcIp[i];
      else
        header[i] = dstIp[i - 4];

    header[9] = 17;
    int datagram_length = payload.length + 8;
    header[10] = (byte) (datagram_length >> 8);
    header[11] = (byte) (datagram_length);

    checksum = calculateChecksum(header, this.toBytes());

    System.out.println("FUUUCK " + checksum);
  }

  public static int calculateChecksum(byte[] pseudoHeader, byte[] udpPacket) {
    int sum = 0;

    sum = addBytesToSum(sum, pseudoHeader);
    sum = addBytesToSum(sum, udpPacket);

    while ((sum >> 16) != 0) {
      sum = (sum & 0xFFFF) + (sum >> 16);
    }
    return ~sum & 0xFFFF;
  }

  private static int addBytesToSum(int sum, byte[] data) {
    for (int i = 0; i < data.length; i += 2) {
      int word = ((data[i] & 0xFF) << 8) + (i + 1 < data.length ? (data[i + 1] & 0xFF) : 0);
      sum += word;
    }
    return sum;
  }

  @Override
  public byte[] toBytes() {
    byte[] result = new byte[8 + payload.length];

    for (int i = 0; i < payload.length; i++)
      result[i + 8] = payload[i];

    // ports
    result[0] = (byte) (srcPort >> 8);
    result[1] = (byte) (srcPort);
    result[2] = (byte) (dstPort >> 8);
    result[3] = (byte) (dstPort);

    // datagram length
    result[4] = (byte) (result.length >> 8);
    result[5] = (byte) (result.length);

    // Checksum (Optional)
    result[6] = (byte) (checksum >> 8);
    result[7] = (byte) (checksum);

    return result;
  }
}
