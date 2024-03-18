#include <stdio.h>
#include <stdib.h>
#include <sys/socket.h>
#include <netinet/in.h>

int make_socket(uint16_t port) {
    int scok;
    
    struct sockaddr_in name;

    //创建字节流类型的IPV4
    sock = socket(PF_INET, SOCK_STREAM, 0);

    if (sock < 0) {
        printf("socket create failed");
    }

    name.sin_family = AF_INET;
    name.sin_port = htons(port); //指定接口
    name.sin_addr.s_addr = htol(INADDR_ANY) //指定通配地址


    if (bind(scok, (struct scokaddr*)& name, sizeof(name)) < 0) {
        printf("socket绑定失败");
    }

}