
#include <stdlib.h>
#include <stdio.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <unistd.h>
#include <iostream>
#include <thread>

#define SERVER_PORT 8999
#define SERVER_IP "127.0.0.1"
#define WAIT_ACCEPT_COUNT 15
#define MAX_BUFFER_LEN 1024

int main(int argc, char const *argv[])
{
    int serverFd = socket(AF_INET, SOCK_STREAM, 0);
    if(serverFd < 0) {
        std::cout << "socket err: " << strerror(errno) << std::endl;
        return -1;
    } 

    sockaddr_in serverAddress;
    bzero(&serverAddress, sizeof(serverAddress));
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_port = htons(SERVER_PORT);
    inet_pton(AF_INET, SERVER_IP, &serverAddress.sin_addr);

    int ret = bind(serverFd, (sockaddr* )&serverAddress, sizeof(serverAddress));
    if(ret != 0) {
        close(serverFd);
        std::cout << "bind err: " << strerror(errno) << std::endl;
        return -1;
    }

    ret = listen(serverFd, WAIT_ACCEPT_COUNT);
    if(ret != 0) {
        close(serverFd);
        std::cout << "listen err: " << strerror(errno) << std::endl;
        return -1;
    }

    while(true){
        sockaddr_in clientAddress;
        int len = sizeof(clientAddress);
        bzero(&clientAddress, len);
        int connFd = accept(serverFd, (sockaddr*)&clientAddress, (socklen_t *)&len);
        if(connFd < 0) {
            std::cout << "accept err: " << strerror(errno) << std::endl;
            continue;
        }

        //begin a new thread 
        std::thread t = std::thread([connFd](){
            std::cout << "thread begin" << std::endl;
            int ret = 0;
            char readBuff[MAX_BUFFER_LEN] = {0};
            while((ret = read(connFd, readBuff, MAX_BUFFER_LEN)) > 0){
                std::cout << "recv,len " << ret << ",threadId "<< std::this_thread::get_id() <<std::endl;
                write(connFd, readBuff, ret);
                std::cout << "write end, threadId " << std::this_thread::get_id() <<std::endl;
                bzero(readBuff, MAX_BUFFER_LEN);          
            }
            if(ret == 0) {
                std::cout << "the connect of client if close" << std::endl; 
            }else {
                std::cout << "read err " << strerror(errno) << std::endl;
            } 
            close(connFd);
        });
        t.detach();
    }
    
    close(serverFd);
    return 0;
}


