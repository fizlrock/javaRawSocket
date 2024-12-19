#include "org_example_FNETLib.h"

#include <cerrno>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <net/if.h>

#include <arpa/inet.h>
#include <jni.h>
#include <netinet/in.h>
#include <stdio.h>
#include <sys/socket.h>
#include <sys/types.h>

#include <arpa/inet.h>
#include <cstring>
#include <netinet/ip.h>
#include <unistd.h>

#include <linux/if_packet.h> // struct sockaddr_ll (see man 7 packet)
#include <netinet/ether.h>   // For Ethernet header

int socketFd = -1;
int packageN = 0;

// Используется для привязки сокета к конкретному сетевому интерфейсу и для
// задания параметров адресации при отправке и получении кадров.
struct sockaddr_ll socketConfig;

JNIEXPORT jboolean JNICALL
Java_org_example_FNETLib_init(JNIEnv *env, jclass, jstring jInterfaceName) {

  const char *INTERFACE_NAME = env->GetStringUTFChars(jInterfaceName, nullptr);
  uint interface_index = if_nametoindex(INTERFACE_NAME);

  // Инициализация сокета

  socketFd = socket(AF_PACKET, SOCK_RAW, htons(ETH_P_ALL));

  if (socketFd < 0) {
    perror("Ошибка создания сокета");
    return false;
  }

  // Инициализация sockaddr_ll
  memset(&socketConfig, 0, sizeof(socketConfig));

  // Семейство адресов. Работаем на сетевом уровне
  socketConfig.sll_family = AF_PACKET;
  // Длина адреса
  socketConfig.sll_halen = 6;
  // Индекс сетевого интерфейса
  socketConfig.sll_ifindex = interface_index;
  // Получаем все пакеты. Записываем в big-endian формате
  socketConfig.sll_protocol = htons(ETH_P_ALL);

  socketConfig.sll_pkttype = PACKET_HOST; // Получаем пакеты предназначенные для хоста
  socketConfig.sll_hatype = ARPHRD_LOOPBACK; // Тип адреса - loopback

  // socketConfig.sll_addr заполнен нулями

  int result =
      bind(socketFd, (struct sockaddr *)&socketConfig, sizeof(socketConfig));

  return socketFd > 0;
}

JNIEXPORT void JNICALL Java_org_example_FNETLib_deinit(JNIEnv *, jclass) {
  close(socketFd);
}

JNIEXPORT jint JNICALL Java_org_example_FNETLib_sendTo(JNIEnv *env, jclass,
                                                       jbyteArray array) {

  printf("Я в сии\n");
  // if (array == NULL) {
  //   return -1; // Возвращаем ошибку
  // }

  // Получаем длину массива
  jsize length = (*env).GetArrayLength(array);
  if (length == 0)
    return 0; // Нет данных для отправки

  // Получаем указатель на элементы массива
  jbyte *data = (*env).GetByteArrayElements(array, NULL);

  // Отправляем данные через сокет
  ssize_t sentBytes = send(socketFd, data, length, 0);

  if (sentBytes < 0)
    printf("Error sending data: %s\n", strerror(errno));

  // Освобождаем ресурсы
  (*env).ReleaseByteArrayElements(
      array, data, JNI_ABORT); // JNI_ABORT говорит JVM не копировать измененные
                               // данные обратно в array

  return (jint)sentBytes; // Возвращаем количество отправл
}

JNIEXPORT jbyteArray JNICALL Java_org_example_FNETLib_recvFrame(JNIEnv *env,
                                                                jclass) {

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
