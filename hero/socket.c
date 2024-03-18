/*
socket_addr是一个通用的地址结构
sa_family地址族, 表示使用什么样的方式对地址进行解释和保存

AF表示address Family, 地址族, PF表示Protocol Family 协议族
AF_LOCAL: 表示本机地址
AF_INET: IPV4
AF_INET6: IPV6
*/

typedef unsigned short int sa_family_t;


struct scokaddr {
    sa_family_t sa_family;
    char sa_data[14]; //具体的地址值 14*8=112
};

//IPV4: 32位
typedef uint32_t in_addr_t;
struct in_addr {
    in_addr_t s_addr;
};


//描述IPV4的套接字地址格式
struct scokaddr_in {
    sa_family_t sin_family;     //16-bit: AF_INET
    in_port_t sin_port          //端口号
    struct in_addr sin_addr;    //32-bit
    unsigned char sin_zero[8];  //占位符
};


struct sockaddr_in6 {
    sa_family_t sin6_family;
    in_port_t sin6_port;
    uint32_t sin6_flowinfo;
    struct in6_addr sin6_addr;
}


//本地进程之间进行通信
struct scokaddr_un {
    unsigned short sun_family;
    char sun_path[108]; //路径名称
};


//domain: 表示使用什么样的套接字
//type: 表示使用什么格式的数据, socket_stream, 对应TCP
//protocol: 现在这个值基本已经废弃
int socket(int domain, int type, int protocol);

//该函数负责把套接字和套接字地址绑定
//虽然第二个参数是通用的地址格式, 实际传入的参数可能是IPV4
bind(int fd, sockaddr* addr, socklen_t len)



//设置一个通配地址


//设置一个IPV4地址
struct scokaddr_in name;
name.sin_addr.s_addr = htonl(INADDR_ANY);


int accept(int listenscokfd, struct scokaddr* cliaddr, sockaddr* addrlen);




////////使用listen函数
//第一个参数表示套接字描述符
//第二个backlog, 在linux中表示已经完成且未accept的队列大小
int listen(int socketfd, int backlog);




