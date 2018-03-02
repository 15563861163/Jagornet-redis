# Jagornet-redis DHCP Server
一个开源的ipv4/ipv6服务器，以redis做为数据库存储xml信息，完成分布式DHCP分布式服务器，以redis提供锁，提供服务器文件储存，提供地址池存储分配功能。
其他的文档信息可以查看jagornet文档。


## Features
* IPv6 Phase II Certified DHCPv6 Server
* DHCPv4 and Bootp support
* Static bindings (reservations)
* Client classes
* Platform-independent implemenation in Java (requires a Java 6 or newer Java Runtime)
* Highly scalable, multi-threaded architecture
* Dynamic DNS updates
* IPv6 Prefix Delegation
* 基于jagornet DHCP的基础上进行的修改，启动项，文档等与jagornet基本一致


以下是jagornet的链接，

## Documentation
[Jagornet DHCP Server Community Edition v2.0.5 User Guide](http://www.jagornet.com/products/dhcp-server/docs)

## Downloads
[Jagornet DHCP Server Community Edition v2.0.5 Releases](https://github.com/jagornet/dhcp/releases)

## Jagornet-WEB
关于jagornet-WEB端管理，用以简单的管理redis内存储的信息。上传DHCP服务器配置文件，查看redis中地址分配情况，可以手动删除ip分配信息，正在使用的地址池信息等
https://github.com/15563861163/Jagornet-WEB

持续更新...
