# Ethcube 

对以太坊API进行包装

# 设计

使用 Akka Http 对外提供服务(Root 节点), 内部采用Akka Cluster做分布式服务处理, worker 节点负责连接Geth/Parity客户端获取以太坊数据, 直接返回给调用者。 

<img src="./docs/ethcube.png"/>

## Root节点

Root 节点使用Akka Http对外提供Restful接口, 这里不做数据缓存, 直接利用Akka Cluster技术向worker节点发送请求, 得到数据之后直接返回。

## Worker节点

Worker 节点对外是以Actor调用, 参数是以protobuf定义, Worker 节点可以多部署, 由Root节点随机访问, 做均衡请求处理。


# 运行

以下命令都是在工程路径下面执行, 运行项目可以使用环境变量或是命令行参数

以下两种方式结果一样, 自动加载 application.conf 和 conf/test.conf

如果不使用参数, 自动加载 application.conf 和 conf/dev.conf


例如 1: 

```
export env="test"

sbt root/run
```

例如 2: 


```
sbt "root/run test"
```

## 编译

```
sbt clean compile
```

## eclipse 编译

```
sbt clean compile eclipse
```

## Root 运行

```
sbt root/run
```

## Worker 运行

```
sbt worker/run
```

## docker 运行

### root docker

```

sbt root/docker:publishLocal

docker images

docker run -it -p 8080:8080 root:0.1.0-SNAPSHOT / docker run -d -p 8080:8080 --name root root:0.1.0-SNAPSHOT

```

### worker docker

```

sbt worker/docker:publishLocal

docker images

docker run -it -p 8080:8080 worker:0.1.0-SNAPSHOT / docker run -d -p 8080:8080 --name worker worker:0.1.0-SNAPSHOT

```


# 功能

1. 提供以太坊API
2. 客户端管理(状态, 延时, 同步等)


