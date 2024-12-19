package org.example;

import static java.util.Arrays.copyOfRange;

import java.util.concurrent.ThreadLocalRandom;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class IpPackage implements Bytes {
  enum IP_NESTED_PROTOCOLS {
    HOPOPT, ICMP, IGMP, GGP, IP_in_IP, ST, TCP, CBT, EGP,
    IGP, BBN_RCC_MON, NVP_II, PUP, ARGUS, EMCON, XNET, CHAOS, UDP, MUX, DCN_MEAS, HMP,
    PRM, XNS_IDP, TRUNK_1, TRUNK_2, LEAF_1, LEAF_2, RDP, IRTP, ISO_TP4, NETBLT, MFE_NSP,
    MERIT_INP, DCCP, PC3, IDPR, XTP, DDP, IDPR_CMTP, TPpp, IL, IPv6, SDRP, IPv6_Route,
    IPv6_Frag, IDRP, RSVP, GRE, MHRP, BNA, ESP, AH, I_NLSP, SWIPE, NARP, MOBILE, TLSP,
    SKIP, IPv6_ICMP, IPv6_NoNxt, IPv6_Opts, CFTP, SAT_EXPAK, KRYPTOLAN, RVD, IPPC, SAT_MON,
    VISA, IPCV, CPNX, CPHB, WSN, PVP, BR_SAT_MON, SUN_ND, WB_MON, WB_EXPAK, ISO_IP, VMTP,
    SECURE_VMTP, VINES, TTP, IPTM, NSFNET_IGP, DGP, TCF, EIGRP, OSPF, Sprite_RPC, LARP, MTP,
    AXdot25, OS, MICP, SCC_SP, ETHERIP, ENCAP, GMTP, IFMP, PNNI, PIM, ARIS, SCPS, AslashN,
    IPComp, SNP, Compaq_Peer, IPX_in_IP, VRRP, PGM, L2TP, DDX, IATP, STP, SRP, UTI, SMP,
    SM, PTP, FIRE, CRTP, CRUDP, SSCOPMCE, IPLT, SPS, PIPE, SCTP, FC, manet, HIP, Shim6,
    WESP, ROHC
  }

  // final EthernetFrame eth;
  final IpAddress srcIP, dstIP;

  final int ttl, header_length;
  final IP_NESTED_PROTOCOLS type;

  @ToString.Exclude
  final byte[] payload, data;

  /**
   * Привести payload Ethernet фрейма к IP пакету
   * 
   * @param eth
   */
  public IpPackage(EthernetFrame eth) {
    this(eth.payload);
  }

  public IpPackage(byte[] payload) {

    data = payload;
    header_length = 4 * (int) (payload[0] & 0x0F);
    // header_length = 20;
    ttl = payload[8];

    type = IP_NESTED_PROTOCOLS.values()[payload[9]];

    dstIP = IpAddress.build(copyOfRange(payload, 12, 16));
    srcIP = IpAddress.build(copyOfRange(payload, 16, 20));
    this.payload = copyOfRange(payload, header_length, payload.length);

  }

  public static IpPackage from(IpAddress dstIp, IpAddress srcIp, UdpDatagram pack) {

    byte[] udp_bytes = pack.toBytes();

    byte[] result = new byte[20 + udp_bytes.length];

    for (int i = 0; i < udp_bytes.length; i++)
      result[i + 20] = udp_bytes[i];

    byte ipVersion = 4;
    byte headerLength = 5;

    result[0] = (byte) ((ipVersion << 4) | headerLength);
    result[1] = 0b00000000; // DSF

    int total_length = result.length;
    result[2] = (byte) (total_length >> 24);
    result[3] = (byte) (total_length >> 16);

    // Package ID
    result[4] = (byte) ThreadLocalRandom.current().nextInt();
    result[5] = (byte) ThreadLocalRandom.current().nextInt();

    result[6] = 0b01000000;
    result[7] = 0x00000000;

    // TTL
    result[8] = 64;

    // UDP flag
    result[9] = (byte) IP_NESTED_PROTOCOLS.UDP.ordinal();

    byte[] srcIpBytes = srcIp.toBytes();
    result[12] = srcIpBytes[0];
    result[13] = srcIpBytes[1];
    result[14] = srcIpBytes[2];
    result[15] = srcIpBytes[3];

    byte[] dstIpBytes = dstIp.toBytes();
    result[16] = dstIpBytes[0];
    result[17] = dstIpBytes[1];
    result[18] = dstIpBytes[2];
    result[19] = dstIpBytes[3];

    // Checksum. See https://www.ietf.org/rfc/rfc1071.txt
    // TODO
    long cs = checksum(result, 20);
    result[10] = (byte) (cs >> 8);
    result[11] = (byte) cs;

    return new IpPackage(result);
  };

  public static long checksum(byte[] buf, int length) {
    int i = 0;
    long sum = 0;
    while (length > 0) {
      sum += (buf[i++] & 0xff) << 8;
      if ((--length) == 0)
        break;
      sum += (buf[i++] & 0xff);
      --length;
    }

    return (~((sum & 0xFFFF) + (sum >> 16))) & 0xFFFF;
  }

  @Override
  public byte[] toBytes() {
    return data;
  }
}
