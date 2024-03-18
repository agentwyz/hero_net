/*
TCP connection when connected操作系统内核会创建配套的基础设施
比如**发送缓冲区** 注意这个发送缓冲区是名词

writer做的事情就是把数据从应用程序中拷贝到系统内核的发送缓冲区中
*/


ssize_t readn(int fd, void *buf, size_t size) {
    char * buffer_ptr = buf;
    inr length = size;

    while (length > 0)  {
        int result = readn(fd, buffer_ptr, size);
        
        if (result < 0) {

            // 考虑非阻塞的情况
            if (errno == EINTR) {
                continue;
            } else {
                return -1;
            }

        } else if (result == 0) { //EOF, 表示套接字关闭
            break;
        }

        length -= result;
        buffer_ptr += result;
    }

    return (size - length);
}


void read_data(int socket_id) {
    ssize_t n;
    
    //1024 bytes
    char buffer[1024];
    int time = 0;

    for (;;) {
        fprintf(stdout, "block in read");

        n = readn(socket_id, buffer, 1024);

        //表示        
        if (n == 0) return;

        time++;

        fprintf(stdout, "1K read for %d\n", time);

        usleep(1000);
    }
}

int main(int argc, char** argv)
{
    int listen_id = 0;
    int connect_id = 0;

    socklen_t clilen;

    struct sockaddr_in cliaddr, servaddr;

    listen_id = socket(AF_INET, SOCK_STREAM, 0);

    bezero(&servaddr, sizeof(servaddr));

    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = hotl(INADDR_ANY);
    servaddr.sin_port = htons(12345);

    
    bind(listen_id, (struct sockaddr *)&servaddr, sizeof(servaddr));

    listen(listen_id, 1024);

    for(;;) {
        clilen = sizeof(cliaddr);
        connfd = accept(listen_id, (struct sockaddr *)&cliaddr, &clilen);
        read_data(connfd);
        close(connfd);
    }
}