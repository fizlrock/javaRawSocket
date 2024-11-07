#include "Main.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <arpa/inet.h>
#include <iostream>
#include <jni.h>
#include <netinet/in.h>
#include <stdio.h>
#include <sys/socket.h>
#include <sys/types.h>

#include <arpa/inet.h>
#include <cstring>
#include <iostream>
#include <netinet/ip.h>
#include <unistd.h>

#include <netinet/ether.h> // For Ethernet header

void printMacAddress(unsigned char *mac) {
  std::cout << std::hex;
  for (int i = 0; i < 6; ++i) {
    std::cout << (int)mac[i];
    if (i < 5)
      std::cout << ":";
  }
  std::cout << std::dec << std::endl;
}

int socketFd = -1;
int packageN = 0;

JNIEXPORT jboolean JNICALL Java_Main_init(JNIEnv *env, jclass,
                                          jstring jInterfaceName) {

  const char *INTERFACE_NAME = env->GetStringUTFChars(jInterfaceName, nullptr);
  // socketFd =
  //     socket(AF_INET, SOCK_RAW, IPPROTO_ICMP); // Пример для протокола ICMP

  socketFd = socket(AF_PACKET, SOCK_RAW, htons(ETH_P_ALL));

  if (socketFd < 0)
    perror("Ошибка создания сокета");

  // Шаг 2: Привязываем сокет к интерфейсу через SO_BINDTODEVICE
  if (setsockopt(socketFd, SOL_SOCKET, SO_BINDTODEVICE, INTERFACE_NAME,

                 strlen(INTERFACE_NAME)) < 0) {
    perror("Ошибка привязки к интерфейсу");
    close(socketFd);
  }
  return socketFd > 0;
}

JNIEXPORT void JNICALL Java_Main_deinit(JNIEnv *, jclass) { close(socketFd); }

JNIEXPORT jint JNICALL Java_Main_sendTo(JNIEnv *, jclass, jbyteArray) {
  return 0;
}

JNIEXPORT jbyteArray JNICALL Java_Main_recvFrame(JNIEnv *env, jclass) {

  jbyteArray byteArray;
  unsigned char buffer[65536]; // Буфер для получения пакетов

  int bytes_received =
      recvfrom(socketFd, buffer, sizeof(buffer), 0, nullptr, nullptr);

  if (bytes_received < 0) {
    perror("Ошибка получения пакета");
    return nullptr;
  }

  byteArray = env->NewByteArray(bytes_received);
  env->SetByteArrayRegion(byteArray, 0, bytes_received,
                          reinterpret_cast<const jbyte *>(buffer));
  return byteArray;
}
