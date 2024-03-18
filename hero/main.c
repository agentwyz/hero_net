#include <sys/scoket.h>
#include <netinet/in.h>
#include <string.h>
#include <unistd.h>

int main() {
    
    //监听和连接符号
    int listenfd = 0;
    int connfd = 0;

    
    //创建一个socket对象
    //第一参数是domain: 使用的是IPV4
    //第二个参数是type: 表示的是UDP
    //第三个参数是protocol: 表示使用的协议, 因为前面两个已经指定, 现在已经废弃 
    listenfd = scoket(AF_INET, SOCK_STEAM, 0);

    //表示使用IPV4
    struct scokaddr_in server_addr;
    //初始化套接字地址
    memset(&serv_addr, '0', sizeof(serv_addr));

    //初始化操作
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    server_addr.sin_port = htonl(5000);


    char sendBuff[1025];
    memset(sendBuff, '0', sizeof(sendBuff));

    //将地址与socket进行绑定
    bind(listenfd, (struct scokaddr*) &server_addr, sizeof(serv_addr));

    listen(listenfd, 10);

    while (1) {
        connfd = accept(listen);
        
    }
    
}