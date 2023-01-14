2 搭建模块环境
2.1 架构的问题分析
当前要开发的是媒资管理服务，目前为止共三个三微服务：内容管理、系统管理、媒资管理，如下图：

后期还会添加更多的微服务，当前这种由前端直接请求微服务的方式存在弊端：
如果在前端对每个请求地址都配置绝对路径，非常不利于系统维护，比如下边代码中请求系统管理服务的地址使用的是localhost

当系统上线后这里需要改成公网的域名，如果这种地址非常多则非常麻烦。
基于这个问题可以采用网关来解决，如下图：

这样在前端的代码中只需要指定每个接口的相对路径，如下所示：

在前端代码的一个固定的地方在接口地址前统一加网关的地址，每个请求统一到网关，由网关将请求转发到具体的微服务。
为什么所有的请求先到网关呢？
有了网关就可以对请求进行路由，比如：可以根据请求路径路由、根据host地址路由等， 当微服务有多个实例时可以通过负载均衡算法进行路由，如下：

另外，网关还可以实现权限控制、限流等功能。
项目采用Spring Cloud Gateway作为网关，网关在请求路由时需要知道每个微服务实例的地址，项目使用Nacos作用服务发现中心和配置中心，整体的架构图如下：

流程如下：
1、微服务启动，将自己注册到Nacos，Nacos记录了各微服务实例的地址。
2、网关从Nacos读取服务列表，包括服务名称、服务地址等。
3、请求到达网关，网关将请求路由到具体的微服务。
要使用网关首先搭建Nacos，Nacos有两个作用：
1、服务发现中心。
微服务将自身注册至Nacos，网关从Nacos获取微服务列表。
2、配置中心。
微服务众多，它们的配置信息也非常复杂，为了提供系统的可维护性，微服务的配置信息统一在Nacos配置。
2.2 搭建Nacos
2.2.1 服务发现中心
根据上节讲解的网关的架构图，要使用网关首先搭建Nacos。
首先搭建Nacos服务发现中心。
在搭建Nacos服务发现中心之前需要搞清楚两个概念：namespace和group
namespace：用于区分环境、比如：开发环境、测试环境、生产环境。
group：用于区分项目，比如：xuecheng-plus项目、xuecheng2.0项目
首先在nacos配置namespace:
登录Centos，启动Naocs，使用sh /data/soft/restart.sh将自动启动Nacos。
访问：http://192.168.101.65:8848/nacos/
账号密码：nacos/nacos
登录成功，点击左侧菜单“命名空间”进入命名空间管理界面，

点击“新建命名空间”，填写命名空间的相关信息。如下图：

使用相同的方法再创建“测试环境”、"生产环境"的命名空间。


创建成功，如下图：

这里创建具体班级的命名空间，假如创建1010班级的命名空间，如下：

首先完成各服务注册到Naocs，下边将内容管理服务注册到nacos中。
1) 在xuecheng-plus-parent中添加依赖管理 
XML<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>${spring-cloud-alibaba.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
2）在内容管理模块的接口工程、系统管理模块的接口工程中添加如下依赖
XML<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
3）配置nacos的地址
在系统管理的接口工程的配置文件中配置如下信息：
YAML#微服务配置
spring:
  application:
    name: system-service
  cloud:
    nacos:
      server-addr: 192.168.101.65:8848
      discovery:
        namespace: dev
        group: xuecheng-plus-project
在内容管理的接口工程的配置文件中配置如下信息：
YAMLspring:
  application:
    name: content-service
  cloud:
    nacos:
      server-addr: 192.168.101.65:8848
      discovery:
        namespace: dev
        group: xuecheng-plus-project
4）重启内容管理服务、系统管理服务。
待微服务启动成功，进入Nacos服务查看服务列表

在 “开发环境” 命名空间下有两个服务实例。微服务在Nacos注册成功。
点击其它一个微服务的“详情”

通过上图可以查看微服务实例的地址。
2.2.2 配置中心
2.2.2.1 配置三要素
搭建完成Nacos服务发现中心，下边搭建Nacos为配置中心，其目的就是通过Nacos去管理项目的所有配置。
先将项目中的配置文件分分类：
1、每个项目特有的配置
是指该配置只在有些项目中需要配置，或者该配置在每个项目中配置的值不同。
比如：spring.application.name每个项目都需要配置但值不一样，以及有些项目需要连接数据而有些项目不需要，有些项目需要配置消息队列而有些项目不需要。
2、项目所公用的配置
是指在若干项目中配置内容相同的配置。比如：redis的配置，很多项目用的同一套redis服务所以配置也一样。
另外还需要知道nacos如何去定位一个具体的配置文件，即配置的三要素：namespace、group、dataid. 
1、通过namespace、group找到具体的环境和具体的项目。
2、通过dataid找到具体的配置文件，dataid有三部分组成，
比如：content-service-dev.yaml配置文件  由（content-service）-（dev）. (yaml)三部分组成
content-service：第一部分，它是在application.yaml中配置的应用名，即spring.application.name的值。
dev：第二部分，它是环境名，通过spring.profiles.active指定，
Yaml: 第三部分，它是配置文件 的后缀，目前nacos支持properties、yaml等格式类型，本项目选择yaml格式类型。
所以，如果我们要配置content-service工程的配置文件:
在开发环境中配置content-service-dev.yaml
在测试环境中配置content-service-test.yaml
在生产环境中配置content-service-prod.yaml
2.2.2.2 配置content-service
下边以开发环境为例对content-service工程的配置文件进行配置，进入nacos，进入开发环境。

点击 加号，添加一个配置

输入data id、group以及配置文件内容。
为什么没在nacos中配置下边的内容 ？
YAMLspring:
  application:
    name: content-service
因为刚才说了dataid第一部分就是spring.application.name，nacos 客户端要根据此值确定配置文件 名称，所以spring.application.name不在nacos中配置，而是要在工程的本地进行配置。
本地配置文件现在是application.yaml需要修改为bootstrap.yaml，因为SpringBoot读取配置文件 的顺序如下：

所以在content-service工程 中添加bootstrap.yaml，内容如下：
YAMLspring:
  application:
    name: content-service
  cloud:
    nacos:
      server-addr: 192.168.101.65:8848
      discovery:
        namespace: ${spring.profiles.active}
        group: xuecheng-plus-project
      config:
        namespace: ${spring.profiles.active}
        group: xuecheng-plus-project
        file-extension: yaml
        refresh-enabled: true

#profiles默认为dev
  profiles:
    active: dev
最后删除原来的application.yaml。
在内容管理模块的接口工程和service工程、系统管理的接口工程和service工程配置依赖：
XML<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
配置完成，运行content-service工程 的单元测试文件，能否正常测试。
通过运行观察控制台打印出下边的信息：
Plain Text[NacosRestTemplate.java:476] - HTTP method: POST, url: http://192.168.101.65:8848/nacos/v1/cs/configs/listener, body: {Listening-Configs=content-service.yaml?xuecheng-plus-project??dev?content-service-dev.yaml?xuecheng-plus-project?88459b1483b8381eccc2ef462bd59182?dev?content-service?xuecheng-plus-project??dev?, tenant=dev}
NacosRestTemplate.java通过Post方式去nacos读取配置信息。
跟踪单元测试方法可以正常读取数据库的数据，说明从nacos读取配置信息正常。
2.2.2.3配置content-api
以相同的方法配置content-api工程的配置文件，在nacos中的开发环境中配置content-api-dev.yaml，内容如下：
YAMLserver:
  servlet:
    context-path: /content
  port: 63040

# 日志文件配置路径
logging:
  config: classpath:log4j2-dev.xml

# swagger 文档配置
swagger:
  title: "学成在线内容管理系统"
  description: "内容系统管理系统对课程相关信息进行业务管理数据"
  base-package: com.xuecheng.content
  enabled: true
  version: 1.0.0
在content-api工程 的本地配置bootstrap.yaml，内容如下：
YAML#server:
#  servlet:
#    context-path: /content
#  port: 63040
#微服务配置
spring:
  application:
    name: content-api
  cloud:
    nacos:
      server-addr: 192.168.101.65:8848
      discovery:
        namespace: ${spring.profiles.active}
        group: xuecheng-plus-project
      config:
        namespace: ${spring.profiles.active}
        group: xuecheng-plus-project
        file-extension: yaml
        refresh-enabled: true
        extension-configs:
          - data-id: content-service-${spring.profiles.active}.yaml
            group: xuecheng-plus-project
            refresh: true
  profiles:
    active: dev

# 日志文件配置路径
logging:
  config: classpath:log4j2-dev.xml

# swagger 文档配置
swagger:
  title: "学成在线内容管理系统"
  description: "内容系统管理系统对课程相关信息进行业务管理数据"
  base-package: com.xuecheng.content
  enabled: true
  version: 1.0.0
注意：因为api接口工程依赖了service工程 的jar，所以这里使用extension-configs扩展配置文件 的方式引用service工程所用到的配置文件。
YAML        extension-configs:
          - data-id: content-service-${spring.profiles.active}.yaml
            group: xuecheng-plus-project
            refresh: true
如果添加多个扩展文件，继续在下添加即可，如下：
YAML        extension-configs:
          - data-id: content-service-${spring.profiles.active}.yaml
            group: xuecheng-plus-project
            refresh: true
          - data-id: 填写文件 dataid
            group: xuecheng-plus-project
            refresh: true           
启动content-api工程，查询控制台是否打印出了请求nacos的日志，如下:
Bash[NacosRestTemplate.java:476] - HTTP method: POST, url: http://192.168.101.65:8848/nacos/v1/cs/configs/listener
并使用Httpclient测试课程查询接口是否可以正常查询。
2.2.3 公用配置
还有一个优化的点是如何在nacos中配置项目的公用配置呢？
nacos提供了shared-configs可以引入公用配置。
在content-api中配置了swagger，所有的接口工程 都需要配置swagger，这里就可以将swagger的配置定义为一个公用配置，哪个项目用引入即可。
单独在xuecheng-plus-common分组下创建xuecheng-plus的公用配置，进入nacos的开发环境，添加swagger-dev.yaml公用配置

删除接口工程中对swagger的配置。
项目使用shared-configs可以引入公用配置。在接口工程的本地配置文件 中引入公用配置，如下：
YAMLspring:
  application:
    name: content-api
  cloud:
    nacos:
      server-addr: 192.168.101.65:8848
      discovery:
        namespace: dev
        group: xuecheng-plus-project
      config:
        namespace: dev
        group: xuecheng-plus-project
        file-extension: yaml
        refresh-enabled: true
        extension-configs:
          - data-id: content-service-${spring.profiles.active}.yaml
            group: xuecheng-plus-project
            refresh: true
        shared-configs:
          - data-id: swagger-${spring.profiles.active}.yaml
            group: xuecheng-plus-common
            refresh: true
          - data-id: logging-${spring.profiles.active}.yaml
            group: xuecheng-plus-common
            refresh: true
  profiles:
    active: dev
再以相同 的方法配置日志的公用配置。

在接口工程和业务工程，引入loggin-dev.yaml公用配置文件 

配置完成，重启content-api接口工程，访问http://localhost:63040/content/swagger-ui.html 查看swagger接口文档是否可以正常访问，查看控制台log4j2日志输出是否正常。
2.2.4 系统管理配置
按照上边的方法 自行将系统管理服务的配置信息在nacos上进行配置。
2.2.5 配置优先级
到目前为止已将所有微服务的配置统一在nacos进行配置，用到的配置文件有本地的配置文件 bootstrap.yaml和nacos上的配置文件，引入配置文件的形式有：
1、通过dataid方式引入
2、以扩展配置文件方式引入
3、以共享配置文件 方式引入
下边测试这几种配置文件方式的优先级。
我们使用内容管理服务中的配置文件，首先在共享配置文件 swagger-dev.yaml中配置四个配置项，如下：

配置完成发布。
下边在content-api工程的启动类中添加如下代码读取这四个配置项的值
Javapackage com.xuecheng;


import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableSwagger2Doc
@SpringBootApplication
public class ContentApplication {

   @Value("${test_config.a}")
   String a;
   @Value("${test_config.b}")
   String b;
   @Value("${test_config.c}")
   String c;
   @Value("${test_config.d}")
   String d;

   @Bean
   public Integer getTest(){
      System.out.println("a="+a);
      System.out.println("b="+b);
      System.out.println("c="+c);
      System.out.println("d="+d);
      return new Integer(1);
   }

   public static void main(String[] args) {
      SpringApplication.run(ContentApplication.class, args);
   }


}
启动content-api工程，在return new Integer(1);处打断点，运行到断点处，如下：

这说明已经成功读取到 四个配置项的值。
下边在content-api工程的扩展配置文件 conent-service-dev.yaml中配置三个配置项，如下：

再次重启content-api工程，在return new Integer(1);处打断点，运行到断点处，如下：

从结果可以看出，扩展配置文件比共享配置文件优先级高。
下边继续content-api-dev.yaml中配置两个配置项，如下：

再次重启内容管理接口工程，在return new Integer(1);处打断点，运行到断点处，如下：

从结果可以看出，通过工程的应用名找到配置文件 content-api-dev.yaml优先级比扩展配置文件 高。
下边我们在content-api工程的本地配置文件bootstrap.yaml中配置如下：
YAMLtest_config:
  a: 4a
  b: 4b
  c: 4c
  d: 4d
再次重启内容管理接口工程，在return new Integer(1);处打断点，运行到断点处，如下：

这说明本地配置文件配置的内容没有起作用，原因是nacos配置文件中的相同的配置项覆盖了本地的配置项。
到这可以总结各各配置文件 的优先级：项目应用名配置文件 > 扩展配置文件  > 共享配置文件 > 本地配置文件。
有时候我们在测试程序时直接在本地加一个配置进行测试，这时我们想让本地最优先，可以在nacos配置文件 中配置如下即可实现：
YAML#配置本地优先
spring:
 cloud:
  config:
    override-none: true
再次重启content-api工程，在return new Integer(1);处打断点，运行到断点处，如下：

可以看出此时本地配置最优先。
2.2.6 导入配置文件
课程资料中提供了系统用的所有配置文件nacos_config_export.zip，下边将nacos_config_export.zip导入nacos。
进入具体的命名空间，点击“导入配置”

打开导入窗口

相同的配置选择覆盖配置。
点击“上传文件”选择nacos_config_export.zip开始导入。
2.3 搭建Gateway
本项目使用Spring Cloud Gateway作为网关，下边创建网关工程。
新建一个网关工程。

工程结构

添加依赖：
XML<dependencies>
    <!--网关-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <!--服务发现中心-->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>

    <!-- 排除 Spring Boot 依赖的日志包冲突 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Spring Boot 集成 log4j2 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-log4j2</artifactId>
    </dependency>


</dependencies>
配置网关的bootstrap.yaml配置文件
YAML#微服务配置
spring:
  application:
    name: gateway
  cloud:
    nacos:
      server-addr: 192.168.101.65:8848
      discovery:
        namespace: ${spring.profiles.active}
        group: xuecheng-plus-project
      config:
        namespace: ${spring.profiles.active}
        group: xuecheng-plus-project
        file-extension: yaml
        refresh-enabled: true
        shared-configs:
          - data-id: logging-${spring.profiles.active}.yaml
            group: xuecheng-plus-common
            refresh: true


  profiles:
    active: dev
在nacos上配置网关路由策略：

详细配置如下：
YAMLserver:
  port: 63010 # 网关端口
spring:
  cloud:
    gateway:
#      filter:
#        strip-prefix:
#          enabled: true
      routes: # 网关路由配置
        - id: content-api # 路由id，自定义，只要唯一即可
          # uri: http://127.0.0.1:8081 # 路由的目标地址 http就是固定地址
          uri: lb://content-api # 路由的目标地址 lb就是负载均衡，后面跟服务名称
          predicates: # 路由断言，也就是判断请求是否符合路由规则的条件
            - Path=/content/** # 这个是按照路径匹配，只要以/content/开头就符合要求
#          filters:
#            - StripPrefix=1
        - id: system-api
          # uri: http://127.0.0.1:8081
          uri: lb://system-api
          predicates:
            - Path=/system/**
#          filters:
#            - StripPrefix=1
        - id: media-api
          # uri: http://127.0.0.1:8081
          uri: lb://media-api
          predicates:
            - Path=/media/**
#          filters:
#            - StripPrefix=1
启动网关工程，通过网关工程访问微服务进行测试。
在http-client-env.json中配置网关的地址

使用httpclient测试课程查询 接口，如下：
JSON### 课程查询列表
POST {{gateway_host}}/content/course/list?pageNo=2&pageSize=1
Content-Type: application/json

{
  "auditStatus": "202002",
  "courseName": ""
}
运行，观察是否可以正常访问接口 ，如下所示可以正常请求接口。
JSONhttp://localhost:63010/content/course/list?pageNo=2&pageSize=1

HTTP/1.1 200 OK
transfer-encoding: chunked
Content-Type: application/json
Date: Sun, 11 Sep 2022 09:54:32 GMT

{
  "items": [
    {
      "id": 26,
      "companyId": 1232141425,
      "companyName": null,
      "name": "spring cloud实战",
      "users": "所有人",
      "tags": null,
      "mt": "1-3",
      "mtName": null,
      "st": "1-3-2",
      "stName": null,
      "grade": "200003",
      "teachmode": "201001",
      "description": "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。",
      "pic": "https://cdn.educba.com/academy/wp-content/uploads/2018/08/Spring-BOOT-Interview-questions.jpg",
      "createDate": "2019-09-04 09:56:19",
      "changeDate": "2021-12-26 22:10:38",
      "createPeople": null,
      "changePeople": null,
      "auditStatus": "202002",
      "status": "203001",
      "coursePubId": null,
      "coursePubDate": null
    }
  ],
  "counts": 29,
  "page": 2,
  "pageSize": 1
}
网关工程搭建完成即可将前端工程中的接口地址改为网关的地址

启动前端工程，测试之前开发内容管理模块的功能。
2.4 搭建媒资工程 
至此网关、Nacos已经搭建完成，下边将媒资工程导入项目。
从课程资料中获取媒资工程 xuecheng-plus-media，拷贝到项目工程根目录。
右键pom.xml转为maven工程。

创建媒资数据库，并导入xcplus_media.sql

重启media-api工程。
3 分布式文件系统
3.1 什么是分布式文件系统
要理解分布式文件系统首先了解什么是文件系统。
查阅百度百科：

       文件系统是负责管理和存储文件的系统软件，操作系统通过文件系统提供的接口去存取文件，用户通过操作系统访问磁盘上的文件。
下图指示了文件系统所处的位置：

常见的文件系统：FAT16/FAT32、NTFS、HFS、UFS、APFS、XFS、Ext4等 。
现在有个问题，一此短视频平台拥有大量的视频、图片，这些视频文件、图片文件该如何存储呢？如何存储可以满足互联网上海量用户的浏览。
今天讲的分布式文件系统就是海量用户查阅海量文件的方案。
我们阅读百度百科去理解分布式文件系统的定义：

通过概念可以简单理解为：一个计算机无法存储海量的文件，通过网络将若干计算机组织起来共同去存储海量的文件，去接收海量用户的请求，这些组织起来的计算机通过网络进行通信，如下图：

 好处：
1、一台计算机的文件系统处理能力扩充到多台计算机同时处理。
 2、一台计算机挂了还有另外副本计算机提供数据。
 3、每台计算机可以放在不同的地域，这样用户就可以就近访问，提高访问速度。
市面上有哪些分布式文件系统的产品呢？
1、NFS
阅读百度百科：


特点：
1）在客户端上映射NFS服务器的驱动器。
2）客户端通过网络访问NFS服务器的硬盘完全透明。
2、GFS


1）GFS采用主从结构，一个GFS集群由一个master和大量的chunkserver组成。
2）master存储了数据文件的元数据，一个文件被分成了若干块存储在多个chunkserver中。
3）用户从master中获取数据元信息，向chunkserver存储数据。
3) HDFS
HDFS，是Hadoop Distributed File System的简称，是Hadoop抽象文件系统的一种实现。HDFS是一个高度容错性的系统，适合部署在廉价的机器上。HDFS能提供高吞吐量的数据访问，非常适合大规模数据集上的应用。 HDFS的文件分布在集群机器上，同时提供副本进行容错及可靠性保证。例如客户端写入读取文件的直接操作都是分布在集群各个机器上的，没有单点性能压力。
下图是HDFS的架构图：

1）HDFS采用主从结构，一个HDFS集群由一个名称结点和若干数据结点组成。
2) 名称结点存储数据的元信息，一个完整的数据文件分成若干块存储在数据结点。
3）客户端从名称结点获取数据的元信息及数据分块的信息，得到信息客户端即可从数据块来存取数据。
4、云计算厂家
阿里云对象存储服务（Object Storage Service，简称 OSS），是阿里云提供的海量、安全、低成本、高可靠的云存储服务。其数据设计持久性不低于 99.9999999999%（12 个 9），服务设计可用性（或业务连续性）不低于 99.995%。
官方网站：https://www.aliyun.com/product/oss 
百度对象存储BOS提供稳定、安全、高效、高可扩展的云存储服务。您可以将任意数量和形式的非结构化数据存入BOS，并对数据进行管理和处理。BOS支持标准、低频、冷和归档存储等多种存储类型，满足多场景的存储需求。 
官方网站：https://cloud.baidu.com/product/bos.html 
3.2 MinIO
3.2.1 介绍
本项目采用MinIO构建分布式文件系统，MinIO 是一个非常轻量的服务,可以很简单的和其他应用的结合使用，它兼容亚马逊 S3 云存储服务接口，非常适合于存储大容量非结构化的数据，例如图片、视频、日志文件、备份数据和容器/虚拟机镜像等。
它一大特点就是轻量，使用简单，功能强大，支持各种平台，单个文件最大5TB，兼容 Amazon S3接口，提供了 Java、Python、GO等多版本SDK支持。
官网：https://min.io
中文：https://www.minio.org.cn/，http://docs.minio.org.cn/docs/
MinIO集群采用去中心化共享架构，每个结点是对等关系，通过Nginx可对MinIO进行负载均衡访问。
去中心化有什么好处？
在大数据领域，通常的设计理念都是无中心和分布式。Minio分布式模式可以帮助你搭建一个高可用的对象存储服务，你可以使用这些存储设备，而不用考虑其真实物理位置。
它将分布在不同服务器上的多块硬盘组成一个对象存储服务。由于硬盘分布在不同的节点上，分布式Minio避免了单点故障。如下图：

Minio使用纠删码技术来保护数据，它是一种恢复丢失和损坏数据的数学算法，它将数据分块冗余的分散存储在各各节点的磁盘上，所有的可用磁盘组成一个集合，上图由8块硬盘组成一个集合，当上传一个文件时会通过纠删码算法计算对文件进行分块存储，除了将文件本身分成4个数据块，还会生成4个校验块，数据块和校验块会分散的存储在这8块硬盘上。
使用纠删码的好处是即便丢失一半数量（N/2）的硬盘，仍然可以恢复数据。 比如上边集合中有4个以内的硬盘损害仍可保证数据恢复，不影响上传和下载，如果多于一半的硬盘坏了则无法恢复。
3.2.2 数据恢复演示
下边在本机演示MinIO恢复数据的过程，在本地创建4个目录表示4个硬盘。

首先下载MinIO，下载地址：https://dl.min.io/server/minio/release/，也可从课程资料找到MinIO的安装文件。
CMD进入有minio.exe的目录，运行下边的命令：
Plain Textminio.exe server D:\develop\minio_data\data1  D:\develop\minio_data\data2  D:\develop\minio_data\data3  D:\develop\minio_data\data4
启动结果如下：

说明如下：
SQLWARNING: MINIO_ACCESS_KEY and MINIO_SECRET_KEY are deprecated.
         Please use MINIO_ROOT_USER and MINIO_ROOT_PASSWORD
Formatting 1st pool, 1 set(s), 4 drives per set.
WARNING: Host local has more than 2 drives of set. A host failure will result in data becoming unavailable.
WARNING: Detected default credentials 'minioadmin:minioadmin', we recommend that you change these values with 'MINIO_ROOT_USER' and 'MINIO_ROOT_PASSWORD' environment variables
1）老版本使用的MINIO_ACCESS_KEY 和 MINIO_SECRET_KEY不推荐使用，推荐使用MINIO_ROOT_USER 和MINIO_ROOT_PASSWORD设置账号和密码。
2）pool即minio节点组成的池子，当前有一个pool和4个硬盘组成的set集合
3）因为集合是4个硬盘，大于2的硬盘损坏数据将无法恢复。
4）账号和密码默认为minioadmin、minioadmin，可以在环境变量中设置通过'MINIO_ROOT_USER' and 'MINIO_ROOT_PASSWORD' 进行设置。
下边输入http://localhost:9000进行登录。

登录成功：

下一步创建bucket，桶，它相当于存储文件的目录，可以创建若干的桶。

输入bucket的名称，点击“CreateBucket”，创建成功

点击“upload”上传文件。
下边上传几个文件 

下边去四个目录观察文件的存储情况

我们发现上传的1.mp4文件存储在了四个目录，即四个硬盘上。
下边测试minio的数据恢复过程：
1、首先删除一个目录。
删除目录后仍然可以在web控制台上传文件和下载文件。
稍等片刻删除的目录自动恢复。
2、删除两个目录。
删除两个目录也会自动恢复。
3、删除三个目录 。
由于 集合中共有4块硬盘，有大于一半的硬盘损坏数据无法恢复。
此时报错：We encountered an internal error, please try again.  (Read failed.  Insufficient number of drives online)在线驱动器数量不足。
3.2.4 分布式集群测试
条件允许的情况下可以测试MinIO分布式存储的特性，首先准备环境。
分布式MinIO要求至少四个磁盘，建议至少4个节点，每个节点2个磁盘。
准备四台虚拟机：192.168.101.65、192.168.101.66、192.168.101.67、192.168.101.68
将课程资料下的minio的执行文件拷贝到四台虚拟机的/home/minio/目录下。
在四台虚拟机分别创建下边的脚本run.sh，内容如下：
Shell#!/bin/bash
# 创建日志目录
mkdir -p /boot/mediafiles/logs
# 创建存储目录
mkdir -p /boot/mediafiles/data/d{1,2,3,4}
# 创建配置目录
mkdir -p /etc/minio
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin

# 在四台机器上都执行该文件，以分布式的方式启动minio
# --address 为api端口（如Java客户端）访问的端口
# --console-address web控制台端口
/home/minio/minio server \
http://192.168.101.65:9000/home/mediafiles/data/export1 \
http://192.168.101.65:9000/home/mediafiles/data/export2 \
http://192.168.101.66:9000/home/mediafiles/data/export1 \
http://192.168.101.66:9000/home/mediafiles/data/export2 \
http://192.168.101.67:9000/home/mediafiles/data/export1 \
http://192.168.101.67:9000/home/mediafiles/data/export2 \
http://192.168.101.68:9000/home/mediafiles/data/export1 \
http://192.168.101.68:9000/home/mediafiles/data/export2 
在四台虚拟机执行脚本run.sh，注意观察日志。
启动成功后访问： http://192.168.101.66:9001/、http://192.168.101.67:9001/、http://192.168.101.68:9001/、http://192.168.101.69:9001/。
访问任意一个都可以操作 minio集群。
下边进行测试：
1、向集群上传一个文件，观察每个节点的两个磁盘目录都存储了数据。
2、停止 一个节点，不影响上传和下载。
假如停止了65节点，通过其它节点上传文件，稍后启动65后自动从其它结点同步文件。
3、停止 两个节点，无法上传，可以下载。
此时上传文件客户端报错如下：

上传文件需要至少一半加1个可用的磁盘。
将停止的两个节点的minio启动，稍等片刻 minio恢复可用。

3.2.4 测试Docker环境
开发阶段和生产阶段统一使用Docker下的MINIO。
在下发的虚拟机中已安装了MinIO的镜像和容器，执行sh /data/soft /restart.sh启动Docker下的MinIO
启动完成登录MinIO查看是否正常。
访问http://192.168.101.65:9000

本项目创建两个buckets：
mediafiles： 普通文件
video：视频文件
3.2.5 SDK
3.2.5.1上传文件
MinIO提供多个语言版本SDK的支持，下边找到java版本的文档：
地址：https://docs.min.io/docs/java-client-quickstart-guide.html
最低需求Java 1.8或更高版本:
maven依赖如下：
XML<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.4.3</version>
</dependency>
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.8.1</version>
</dependency>
在media-service工程添加此依赖。
参数说明：
需要三个参数才能连接到minio服务。
参数	说明
Endpoint	对象存储服务的URL
Access Key	Access key就像用户ID，可以唯一标识你的账户。
Secret Key	Secret key是你账户的密码。
 
示例代码如下：
Javaimport io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
public class FileUploader {
  public static void main(String[] args)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
    try {
      // Create a minioClient with the MinIO server playground, its access key and secret key.
      MinioClient minioClient =
          MinioClient.builder()
              .endpoint("https://play.min.io")
              .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
              .build();
      // Make 'asiatrip' bucket if not exist.
      boolean found =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket("asiatrip").build());
      if (!found) {
        // Make a new bucket called 'asiatrip'.
        minioClient.makeBucket(MakeBucketArgs.builder().bucket("asiatrip").build());
      } else {
        System.out.println("Bucket 'asiatrip' already exists.");
      }
      // Upload '/home/user/Photos/asiaphotos.zip' as object name 'asiaphotos-2015.zip' to bucket
      // 'asiatrip'.
      minioClient.uploadObject(
          UploadObjectArgs.builder()
              .bucket("asiatrip")
              .object("asiaphotos-2015.zip")
              .filename("/home/user/Photos/asiaphotos.zip")
              .build());
      System.out.println(
          "'/home/user/Photos/asiaphotos.zip' is successfully uploaded as "
              + "object 'asiaphotos-2015.zip' to bucket 'asiatrip'.");
    } catch (MinioException e) {
      System.out.println("Error occurred: " + e);
      System.out.println("HTTP trace: " + e.httpTrace());
    }
  }
}
参考示例在media-service工程中 测试上传文件功能，
首先创建一个用于测试的bucket

点击“Manage”修改bucket的访问权限

选择public权限

测试代码如下：
Javapackage com.xuecheng.media;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @description 测试MinIO
 * @author Mr.M
 * @date 2022/9/11 21:24
 * @version 1.0
 */
public class MinIOTest {

 static MinioClient minioClient =
         MinioClient.builder()
                 .endpoint("http://192.168.101.65:9000")
                 .credentials("minioadmin", "minioadmin")
                 .build();


 //上传文件
public static void upload()throws IOException, NoSuchAlgorithmException, InvalidKeyException {
 try {
  boolean found =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket("testbucket").build());
  //检查testbucket桶是否创建，没有创建自动创建
  if (!found) {
   minioClient.makeBucket(MakeBucketArgs.builder().bucket("testbucket").build());
  } else {
   System.out.println("Bucket 'testbucket' already exists.");
  }
  //上传1.mp4
  minioClient.uploadObject(
          UploadObjectArgs.builder()
                  .bucket("testbucket")
                  .object("1.mp4")
                  .filename("D:\\develop\\upload\\1.mp4")
                  .build());
  //上传1.avi,上传到avi子目录
  minioClient.uploadObject(
          UploadObjectArgs.builder()
                  .bucket("testbucket")
                  .object("avi/1.avi")
                  .filename("D:\\develop\\upload\\1.avi")
                  .build());
  System.out.println("上传成功");
 } catch (MinioException e) {
  System.out.println("Error occurred: " + e);
  System.out.println("HTTP trace: " + e.httpTrace());
 }

}
public static void main(String[] args)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
 upload();
}


}
执行main方法，共上传两个文件，1.mp4上传到桶根目录下，1.avi上传到 桶中的avi目录下，avi目录会自动创建。
上传成功，通过web控制台查看文件，并预览文件。
3.2.5.2 删除文件
下边测试删除文件
参考：https://docs.min.io/docs/java-client-api-reference#removeObject
Java//删除文件
public static void delete(String bucket,String filepath)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
 try {

  minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(bucket).object(filepath).build());
  System.out.println("删除成功");
 } catch (MinioException e) {
  System.out.println("Error occurred: " + e);
  System.out.println("HTTP trace: " + e.httpTrace());
 }

}

 public static void main(String[] args)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
//  upload();
  delete("testbucket","1.mp4");
  delete("testbucket","avi/1.avi");

 }
3.2.5.3 查询文件
通过查询文件查看文件是否存在minio中。
参考：https://docs.min.io/docs/java-client-api-reference#getObject
Java//下载文件
 public static void getFile(String bucket,String filepath,String outFile)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
  try {


   try (InputStream stream = minioClient.getObject(
           GetObjectArgs.builder()
                   .bucket(bucket)
                   .object(filepath)
                   .build());
        FileOutputStream fileOutputStream = new FileOutputStream(new File(outFile));
   ) {

    // Read data from stream
    IOUtils.copy(stream,fileOutputStream);
    System.out.println("下载成功");
   }

  } catch (MinioException e) {
   System.out.println("Error occurred: " + e);
   System.out.println("HTTP trace: " + e.httpTrace());
  }

 }


 public static void main(String[] args)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
  upload();
//  delete("testbucket","1.mp4");
//  delete("testbucket","avi/1.avi");
  getFile("testbucket","avi/1.avi","D:\\develop\\minio_data\\1.avi");
 }
4 上传图片
4.1 需求分析
4.1.1 业务流程
课程图片是宣传课程非常重要的信息，在新增课程界面上传课程图片，也可以修改课程图片。
如下图：

课程图片上传至分布式文件系统，在课程信息中保存课程图片路径，如下流程：

1、前端进入上传图片界面
2、上传图片，请求媒资管理服务。
3、媒资管理服务将图片文件存储在MinIO。
4、媒资管理记录文件信息到数据库。
5、保存课程信息，在内容管理数据库保存图片地址。
媒资管理服务由接口层和业务层共同完成，具体分工如下：
用户上传图片请求至媒资管理的接口层，接口层解析文件信息通过业务层将文件保存至minio及数据库。如下图：

4.1.2 数据模型
涉及到的数据表有：课程信息表中的图片字段、媒资数据库的文件表，下边主要看媒资数据库的文件表。

各字段描述如下：

4.2 准备环境
首先在minio配置bucket，bucket名称为：mediafiles，并设置bucket的权限为公开。

在nacos配置中minio的相关信息，进入media-service-dev.yaml:

配置信息如下：
YAMLminio:
  endpoint: http://192.168.101.65:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucket:
    files: mediafiles
    videofiles: video
在media-service工程编写minio的配置类：
Javapackage com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description minio配置
 * @author Mr.M
 * @date 2022/9/12 19:32
 * @version 1.0
 */
 @Configuration
public class MinioConfig {


 @Value("${minio.endpoint}")
 private String endpoint;
 @Value("${minio.accessKey}")
 private String accessKey;
 @Value("${minio.secretKey}")
 private String secretKey;

 @Bean
 public MinioClient minioClient() {

  MinioClient minioClient =
          MinioClient.builder()
                  .endpoint(endpoint)
                  .credentials(accessKey, secretKey)
                  .build();
  return minioClient;
 }
}
4.3 接口定义
根据需求分析，下边进行接口定义，此接口定义为一个通用的上传文件接口，可以上传图片或其它文件。
首先分析接口：
请求地址：/media/upload/coursefile
请求参数：
Content-Type: multipart/form-data;boundary=.....
FormData:   filedata=??  
响应参数：文件信息，如下
JSON{
  "id": "a16da7a132559daf9e1193166b3e7f52",
  "companyId": 1232141425,
  "companyName": null,
  "filename": "1.jpg",
  "fileType": "001001",
  "tags": "",
  "bucket": "/testbucket/2022/09/12/a16da7a132559daf9e1193166b3e7f52.jpg",
  "fileId": "a16da7a132559daf9e1193166b3e7f52",
  "url": "/testbucket/2022/09/12/a16da7a132559daf9e1193166b3e7f52.jpg",
  "timelength": null,
  "username": null,
  "createDate": "2022-09-12T21:57:18",
  "changeDate": null,
  "status": "1",
  "remark": "",
  "auditStatus": null,
  "auditMind": null,
  "fileSize": 248329
}
定义上传响应模型类：
Javapackage com.xuecheng.media.model.dto;

import com.xuecheng.media.model.po.MediaFiles;
import lombok.Data;
import lombok.ToString;

/**
 * @description 上传普通文件成功响应结果
 * @author Mr.M
 * @date 2022/9/12 18:49
 * @version 1.0
 */
 @Data
public class UploadFileResultDto extends MediaFiles {
  

}
定义接口如下：
Java@ApiOperation("上传文件")
@RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@ResponseBody
public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload,@RequestParam(value = "folder",required=false) String folder,@RequestParam(value = "objectName",required=false) String objectName) throws IOException {

}
4.4 接口开发
4.4.1 DAO开发
根据需求分析DAO层实现向media_files表插入一条记录，使用media_files表生成的mapper即可。
4.4.2 Service开发
Service方法需要提供一个更加通用的保存文件的方法。
定义请求参数类：
Javapackage com.xuecheng.media.model.dto;

import com.xuecheng.media.model.po.MediaFiles;
import lombok.Data;
import lombok.ToString;

/**
 * @description 上传普通文件请求参数
 * @author Mr.M
 * @date 2022/9/12 18:49
 * @version 1.0
 */
 @Data
public class UploadFileParamsDto {

 /**
  * 文件名称
  */
 private String filename;

 /**
  * 文件content-type
 */
 private String contentType;

 /**
  * 文件类型（文档，音频，视频）
  */
 private String fileType;
 /**
  * 文件大小
  */
 private Long fileSize;

 /**
  * 标签
  */
 private String tags;

 /**
  * 上传人
  */
 private String username;

 /**
  * 备注
  */
 private String remark;



}
定义service方法：
Java/**
 * @description 上传文件
 * @param uploadFileParamsDto  上传文件信息
 * @param folder  文件目录,如果不传则默认年、月、日
 * @return com.xuecheng.media.model.dto.UploadFileResultDto 上传文件结果
 * @author Mr.M
 * @date 2022/9/12 19:31
*/
public UploadFileResultDto uploadFile(Long companyId,UploadFileParamsDto uploadFileParamsDto,byte[] bytes,String folder,String objectName);
实现方法如下：
Java@Autowired
MinioClient minioClient;

@Autowired
MediaFilesMapper mediaFilesMapper;

//普通文件桶
@Value("${minio.bucket.files}")
private String bucket_Files;

@Transactional
@Override
public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {

    //生成文件id，文件的md5值
    String fileId = DigestUtils.md5Hex(bytes);
    //文件名称
    String filename = uploadFileParamsDto.getFilename();
    //构造objectname
    if (StringUtils.isEmpty(objectName)) {
        objectName = fileId + filename.substring(filename.lastIndexOf("."));
    }
    if (StringUtils.isEmpty(folder)) {
        //通过日期构造文件存储路径
        folder = getFileFolder(new Date(), true, true, true);
    } else if (folder.indexOf("/") < 0) {
        folder = folder + "/";
    }
    //对象名称
    objectName = folder + objectName;
    MediaFiles mediaFiles = null;
    try {
        //转为流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket_Files).object(objectName)
                //-1表示文件分片按5M(不小于5M,不大于5T),分片数量最大10000，
                .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                .contentType(uploadFileParamsDto.getContentType())
                .build();


        minioClient.putObject(putObjectArgs);
        //从数据库查询文件
        mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket_Files + "/" + objectName);
            mediaFiles.setBucket(bucket_Files);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                XueChengPlusException.cast("保存文件信息失败");
            }
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
        }
    } catch (Exception e) {
        e.printStackTrace();
        XueChengPlusException.cast("上传过程中出错");
    }
    return null;
}


//根据日期拼接目录
private String getFileFolder(Date date, boolean year, boolean month, boolean day){
 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 //获取当前日期字符串
 String dateString = sdf.format(new Date());
 //取出年、月、日
 String[] dateStringArray = dateString.split("-");
 StringBuffer folderString = new StringBuffer();
 if(year){
  folderString.append(dateStringArray[0]);
  folderString.append("/");
 }
 if(month){
  folderString.append(dateStringArray[1]);
  folderString.append("/");
 }
 if(day){
  folderString.append(dateStringArray[2]);
  folderString.append("/");
 }
 return folderString.toString();
}
4.4.3 完善接口层
完善接口层代码 ：
Java@ApiOperation("上传文件")
@RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@ResponseBody
public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload,@RequestParam(value = "folder",required=false) String folder,@RequestParam(value = "objectName",required=false) String objectName) throws IOException {

    String contentType = upload.getContentType();
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileSize(upload.getSize());
        if(contentType.indexOf("image")>=0){
            //图片
            uploadFileParamsDto.setFileType("001001");
        }else{
            //其它
            uploadFileParamsDto.setFileType("001003");
        }

        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setFilename(upload.getOriginalFilename());
        uploadFileParamsDto.setContentType(contentType);
        return mediaFileService.uploadFile(companyId,uploadFileParamsDto,upload.getBytes(),folder,objectName);
}
使用httpclient测试
Java### 上传文件
POST {{media_host}}/media/upload/coursefile
Content-Type: multipart/form-data; boundary=WebAppBoundary

--WebAppBoundary
Content-Disposition: form-data; name="filedata"; filename="1.jpg"
Content-Type: application/octet-stream

< d:/develop/upload/1.jpg
4.4.4 Service代码优化
在上传文件的方法中包括两部分：向MinIO存储文件，向数据库存储文件信息，下边将这两部分抽取出来，后期可供其它Service方法调用。
Java@Transactional
@Override
public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {

    //生成文件id，文件的md5值
    String fileId = DigestUtils.md5Hex(bytes);
    //文件名称
    String filename = uploadFileParamsDto.getFilename();
    //构造objectname
    if (StringUtils.isEmpty(objectName)) {
        objectName = fileId + filename.substring(filename.lastIndexOf("."));
    }
    if (StringUtils.isEmpty(folder)) {
        //通过日期构造文件存储路径
        folder = getFileFolder(new Date(), true, true, true);
    } else if (folder.indexOf("/") < 0) {
        folder = folder + "/";
    }
    //对象名称
    objectName = folder + objectName;
    MediaFiles mediaFiles = null;
    try {
        //上传至文件系统
        addMediaFilesToMinIO(bytes,bucket_Files,objectName,uploadFileParamsDto.getContentType());
        //写入文件表
        mediaFiles = addMediaFilesToDb(companyId,fileId,uploadFileParamsDto,bucket_Files,objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    } catch (Exception e) {
        e.printStackTrace();
        XueChengPlusException.cast("上传过程中出错");
    }
    return null;

}

/**
 * @description 将文件写入minIO
 * @param bytes  文件字节数组
 * @param bucket  桶
 * @param objectName 对象名称
 * @param contentType  内容类型
 * @return void
 * @author Mr.M
 * @date 2022/10/12 21:22
*/
public void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName, String contentType) {
    //转为流
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

    try {
        PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket).object(objectName)
                //-1表示文件分片按5M(不小于5M,不大于5T),分片数量最大10000，
                .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                .contentType(contentType)
                .build();

        minioClient.putObject(putObjectArgs);
    } catch (Exception e) {
       e.printStackTrace();
       XueChengPlusException.cast("上传文件到文件系统出错");
    }
}

/**
 * @description 将文件信息添加到文件表
 * @param companyId  机构id
 * @param fileMd5  文件md5值
 * @param uploadFileParamsDto  上传文件的信息
 * @param bucket  桶
 * @param objectName 对象名称
 * @return com.xuecheng.media.model.po.MediaFiles
 * @author Mr.M
 * @date 2022/10/12 21:22
*/
public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){

    //从数据库查询文件
    MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
    if (mediaFiles == null) {
        mediaFiles = new MediaFiles();
        //拷贝基本信息
        BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
        mediaFiles.setId(fileMd5);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setUrl("/" + bucket + "/" + objectName);
        mediaFiles.setBucket(bucket);
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setAuditStatus("002003");
        mediaFiles.setStatus("1");
        //保存文件信息到文件表
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert < 0) {
            XueChengPlusException.cast("保存文件信息失败");
        }

    }
    return mediaFiles;

}
优化后进行测试。
4.4.4 Service事务优化
上边的service方法优化后并测试通过，现在思考关于uploadFile方法的是否应该开启事务。
目前是在uploadFile方法上添加@Transactional，当调用uploadFile方法前会开启数据库事务，如果上传文件过程时间较长那么数据库的事务持续时间就会变长，这样数据库链接释放就慢，最终导致数据库链接不够用。
我们只将addMediaFilesToDb方法添加事务控制即可,uploadFile方法上的@Transactional注解去掉。
优化后如下：
Java@Transactional
public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){

    //从数据库查询文件
    MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
    if (mediaFiles == null) {
        mediaFiles = new MediaFiles();
        //拷贝基本信息
        BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
        mediaFiles.setId(fileMd5);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setUrl("/" + bucket + "/" + objectName);
        mediaFiles.setBucket(bucket);
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setAuditStatus("002003");
        mediaFiles.setStatus("1");
        //保存文件信息到文件表
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert < 0) {
            XueChengPlusException.cast("保存文件信息失败");
        }

    }
    return mediaFiles;

}
我们人为在int insert = mediaFilesMapper.insert(mediaFiles);下边添加一个异常代码int a=1/0;
测试是否事务控制。
很遗憾，事务控制失败。方法上已经添加了@Transactional注解为什么该方法不能被事务控制呢？
如果是在uploadFile方法上添加@Transactional注解就可以控制事务，去掉则不行。
现在的问题其实是一个非事务方法调同类一个事务方法，事务无法控制，这是为什么？
下边分析原因：
如果在uploadFile方法上添加@Transactional注解，代理对象执行此方法前会开启事务，如下图：

如果在uploadFile方法上没有@Transactional注解，代理对象执行此方法前不进行事务控制，如下图：

现在在addMediaFilesToDb方法上添加@Transactional注解，也不会进行事务是因为并不是通过代理对象执行的addMediaFilesToDb方法。为了判断在uploadFile方法中去调用addMediaFilesToDb方法是否是通过代理对象去调用，我们可以打断点跟踪。

我们发现在uploadFile方法中去调用addMediaFilesToDb方法不是通过代理对象去调用。
如何解决呢？通过代理对象去调用addMediaFilesToDb方法即可解决。
在MediaFileService的实现类中注入MediaFileService的代理对象，如下：
Java@Autowired
MediaFileService currentProxy;
将addMediaFilesToDb方法提成接口。
Java/**
 * @description 将文件信息添加到文件表
 * @param companyId  机构id
 * @param fileMd5  文件md5值
 * @param uploadFileParamsDto  上传文件的信息
 * @param bucket  桶
 * @param objectName 对象名称
 * @return com.xuecheng.media.model.po.MediaFiles
 * @author Mr.M
 * @date 2022/10/12 21:22
 */
@Transactional
public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);
调用addMediaFilesToDb方法的代码处改为如下：
Javatry {
.....
    //写入文件表
    mediaFiles = currentProxy.addMediaFilesToDb(companyId,fileId,uploadFileParamsDto,bucket_Files,objectName);
    ....
再次测试事务是否可以正常控制。
4.5 接口测试
1、首先使用httpclient测试
Java### 上传文件
POST {{media_host}}/media/upload/coursefile
Content-Type: multipart/form-data; boundary=WebAppBoundary

--WebAppBoundary
Content-Disposition: form-data; name="filedata"; filename="1.jpg"
Content-Type: application/octet-stream

< d:/develop/upload/1.jpg
2、再进行前后端联调测试
在新增课程、编辑课程界面上传图，保存课程信息后再次进入编辑课程界面，查看是否可以正常保存课程图片信息。
上图图片完成后，进入媒资管理，查看文件列表中是否有刚刚上传的图片信息。

4.6 bug修复
经过测试发现在媒资列表可以查询刚刚上传的图片信息，但是通过查询条件去查询不起作用。
请查阅代码修复此bug。
5 上传视频
5.1 需求分析
1、教学机构人员进入媒资管理列表查询自己上传的媒资文件。
点击“媒资管理”

进入媒资管理列表页面查询本机构上传的媒资文件。

2、教育机构用户在"媒资管理"页面中点击 "上传视频" 按钮。

点击“上传视频”打开上传页面

3、选择要上传的文件，自动执行文件上传。

4、视频上传成功会自动处理，处理完成可以预览视频。

5.2 理解断点续传
5.2.1 什么是断点续传
通常视频文件都比较大，所以对于媒资系统上传文件的需求要满足大文件的上传要求。http协议本身对上传文件大小没有限制，但是客户的网络环境质量、电脑硬件环境等参差不齐，如果一个大文件快上传完了网断了没有上传完成，需要客户重新上传，用户体验非常差，所以对于大文件上传的要求最基本的是断点续传。
什么是断点续传：
        引用百度百科：断点续传指的是在下载或上传时，将下载或上传任务（一个文件或一个压缩包）人为的划分为几个部分，每一个部分采用一个线程进行上传或下载，如果碰到网络故障，可以从已经上传或下载的部分开始继续上传下载未完成的部分，而没有必要从头开始上传下载，断点续传可以提高节省操作时间，提高用户体验性。
断点续传流程如下图：

流程如下：
1、前端上传前先把文件分成块
2、一块一块的上传，上传中断后重新上传，已上传的分块则不用再上传
3、各分块上传完成最后在服务端合并文件
5.2.2 分块与合并测试
为了更好的理解文件分块上传的原理，下边用java代码测试文件的分块与合并。
文件分块的流程如下：
1、获取源文件长度
2、根据设定的分块文件的大小计算出块数
3、从源文件读数据依次向每一个块文件写数据。
测试代码如下：
Javapackage com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description 大文件处理测试
 * @date 2022/9/13 9:21
 */
public class BigFileTest {


    //测试文件分块方法
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("d:/develop/bigfile_test/nacos.avi");
        String chunkPath = "d:/develop/bigfile_test/chunk/";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        //分块大小
        long chunkSize = 1024 * 1024 * 1;
        //分块数量
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分块总数："+chunkNum);
        //缓冲区大小
        byte[] b = new byte[1024];
        //使用RandomAccessFile访问文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //分块
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File file = new File(chunkPath + i);
            if(file.exists()){
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                //向分块文件中写数据
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                    if (file.length() > chunkSize) {
                        break;
                    }
                }
                raf_write.close();
                System.out.println("完成分块"+i);
            }

        }
        raf_read.close();

    }


   

}
文件合并流程：
1、找到要合并的文件并按文件合并的先后进行排序。
2、创建合并文件
3、依次从合并的文件中读取数据向合并文件写入数
文件合并的测试代码 ：
Java //测试文件合并方法
    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFolder = new File("d:/develop/bigfile_test/chunk/");
        //原始文件
        File originalFile = new File("d:/develop/bigfile_test/nacos.avi");
        //合并文件
        File mergeFile = new File("d:/develop/bigfile_test/nacos01.avi");
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        //创建新的合并文件
        mergeFile.createNewFile();
        //用于写文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //指针指向文件顶端
        raf_write.seek(0);
        //缓冲区
        byte[] b = new byte[1024];
        //分块列表
        File[] fileArray = chunkFolder.listFiles();
        // 转成集合，便于排序
        List<File> fileList = new ArrayList<File>(Arrays.asList(fileArray));
        // 从小到大排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        //合并文件
        for (File chunkFile : fileList) {
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_read.read(b)) != -1) {
                raf_write.write(b, 0, len);

            }
            raf_read.close();
        }
        raf_write.close();

        //校验文件
        try (

                FileInputStream fileInputStream = new FileInputStream(originalFile);
                FileInputStream mergeFileStream = new FileInputStream(mergeFile);


        ) {
            //取出原始文件的md5
            String originalMd5 = DigestUtils.md5Hex(fileInputStream);
            //取出合并文件的md5进行比较
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileStream);
            if (originalMd5.equals(mergeFileMd5)) {
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }

        }


    }
5.2.3 上传视频流程
下图是上传视频的整体流程：

1、前端上传文件前请求媒资接口层检查文件是否存在，如果已经存在则不再上传。
2、如果文件在系统不存在前端开始上传，首先对视频文件进行分块
3、前端分块进行上传，上传前首先检查分块是否上传，如已上传则不再上传，如果未上传则开始上传分块。
4、前端请求媒资管理接口层请求上传分块。
5、接口层请求服务层上传分块。
6、服务端将分块信息上传到MinIO。
7、前端将分块上传完毕请求接口层合并分块。
8、接口层请求服务层合并分块。
9、服务层根据文件信息找到MinIO中的分块文件，下载到本地临时目录，将所有分块下载完毕后开始合并 。
10、合并完成将合并后的文件上传到MinIO。
5.3 接口定义
根据上传视频流程，定义如下接口。
Javapackage com.xuecheng.media.api;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Mr.M
 * @version 1.0
 * @description 大文件上传接口
 * @date 2022/9/6 11:29
 */
@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
public class BigFilesController {

    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(
            @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
       
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {

        
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        

    }


}
RestResponse 是一个通用的模型类，在base工程定义如下，从课程资料中拷贝RestResponse.java类到base工程下的model包下。
Javapackage com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * @description 通用结果类型
 * @author Mr.M
 * @date 2022/9/13 14:44
 * @version 1.0
 */

 @Data
 @ToString
public class RestResponse<T> {

  /**
   * 响应编码,0为正常,-1错误
   */
  private int code;

  /**
   * 响应提示信息
   */
  private String msg;

  /**
   * 响应内容
   */
  private T result;


  public RestResponse() {
   this(0, "success");
  }

  public RestResponse(int code, String msg) {
   this.code = code;
   this.msg = msg;
  }

  /**
   * 错误信息的封装
   *
   * @param msg
   * @param <T>
   * @return
   */
  public static <T> RestResponse<T> validfail(String msg) {
   RestResponse<T> response = new RestResponse<T>();
   response.setCode(-1);
   response.setMsg(msg);
   return response;
  }



  /**
   * 添加正常响应数据（包含响应内容）
   *
   * @return RestResponse Rest服务封装相应数据
   */
  public static <T> RestResponse<T> success(T result) {
   RestResponse<T> response = new RestResponse<T>();
   response.setResult(result);
   return response;
  }

  /**
   * 添加正常响应数据（不包含响应内容）
   *
   * @return RestResponse Rest服务封装相应数据
   */
  public static <T> RestResponse<T> success() {
   return new RestResponse<T>();
  }


  public Boolean isSuccessful() {
   return this.code == 0;
  }

 }
5.4 接口开发
5.4.1 DAO开发
向媒资数据库的文件表插入记录，使用自动生成的Mapper接口即可满足要求。
5.4.2 Service开发
5.4.2.1 接口定义
首先定义接口，从课程资料中拷贝RestResponse.java类到base工程下的model包下。
1、检查文件方法
检查文件是否在系统存在
Java/**
 * @description 检查文件是否存在
 * @param fileMd5 文件的md5
 * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
 * @author Mr.M
 * @date 2022/9/13 15:38
*/
public RestResponse<Boolean> checkFile(String fileMd5);
2、检查分块是否存在
Java/**
 * @description 检查分块是否存在
 * @param fileMd5  文件的md5
 * @param chunkIndex  分块序号
 * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
 * @author Mr.M
 * @date 2022/9/13 15:39
*/
public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);
3、上传分块
Java/**
 * @description 上传分块
 * @param fileMd5  文件md5
 * @param chunk  分块序号
 * @param bytes  文件字节
 * @return com.xuecheng.base.model.RestResponse
 * @author Mr.M
 * @date 2022/9/13 15:50
*/
public RestResponse uploadChunk(String fileMd5,int chunk,byte[] bytes);
4、合并分块
Java/**
 * @description 合并分块
 * @param companyId  机构id
 * @param fileMd5  文件md5
 * @param chunkTotal 分块总和
 * @param uploadFileParamsDto 文件信息
 * @return com.xuecheng.base.model.RestResponse
 * @author Mr.M
 * @date 2022/9/13 15:56
*/
public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);
5.4.2.2 检查文件和分块
接口完成进行接口实现，首先实现检查文件方法和检查分块方法。
service接口定义
Java/**
 * @description 检查文件是否存在
 * @param fileMd5 文件的md5
 * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
 * @author Mr.M
 * @date 2022/9/13 15:38
*/
public RestResponse<Boolean> checkFile(String fileMd5);

/**
 * @description 检查分块是否存在
 * @param fileMd5  文件的md5
 * @param chunkIndex  分块序号
 * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
 * @author Mr.M
 * @date 2022/9/13 15:39
*/
public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);
service接口实现方法：
Java@Override
public RestResponse<Boolean> checkFile(String fileMd5) {
    //查询文件信息
    MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
    if (mediaFiles != null) {
        //桶
        String bucket = mediaFiles.getBucket();
        //存储目录
        String filePath = mediaFiles.getFilePath();
        //文件流
        InputStream stream = null;
        try {
            stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filePath)
                            .build());

            if (stream != null) {
                //文件已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
           
        }
    }
    //文件不存在
    return RestResponse.success(false);
}



@Override
public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

    //得到分块文件目录
    String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
    //得到分块文件的路径
    String chunkFilePath = chunkFileFolderPath + chunkIndex;

    //文件流
    InputStream fileInputStream = null;
    try {
        fileInputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket_videoFiles)
                        .object(chunkFilePath)
                        .build());

        if (fileInputStream != null) {
            //分块已存在
            return RestResponse.success(true);
        }
    } catch (Exception e) {
        
    }
    //分块未存在
    return RestResponse.success(false);
}

//得到分块文件的目录
private String getChunkFileFolderPath(String fileMd5) {
    return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
}
5.4.2.3 上传分块
定义service接口
Java/**
 * @description 上传分块
 * @param fileMd5  文件md5
 * @param chunk  分块序号
 * @param bytes  文件字节
 * @return com.xuecheng.base.model.RestResponse
 * @author Mr.M
 * @date 2022/9/13 15:50
*/
public RestResponse uploadChunk(String fileMd5,int chunk,byte[] bytes);
接口实现：
Java@Override
public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {


    //得到分块文件的目录路径
    String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
    //得到分块文件的路径
    String chunkFilePath = chunkFileFolderPath + chunk;

    try {
    //将文件存储至minIO
    addMediaFilesToMinIO(bytes, bucket_videoFiles,chunkFilePath,"application/octet-stream");

} catch (Exception ex) {
    ex.printStackTrace();
    XueChengPlusException.cast("上传过程出错请重试");
}
    return RestResponse.success();
}
5.4.2.4 下载分块
合并分块前要检查分块文件是否全部上传完成，如果完成则将已经上传的分块文件下载下来，然后再进行合并，下边先实现检查及下载所有分块的方法。
Java//检查所有分块是否上传完毕
private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
    //得到分块文件的目录路径
    String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
    File[] files = new File[chunkTotal];
    //检查分块文件是否上传完毕
    for (int i = 0; i < chunkTotal; i++) {
        String chunkFilePath = chunkFileFolderPath + i;
        //下载文件
        File chunkFile =null;
        try {
            chunkFile = File.createTempFile("chunk" + i, null);
        } catch (IOException e) {
            e.printStackTrace();
            XueChengPlusException.cast("下载分块时创建临时文件出错");
        }
        downloadFileFromMinIO(chunkFile,bucket_videoFiles,chunkFilePath);
        files[i]=chunkFile;
    }
    return files;
}
//得到分块文件的目录
private String getChunkFileFolderPath(String fileMd5) {
    return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
}
//根据桶和文件路径从minio下载文件
public File downloadFileFromMinIO(File file,String bucket,String objectName){
    InputStream fileInputStream = null;
    OutputStream fileOutputStream = null;
    try {
        fileInputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build());
        try {
            fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(fileInputStream, fileOutputStream);

        } catch (IOException e) {
            XueChengPlusException.cast("下载文件"+objectName+"出错");
        }
    } catch (Exception e) {
        e.printStackTrace();
        XueChengPlusException.cast("文件不存在"+objectName);
    } finally {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    return file;
}
5.4.2.5 合并分块
所有分块文件下载成功后开始合并这些分块文件。
Java@Override
public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {

    String fileName = uploadFileParamsDto.getFilename();
    //下载所有分块文件
    File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
    //扩展名
    String extName = fileName.substring(fileName.lastIndexOf("."));
    //创建临时文件作为合并文件
    File mergeFile = null;
    try {
        mergeFile = File.createTempFile(fileMd5, extName);
    } catch (IOException e) {
        XueChengPlusException.cast("合并文件过程中创建临时文件出错");
    }

    try {
        //开始合并
        byte[] b = new byte[1024];
        try(RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");) {
            for (File chunkFile : chunkFiles) {
                try (FileInputStream chunkFileStream = new FileInputStream(chunkFile);) {
                    int len = -1;
                    while ((len = chunkFileStream.read(b)) != -1) {
                        //向合并后的文件写
                        raf_write.write(b, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            XueChengPlusException.cast("合并文件过程中出错");
        }
        log.debug("合并文件完成{}",mergeFile.getAbsolutePath());
        uploadFileParamsDto.setFileSize(mergeFile.length());

        try (InputStream mergeFileInputStream = new FileInputStream(mergeFile);) {
            //对文件进行校验，通过比较md5值
            String newFileMd5 = DigestUtils.md5Hex(mergeFileInputStream);
            if (!fileMd5.equalsIgnoreCase(newFileMd5)) {
                //校验失败
                XueChengPlusException.cast("合并文件校验失败");
            }
            log.debug("合并文件校验通过{}",mergeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            //校验失败
            XueChengPlusException.cast("合并文件校验异常");
        }

        //将临时文件上传至minio
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);
        try {

            //上传文件到minIO
            addMediaFilesToMinIO(mergeFile.getAbsolutePath(), bucket_videoFiles, mergeFilePath);
            log.debug("合并文件上传MinIO完成{}",mergeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("合并文件时上传文件出错");
        }

        //入数据库
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videoFiles, mergeFilePath);
        if (mediaFiles == null) {
            XueChengPlusException.cast("媒资文件入库出错");
        }

        return RestResponse.success();
    } finally {
        //删除临时文件
        for (File file : chunkFiles) {
            try {
                file.delete();
            } catch (Exception e) {

            }
        }
        try {
            mergeFile.delete();
        } catch (Exception e) {

        }
    }
}


 private String getFilePathByMd5(String fileMd5,String fileExt){
    return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
}


//将文件上传到minIO，传入文件绝对路径
public void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
    try {
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .filename(filePath)
                        .build());
    } catch (Exception e) {
        e.printStackTrace();
        XueChengPlusException.cast("上传文件到文件系统出错");
    }
}
5.4.3 接口层完善
下边完善接口层
Java@ApiOperation(value = "文件上传前检查文件")
@PostMapping("/upload/checkfile")
public RestResponse<Boolean> checkfile(
        @RequestParam("fileMd5") String fileMd5
) throws Exception {
    return mediaFileService.checkFile(fileMd5);
}


@ApiOperation(value = "分块文件上传前的检测")
@PostMapping("/upload/checkchunk")
public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                        @RequestParam("chunk") int chunk) throws Exception {
    return mediaFileService.checkChunk(fileMd5,chunk);
}

@ApiOperation(value = "上传分块文件")
@PostMapping("/upload/uploadchunk")
public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                @RequestParam("fileMd5") String fileMd5,
                                @RequestParam("chunk") int chunk) throws Exception {

    return mediaFileService.uploadChunk(fileMd5,chunk,file.getBytes());
}

@ApiOperation(value = "合并文件")
@PostMapping("/upload/mergechunks")
public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                @RequestParam("fileName") String fileName,
                                @RequestParam("chunkTotal") int chunkTotal) throws Exception {
    Long companyId = 1232141425L;

    UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
    uploadFileParamsDto.setFileType("001002");
    uploadFileParamsDto.setTags("课程视频");
    uploadFileParamsDto.setRemark("");
    uploadFileParamsDto.setFilename(fileName);
    ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(fileName);
    String mimeType = extensionMatch.getMimeType();
    uploadFileParamsDto.setContentType(mimeType);

    return mediaFileService.mergechunks(companyId,fileMd5,chunkTotal,uploadFileParamsDto);

}
5.5 接口测试
如果是单个接口测试使用httpclient
Java### 检查文件
POST{{media_host}}/media/upload/register
Content-Type: application/x-www-form-urlencoded;

fileMd5=c5c75d70f382e6016d2f506d134eee11

### 上传分块前检查
POST {{media_host}}/media/upload/checkchunk
Content-Type: application/x-www-form-urlencoded;

fileMd5=c5c75d70f382e6016d2f506d134eee11&chunk=0

### 上传分块文件
POST {{media_host}}/media/upload/uploadchunk?fileMd5=c5c75d70f382e6016d2f506d134eee11&chunk=1
Content-Type: multipart/form-data; boundary=WebAppBoundary

--WebAppBoundaryContent-Disposition: form-data; name="file"; filename="1"
Content-Type: application/octet-stream

< E:/ffmpeg_test/chunks/1



### 合并文件
POST {{media_host}}/media/upload/mergechunks
Content-Type: application/x-www-form-urlencoded;

fileMd5=dcb37b85c9c03fc5243e20ab4dfbc1c8&fileName=8.avi&chunkTotal=1
下边介绍采用前后联调：
1、首先在每个接口层方法上打开断点
在前端上传视频，观察接口层是否收到参数。
2、进入service方法逐行跟踪。
3、断点续传测试
上传一部分后，停止刷新浏览器再重新上传，通过浏览器日志发现已经上传过的分块不再重新上传

6 文件预览
6.1 需求分析
图片上传成功、视频上传成功可以通过预览功能查看文件的内容。
预览的方式是通过浏览器直接打文件，对于图片和浏览器支持的视频格式的视频文件可以直接预览。

业务流程如下：

说明如下：
1、前端请求接口层预览文件
2、接口层将文件id传递给服务层
3、服务层使用文件id查询媒资数据库文件表，获取文件的url
4、接口层将文件url返回给前端，通过浏览器打开URL。
6.2 接口定义
根据需求分析定义接口如下：
Java @ApiOperation("预览文件")
@GetMapping("/preview/{mediaId}")
public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId){

}
6.3 接口开发
6.3.1 DAO开发
使用自动生成的MediaFiels表的Mapper接口。
6.3.2 Service开发
定义根据id查询媒资文件接口
Java/**
 * @description 根据id查询文件信息
 * @param id  文件id
 * @return com.xuecheng.media.model.po.MediaFiles 文件信息
 * @author Mr.M
 * @date 2022/9/13 17:47
*/
public MediaFiles getFileById(String id);
方法实现：
Javapublic MediaFiles getFileById(String id) {
    return mediaFilesMapper.selectById(id);

}
6.3.3 接口层完善
对接口层完善：
Java@ApiOperation("预览文件")
@GetMapping("/preview/{mediaId}")
public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId){


    MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
    if(mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())){
        XueChengPlusException.cast("视频还没有转码处理");
    }
    return RestResponse.success(mediaFiles.getUrl());

}
6.3.4 接口测试
使用前后端联调。
上传mp4视频文件，预览文件。
上传图片文件，预览文件。
对于无法预览的视频文件，稍后通过视频处理对视频转码。
7 视频处理
7.1 视频编码技术
7.1.1 什么是视频编码
视频上传成功后需要对视频进行转码处理。
什么是视频编码？查阅百度百科如下：

详情参考 ：https://baike.baidu.com/item/%E8%A7%86%E9%A2%91%E7%BC%96%E7%A0%81/839038
首先我们要分清文件格式和编码格式：
文件格式：是指.mp4、.avi、.rmvb等 这些不同扩展名的视频文件的文件格式  ，视频文件的内容主要包括视频和音频，其文件格式是按照一 定的编码格式去编码，并且按照该文件所规定的封装格式将视频、音频、字幕等信息封装在一起，播放器会根据它们的封装格式去提取出编码，然后由播放器解码，最终播放音视频。
音视频编码格式：通过音视频的压缩技术，将视频格式转换成另一种视频格式，通过视频编码实现流媒体的传输。比如：一个.avi的视频文件原来的编码是a，通过编码后编码格式变为b，音频原来为c，通过编码后变为d。
音视频编码格式各类繁多，主要有几下几类：
MPEG系列
（由ISO[国际标准组织机构]下属的MPEG[运动图象专家组]开发 ）视频编码方面主要是Mpeg1（vcd用的就是它）、Mpeg2（DVD使用）、Mpeg4（的DVDRIP使用的都是它的变种，如：divx，xvid等）、Mpeg4 AVC（正热门）；音频编码方面主要是MPEG Audio Layer 1/2、MPEG Audio Layer 3（大名鼎鼎的mp3）、MPEG-2 AAC 、MPEG-4 AAC等等。注意：DVD音频没有采用Mpeg的。
H.26X系列
（由ITU[国际电传视讯联盟]主导，侧重网络传输，注意：只是视频编码）
包括H.261、H.262、H.263、H.263+、H.263++、H.264（就是MPEG4 AVC-合作的结晶）
目前最常用的编码标准是视频H.264，音频AAC。
提问：
H.264是编码格式还是文件格式？
mp4是编码格式还是文件格式？
7.1.2 FFmpeg 的基本使用
我们将视频录制完成后，使用视频编码软件对视频进行编码，本项目 使用FFmpeg对视频进行编码 。

FFmpeg被许多开源项目采用，QQ影音、暴风影音、VLC等。
下载：FFmpeg https://www.ffmpeg.org/download.html#build-windows
请从课程资料目录解压ffmpeg.zip,并将解压得到的exe文件加入环境变量。
测试是否正常：cmd运行 ffmpeg -v

安装成功，作下简单测试
将一个.avi文件转成mp4、mp3、gif等。
比如我们将nacos.avi文件转成mp4，运行如下命令：
ffmpeg -i nacos.avi nacos.mp4
转成mp3：ffmpeg -i nacos.avi nacos.mp3
转成gif：ffmpeg -i nacos.avi nacos.gif
官方文档（英文）：http://ffmpeg.org/ffmpeg.html
7.2 分布式任务处理
7.2.1 什么是分布式任务调度
了解了视频编码的相关知识，如何用Java程序对视频进行处理呢？这里有一个关键的需求就是当视频比较多的时候我们程序可以高效处理。
如何去高效处理一批任务呢？
1、多线程
多线程是充分利用单机的资源。
2、分布式加多线程
充分利用我台计算机，每台计算机多线程处理。
方案2可扩展性更强。
方案2是一种分布式任务调度的处理方案。
什么是分布式任务调度？
我们可以先思考一下下面业务场景的解决方案：
        某电商系统需要在每天上午10点，下午3点，晚上8点发放一批优惠券。
        某财务系统需要在每天上午10点前结算前一天的账单数据，统计汇总。
        某电商平台每天凌晨3点，要对订单中的无效订单进行清理。
        12306网站会根据车次不同，设置几个时间点分批次放票。
        电商整点抢购，商品价格某天上午8点整开始优惠。
        商品成功发货后，需要向客户发送短信提醒。
类似的场景还有很多，我们该如何实现？
以上这些场景，就是任务调度所需要解决的问题。
任务调度顾名思义，就是对任务的调度，它是指系统为了完成特定业务，基于给定时间点，给定时间间隔或者给定执行次数自动执行任务。
如何实现任务调度？
多线程方式实现：
学过多线程的同学，可能会想到，我们可以开启一个线程，每sleep一段时间，就去检查是否已到预期执行时间。
以下代码简单实现了任务调度的功能：
Javapublic static void main(String[] args) {    
    //任务执行间隔时间
    final long timeInterval = 1000;
    Runnable runnable = new Runnable() {
        public void run() {
            while (true) {
                //TODO：something
                try {
                    Thread.sleep(timeInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    Thread thread = new Thread(runnable);
    thread.start();
}
上面的代码实现了按一定的间隔时间执行任务调度的功能。
Jdk也为我们提供了相关支持，如Timer、ScheduledExecutor，下边我们了解下。
Timer方式实现：
Javapublic static void main(String[] args){  
    Timer timer = new Timer();  
    timer.schedule(new TimerTask(){
        @Override  
        public void run() {  
           //TODO：something
        }  
    }, 1000, 2000);  //1秒后开始调度，每2秒执行一次
}
        Timer 的优点在于简单易用，每个Timer对应一个线程，因此可以同时启动多个Timer并行执行多个任务，同一个Timer中的任务是串行执行。
ScheduledExecutor方式实现：
Javapublic static void main(String [] agrs){
    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    service.scheduleAtFixedRate(
            new Runnable() {
                @Override
                public void run() {
                    //TODO：something
                    System.out.println("todo something");
                }
            }, 1,
            2, TimeUnit.SECONDS);
}
        Java 5 推出了基于线程池设计的 ScheduledExecutor，其设计思想是，每一个被调度的任务都会由线程池中一个线程去执行，因此任务是并发执行的，相互之间不会受到干扰。
        Timer 和 ScheduledExecutor 都仅能提供基于开始时间与重复间隔的任务调度，不能胜任更加复杂的调度需求。比如，设置每月第一天凌晨1点执行任务、复杂调度任务的管理、任务间传递数据等等。
        Quartz 是一个功能强大的任务调度框架，它可以满足更多更复杂的调度需求，Quartz 设计的核心类包括 Scheduler, Job 以及 Trigger。其中，Job 负责定义需要执行的任务，Trigger 负责设置调度策略，Scheduler 将二者组装在一起，并触发任务开始执行。Quartz支持简单的按时间间隔调度、还支持按日历调度方式，通过设置CronTrigger表达式（包括：秒、分、时、日、月、周、年）进行任务调度。
第三方Quartz方式实现：
Javapublic static void main(String [] agrs) throws SchedulerException {
    //创建一个Scheduler
    SchedulerFactory schedulerFactory = new StdSchedulerFactory();
    Scheduler scheduler = schedulerFactory.getScheduler();
    //创建JobDetail
    JobBuilder jobDetailBuilder = JobBuilder.newJob(MyJob.class);
    jobDetailBuilder.withIdentity("jobName","jobGroupName");
    JobDetail jobDetail = jobDetailBuilder.build();
    //创建触发的CronTrigger 支持按日历调度
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("triggerName", "triggerGroupName")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?"))
                .build();
        //创建触发的SimpleTrigger 简单的间隔调度
        /*SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("triggerName","triggerGroupName")
                .startNow()
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInSeconds(2)
                        .repeatForever())
                .build();*/
    scheduler.scheduleJob(jobDetail,trigger);
    scheduler.start();
}

public class MyJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        System.out.println("todo something");
    }
}
通过以上内容我们学习了什么是任务调度，任务调度所解决的问题，以及任务调度的多种实现方式。
什么是分布式任务调度？
        通常任务调度的程序是集成在应用中的，比如：优惠卷服务中包括了定时发放优惠卷的的调度程序，结算服务中包括了定期生成报表的任务调度程序，由于采用分布式架构，一个服务往往会部署多个冗余实例来运行我们的业务，在这种分布式系统环境下运行任务调度，我们称之为分布式任务调度，如下图：

分布式调度要实现的目标：
        不管是任务调度程序集成在应用程序中，还是单独构建的任务调度系统，如果采用分布式调度任务的方式就相当于将任务调度程序分布式构建，这样就可以具有分布式系统的特点，并且提高任务的调度处理能力：
1、并行任务调度
        并行任务调度实现靠多线程，如果有大量任务需要调度，此时光靠多线程就会有瓶颈了，因为一台计算机CPU的处理能力是有限的。
        如果将任务调度程序分布式部署，每个结点还可以部署为集群，这样就可以让多台计算机共同去完成任务调度，我们可以将任务分割为若干个分片，由不同的实例并行执行，来提高任务调度的处理效率。
2、高可用
        若某一个实例宕机，不影响其他实例来执行任务。
3、弹性扩容
        当集群中增加实例就可以提高并执行任务的处理效率。
4、任务管理与监测
        对系统中存在的所有定时任务进行统一的管理及监测。让开发人员及运维人员能够时刻了解任务执行情况，从而做出快速的应急处理响应。
5、避免任务重复执行
        当任务调度以集群方式部署，同一个任务调度可能会执行多次，比如在上面提到的电商系统中到点发优惠券的例子，就会发放多次优惠券，对公司造成很多损失，所以我们需要控制相同的任务在多个运行实例上只执行一次。
7.2.2 XXL-JOB介绍
XXL-JOB是一个轻量级分布式任务调度平台，其核心设计目标是开发迅速、学习简单、轻量级、易扩展。现已开放源代码并接入多家公司线上产品线，开箱即用。
官网：https://www.xuxueli.com/xxl-job/
文档：https://www.xuxueli.com/xxl-job/#%E3%80%8A%E5%88%86%E5%B8%83%E5%BC%8F%E4%BB%BB%E5%8A%A1%E8%B0%83%E5%BA%A6%E5%B9%B3%E5%8F%B0XXL-JOB%E3%80%8B
XXL-JOB主要有调度中心、执行器、任务：

调度中心：
        负责管理调度信息，按照调度配置发出调度请求，自身不承担业务代码；
        主要职责为执行器管理、任务管理、监控运维、日志管理等
任务执行器：
        负责接收调度请求并执行任务逻辑；
        只要职责是注册服务、任务执行服务（接收到任务后会放入线程池中的任务队列）、执行结果上报、日志服务等
任务：负责执行具体的业务处理。
调度中心与执行器之间的工作流程如下：

执行流程：
        1.任务执行器根据配置的调度中心的地址，自动注册到调度中心
        2.达到任务触发条件，调度中心下发任务
        3.执行器基于线程池执行任务，并把执行结果放入内存队列中、把执行日志写入日志文件中
        4.执行器消费内存队列中的执行结果，主动上报给调度中心
        5.当用户在调度中心查看任务日志，调度中心请求任务执行器，任务执行器读取任务日志文件并返回日志详情
7.2.3 搭建XXL-JOB
7.2.3.1 调度中心
首先下载XXL-JOB
GitHub：https://github.com/xuxueli/xxl-job
码云：https://gitee.com/xuxueli0323/xxl-job
项目使用2.3.1版本： https://github.com/xuxueli/xxl-job/releases/tag/2.3.1
也可从课程资料目录获取，解压xxl-job-2.3.1.zip
使用IDEA打开解压后的目录

xxl-job-admin：调度中心
xxl-job-core：公共依赖
xxl-job-executor-samples：执行器Sample示例（选择合适的版本执行器，可直接使用）
    ：xxl-job-executor-sample-springboot：Springboot版本，通过Springboot管理执行器，推荐这种方式；
    ：xxl-job-executor-sample-frameless：无框架版本；
doc :文档资料，包含数据库脚本
创建数据库：xxl_job_2.3.1

首先修改doc下的tables_xxl_job.sql脚本内容：
JavaCREATE database if NOT EXISTS `xxl_job_2.3.1` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `xxl_job_2.3.1`;
将tables_xxl_job.sql脚本导入xxl_job_2.3.1数据库，导入成功，刷新表，如下图：

修改xxl-job-admin任务调度中心下application.properties的配置文件内容，修改数据库链接地址：
Javaspring.datasource.url=jdbc:mysql://192.168.101.65:3306/xxl_job_2.3.1?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=mysql
然后启动xxl-job-admin任务调度中心，运行com.xxl.job.admin.XxlJobAdminApplication
启动成功访问http://localhost:8080/xxl-job-admin
账号和密码：admin/ 123456

到此调度中心启动成功。
在课前下发的虚拟机中已经创建的xxl-job调度中心的容器，后边调用使用docker容器运行xxl-job。
启动docker容器：docker start xxl-job-admin
访问：http://192.168.101.65:8088/xxl-job-admin/
账号和密码：admin/123456
7.2.3.2 执行器
下边配置执行器，执行器负责与调度中心通信接收调度中心发起的任务调度请求。
1、首先在媒资管理模块的service工程添加依赖，在项目的父工程已约定了版本2.3.1
XML<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-job-core</artifactId>
</dependency>
2、在nacos下的media-service-dev.yaml下配置xxl-job
YAMLxxl:
  job:
    admin: 
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: media-process-service
      address: 
      ip: 
      port: 9999
      logpath: /data/applogs/xxl-job/jobhandler
      logretentiondays: 30
    accessToken: default_token
注意配置中的appname这是执行器的应用名，稍后在调度中心配置执行器时要使用。
3、配置xxl-job的执行器
将示例工程下配置类拷贝到媒资管理的service工程下

拷贝至：

4、下边进入调度中心添加执行器

点击新增，填写执行器信息，appname是前边在nacos中配置xxl信息时指定的执行器的应用名。

添加成功：

到此完成媒资管理模块service工程配置xxl-job执行器，在xxl-job调度中心添加执行器，下边准备测试执行器与调度中心是否正常通信，因为接口工程依赖了service工程，所以启动媒资管理模块的接口工程。
启动后观察日志，出现下边的日志表示执行器在调度中心注册成功

同时观察调度中心中的执行器界面

在线机器地址处已显示1个执行器。
7.2.3.3 执行任务
下边编写任务，任务类的编写方法参考示例工程，如下图：

在service包下新建jobhandler存放任务类，下边参考示例工程编写一个任务类
Javapackage com.xuecheng.media.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @description 测试执行器
 * @author Mr.M
 * @date 2022/9/13 20:32
 * @version 1.0
 */
 @Component
 @Slf4j
public class SampleJob {

 /**
  * 1、简单任务示例（Bean模式）
  */
 @XxlJob("testJob")
 public void testJob() throws Exception {
  log.info("开始执行.....");

 }

}
下边在调度中心添加任务，进入任务管理

点击新增，填写任务信息

注意红色标记处：
调度类型选择Cron，并配置Cron表达式设置定时策略。
运行模式有BEAN和GLUE，bean模式较常用就是在项目工程中编写执行器的任务代码，GLUE是将任务代码编写在调度中心。
JobHandler任务方法名填写@XxlJob注解中的名称。
添加成功，启动任务

通过调度日志查看任务执行情况

下边启动媒资管理的service工程，启动执行器。
观察执行器方法的执行。

如果要停止任务需要在调度中心操作

任务跑一段时间注意清理日志

7.2.5 分片广播
掌握了xxl-job的基本使用，下边思考如何进行分布式任务处理呢？如下图，我们会启动多个执行器组成一个集群，去执行任务。

执行器在集群部署下调度中心有哪些调度策略呢？
查看xxl-job官方文档，阅读高级配置相关的内容：
SQL高级配置：
    - 路由策略：当执行器集群部署时，提供丰富的路由策略，包括；
        FIRST（第一个）：固定选择第一个机器；
        LAST（最后一个）：固定选择最后一个机器；
        ROUND（轮询）：；
        RANDOM（随机）：随机选择在线的机器；
        CONSISTENT_HASH（一致性HASH）：每个任务按照Hash算法固定选择某一台机器，且所有任务均匀散列在不同机器上。
        LEAST_FREQUENTLY_USED（最不经常使用）：使用频率最低的机器优先被选举；
        LEAST_RECENTLY_USED（最近最久未使用）：最久未使用的机器优先被选举；
        FAILOVER（故障转移）：按照顺序依次进行心跳检测，第一个心跳检测成功的机器选定为目标执行器并发起调度；
        BUSYOVER（忙碌转移）：按照顺序依次进行空闲检测，第一个空闲检测成功的机器选定为目标执行器并发起调度；
        SHARDING_BROADCAST(分片广播)：广播触发对应集群中所有机器执行一次任务，同时系统自动传递分片参数；可根据分片参数开发分片任务；
    - 子任务：每个任务都拥有一个唯一的任务ID(任务ID可以从任务列表获取)，当本任务执行结束并且执行成功时，将会触发子任务ID所对应的任务的一次主动调度。
    - 调度过期策略：
        - 忽略：调度过期后，忽略过期的任务，从当前时间开始重新计算下次触发时间；
        - 立即执行一次：调度过期后，立即执行一次，并从当前时间开始重新计算下次触发时间；
    - 阻塞处理策略：调度过于密集执行器来不及处理时的处理策略；
        单机串行（默认）：调度请求进入单机执行器后，调度请求进入FIFO队列并以串行方式运行；
        丢弃后续调度：调度请求进入单机执行器后，发现执行器存在运行的调度任务，本次请求将会被丢弃并标记为失败；
        覆盖之前调度：调度请求进入单机执行器后，发现执行器存在运行的调度任务，将会终止运行中的调度任务并清空队列，然后运行本地调度任务；
    - 任务超时时间：支持自定义任务超时时间，任务运行超时将会主动中断任务；
    - 失败重试次数；支持自定义任务失败重试次数，当任务失败时将会按照预设的失败重试次数主动进行重试；
第一个：每次调度选择集群中第一台执行器。
最后一个：每次调度选择集群中最后一台执行器。
轮询：按照顺序每次调度选择一台执行器去调度。
随机：每次调度随机选择一台执行器去调度。
CONSISTENT_HASH：按任务的hash值选择一台执行器去调度。
其它策略请自行阅读文档。
下边要重点说的是分片广播策略，分片是指是调度中心将集群中的执行器标上序号：0，1，2，3...，广播是指每次调度会向集群中所有执行器发送调度请求，请求中携带分片参数。
如下图：

每个执行器收到调度请求根据分片参数自行决定是否执行任务。
另外xxl-job还支持动态分片，当执行器数量有变更时，调度中心会动态修改分片的数量。
作业分片适用哪些场景呢？
分片任务场景：10个执行器的集群来处理10w条数据，每台机器只需要处理1w条数据，耗时降低10倍；
广播任务场景：广播执行器同时运行shell脚本、广播集群节点进行缓存更新等。
所以，广播分片方式不仅可以充分发挥每个执行器的能力，并且根据分片参数可以控制任务是否执行，最终灵活控制了执行器集群分布式处理任务。
使用说明：
"分片广播" 和普通任务开发流程一致，不同之处在于可以获取分片参数进行分片业务处理。
Java语言任务获取分片参数方式：
BEAN、GLUE模式(Java)，可参考Sample示例执行器中的示例任务"ShardingJobHandler"：
ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
分片参数属性说明：
index：当前分片序号(从0开始)，执行器集群列表中当前执行器的序号；
total：总分片数，执行器集群的总机器数量；
下边测试作业分片：
1、定义作业分片的任务方法
Java/**
  * 2、分片广播任务
  */
 @XxlJob("shardingJobHandler")
 public void shardingJobHandler() throws Exception {

  // 分片参数
  int shardIndex = XxlJobHelper.getShardIndex();
  int shardTotal = XxlJobHelper.getShardTotal();

log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
log.info("开始执行第"+shardIndex+"批任务");

 }
2、在调度中心添加任务

高级配置说明：
Plain Text
添加成功：

启动任务，观察日志

下边启动两个执行器实例，观察每个实例的执行情况
首先在nacos中配置media-service的本地优先配置：
YAML#配置本地优先
spring:
 cloud:
  config:
    override-none: true
将media-service启动两个实例
两个实例的在启动时注意端口不能冲突：
实例1 在VM options处添加：-Dserver.port=63051 -Dxxl.job.executor.port=9998
实例2 在VM options处添加：-Dserver.port=63050 -Dxxl.job.executor.port=9999
例如：

启动两个实例
观察任务调度中心，稍等片刻执行器有两个

观察两个执行实例的日志：

另一实例的日志如下：

从日志可以看每个实例的分片序号不同。
到此作业分片任务调试完成，此时我们可以思考：
当一次分片广播到来，各执行器如何根据分片参数去分布式执行任务，保证执行器之间执行的任务不重复呢？
7.3 需求分析
7.3.1 作业分片方案
掌握了xxl-job的作业分片调度方式，下边思考如何分布式去执行学成在线平台中的视频处理任务。
任务添加成功后，对于要处理的任务会添加到待处理任务表中，现在启动多个执行器实例去查询这些待处理任务，此时如何保证多个执行器不会重复执行任务？
执行器收到调度请求后各自己查询属于自己的任务，这样就保证了执行器之间不会重复执行任务。
xxl-job设计作业分片就是为了分布式执行任务，XXL-JOB并不直接提供数据处理的功能，它只会给执行器分配好分片序号并向执行器传递分片总数、分片序号这些参数，开发者需要自行处理分片项与真实数据的对应关系。
下图表示了多个执行器获取视频处理任务的结构：

每个执行器收到广播任务有两个参数：分片总数、分片序号。每个执行从数据表取任务时可以让任务id 模上 分片总数，如果等于分片序号则执行此任务。
上边两个执行器实例那么分片总数为2，序号为0、1，从任务1开始，如下：
1  %  2 = 1    执行器2执行
2  %  2 =  0    执行器1执行
3  %  2 =  1     执行器2执行
以此类推.
7.3.2 保证任务不重复执行
通过作业分片的方式保证了执行器之间分配的任务不重复，如果同一个执行器在处理一个视频还没有完成，此时调度中心又一次请求调度，为了不重复处理同一个视频该怎么办？
首先配置调度过期策略：
查看文档如下：
    - 调度过期策略：
        - 忽略：调度过期后，忽略过期的任务，从当前时间开始重新计算下次触发时间；
        - 立即执行一次：调度过期后，立即执行一次，并从当前时间开始重新计算下次触发时间；
    - 阻塞处理策略：调度过于密集执行器来不及处理时的处理策略；
这里我们选择忽略，如果立即执行一次可能会重复调度。

其次，再看阻塞处理策略，阻塞处理策略就是当前执行器正在执行任务还没有结束，此时调度时间到该如何处理。
查看文档如下：
        单机串行（默认）：调度请求进入单机执行器后，调度请求进入FIFO队列并以串行方式运行；
        丢弃后续调度：调度请求进入单机执行器后，发现执行器存在运行的调度任务，本次请求将会被丢弃并标记为失败；
        覆盖之前调度：调度请求进入单机执行器后，发现执行器存在运行的调度任务，将会终止运行中的调度任务并清空队列，然后运行本地调度任务；
这里选择 丢弃后续调度，避免重复调度。
最后，也就是要注意保证任务处理的幂等性，什么是任务的幂等性？任务的幂等性是指：对于数据的操作不论多少次，操作的结果始终是一致的。执行器接收调度请求去执行任务，要有办法去判断该任务是否处理完成，如果处理完则不再处理，即使重复调度处理相同的任务也不能重复处理相同的视频。
什么是幂等性？
它描述了一次和多次请求某一个资源对于资源本身应该具有同样的结果。
幂等性是为了解决重复提交问题，比如：恶意刷单，重复支付等。
解决幂等性常用的方案：
1）数据库约束，比如：唯一索引，主键。
2）乐观锁，常用于数据库，更新数据时根据乐观锁状态去更新。
3）唯一序列号，操作传递一个唯一序列号，操作时判断与该序列号相等则执行。
这里我们在数据库视频处理表中添加处理状态字段，视频处理完成更新状态为完成，执行视频处理前判断状态是否完成，如果完成则不再处理。
7.3.2 业务流程
确定了分片方案，下边梳理整个视频上传及处理的业务流程。

视频处理的详细流程如下：

1、任务调度中心广播作业分片。
2、执行器收到广播作业分片，从数据库读取待处理任务。
3、执行器根据任务内容从MinIO下载要处理的文件。
4、执行器启动多线程去处理任务。
5、任务处理完成，上传处理后的视频到MinIO。
6、将更新任务处理结果，如果视频处理完成除了更新任务处理结果以外还要将文件的访问地址更新至任务处理表及文件表中，最后将任务完成记录写入历史表。
下图是待处理任务表：

完成任务历史表与结果与待处理任务相同。
7.4 Service开发
7.4.1 DAO开发
根据需求分析，DAO接口包括如下：
1、查询待处理视频列表。
2、保证视频处理结果
3、添加视频历史处理记录表。
4、删除已完成视频处理记录
5、上传处理后的视频文件向media_files插入记录
如何保证查询到的待处理视频记录不重复？
编写根据分片参数获取待处理任务的DAO方法。
Javapublic interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    /**
     * @description 根据分片参数获取待处理任务
     * @param shardTotal  分片总数
     * @param shardindex  分片序号
     * @param count 任务数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess> 
     * @author Mr.M
     * @date 2022/9/14 8:54
    */
    @Select("SELECT t.* FROM media_process t WHERE t.id % #{shardTotal} = #{shardindex} and t.status='1' limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardindex") int shardindex, @Param("count") int count);
}
7.4.2 Service开发
定义视频处理的Service：
Javapackage com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件处理业务方法
 * @date 2022/9/10 8:55
 */
public interface MediaFileProcessService {

    /**
 * @description 将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
 * @param status  处理结果，2:成功3失败
 * @param fileId  文件id
 * @param url 文件访问url
 * @param errorMsg 失败原因
 * @return void
 * @author Mr.M
 * @date 2022/9/14 14:45
*/
@Transactional
public void saveProcessFinishStatus(int status,String fileId, String url,String errorMsg);

    /**
     * @description 获取待处理任务
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * @author Mr.M
     * @date 2022/9/14 14:49
    */
    public List<MediaProcess> getMediaProcessList(int shardIndex,int shardTotal,int count);


}
实现类如下：
Javapackage com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/14 14:41
 * @version 1.0
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

 @Autowired
 MediaFilesMapper mediaFilesMapper;

 @Autowired
 MediaProcessMapper mediaProcessMapper;

 @Autowired
 MediaProcessHistoryMapper mediaProcessHistoryMapper;



 @Transactional
 public void saveProcessFinishStatus(int status,String fileId, String url,String errorMsg) {
  MediaProcess mediaProcess = mediaProcessMapper.selectOne(new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getFileId, fileId));
  if(mediaProcess == null){
   return ;
  }
  //处理失败
 if(status == 3){
  mediaProcess.setStatus("3");
  mediaProcess.setErrormsg(errorMsg);
  mediaProcessMapper.updateById(mediaProcess);
  return ;
 }
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
  if(mediaFiles!=null){
   mediaFiles.setUrl(url);
   mediaFilesMapper.updateById(mediaFiles);
  }

   mediaProcess.setUrl(url);
   mediaProcess.setStatus("2");
   mediaProcess.setFinishDate(LocalDateTime.now());
   mediaProcessMapper.updateById(mediaProcess);
   //添加到历史记录
   MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
   BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
   mediaProcessHistoryMapper.insert(mediaProcessHistory);
   //删除mediaProcess
   mediaProcessMapper.deleteById(mediaProcess.getId());

 }

 @Override
 public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
  List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
   return mediaProcesses;
 }


}
7.5 任务开发
7.5.1 视频处理工具类
将课程资料目录中的util.zip解压，将解压出的工具类拷贝至base工程。

并在base工程添加如下依赖：
XML<!-- fast Json -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
</dependency>

<!-- servlet Api 依赖 -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <scope>provided</scope>
</dependency>

<!-- 通用组件 -->
<dependency>
    <groupId>commons-lang</groupId>
    <artifactId>commons-lang</artifactId>
</dependency>
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.11</version>
</dependency>
其中Mp4VideoUtil类是用于将视频转为mp4格式，是我们项目要使用的工具类。
下边看下这个类的代码，并进行测试。
我们要通过ffmpeg对视频转码，Java程序调用ffmpeg，使用java.lang.ProcessBuilder去完成，具体在Mp4VideoUtil类的63行。
使用该类的main方法进行测试
Javapublic static void main(String[] args) throws IOException {
    //ffmpeg的路径
    String ffmpeg_path = "D:\\soft\\ffmpeg\\ffmpeg.exe";//ffmpeg的安装位置
    //源avi视频的路径
    String video_path = "D:\\develop\\bigfile_test\\nacos01.avi";
    //转换后mp4文件的名称
    String mp4_name = "nacos01.mp4";
    //转换后mp4文件的路径
    String mp4_path = "D:\\develop\\bigfile_test\\nacos01.mp4";
    //创建工具类对象
    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
    //开始视频转换，成功将返回success
    String s = videoUtil.generateMp4();
    System.out.println(s);
}
执行main方法，最终在控制台输出 success 表示执行成功。
7.5.1 任务类
视频采用并发处理，每个视频使用一个线程去处理，每次处理的视频数量不要超过cpu核心数。
所有视频处理完成结束本次执行，为防止代码异常出现无限期等待添加超时设置，到达超时时间还没有处理完成仍结束任务。
定义任务类VideoTask 如下：
Javapackage com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 视频处理任务
 *
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    private MediaFileService mediaFileService;

    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    //ffmpeg绝对路径
    @Value("${videoprocess.ffmpegpath}")
    String ffmpegPath;

    /**
     * 视频处理
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);

        //一次取出2条记录，可以调整此数据，一次处理的最大个数不要超过cpu核心数
        List<MediaProcess> mediaProcesses = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 2);
        //数据个数
        int size = mediaProcesses.size();

        log.debug("取出待处理视频记录"+size+"条");
        if(size<=0){
            return ;
        }

        //启动size上线程数量的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);

        mediaProcesses.forEach(mediaProcess -> {
            threadPool.execute(()->{
                //桶
                String bucket = mediaProcess.getBucket();
                //存储路径
                String filePath = mediaProcess.getFilePath();
                //原始视频的md5值
                String fileId = mediaProcess.getFileId();
                //原始文件名称
                String filename = mediaProcess.getFilename();

                //下载文件
                //先创建临时文件，为原始的视频文件
                File originalVideo = null;
                //处理结束的mp4文件
                File mp4Video = null;
                try {
                    originalVideo = File.createTempFile("original", null);
                    mp4Video = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    log.error("下载待处理的原始文件前创建临时文件失败");
                }
                try {
                    originalVideo = mediaFileService.downloadFileFromMinIO(originalVideo, bucket, filePath);

                    //开始处理视频
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath,originalVideo.getAbsolutePath(),mp4Video.getName(),mp4Video.getAbsolutePath());
                    String result = mp4VideoUtil.generateMp4();
                    if(!result.equals("success")){
                        //记录错误信息
                        log.error("generateMp4 error ,video_path is {},error msg is {}",bucket+filePath,result);
                        mediaFileProcessService.saveProcessFinishStatus(3,fileId,null,result);
                        return ;
                    }
                    //将mp4上传至minio
                    //文件路径
                    String objectName = getFilePath(fileId, ".mp4");
                    mediaFileService.addMediaFilesToMinIO(mp4Video.getAbsolutePath(),bucket,objectName);

                    //访问url
                    String url = "/"+bucket+"/"+objectName;
                    //将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
                    mediaFileProcessService.saveProcessFinishStatus(2,fileId,url,null);

                } catch (Exception e) {
                   e.printStackTrace();
                }finally {
                    //清理文件
                    if(originalVideo!=null){
                        try {
                            originalVideo.delete();
                        } catch (Exception e) {

                        }
                    }
                    if(mp4Video!=null){
                        try {
                            mp4Video.delete();
                        } catch (Exception e) {

                        }
                    }
                }
                countDownLatch.countDown();

            });
        });

        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await();


    }


    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

}
7.6 视频处理测试
7.6.1 上传视频代码完善
上传视频成功根据视频文件格式判断，如果是avi文件则需要加入视频待处理表。
Java/**
 * @param fileMd5             文件md5
 * @param uploadFileParamsDto 文件的基本信息
 * @param bucket              桶
 * @param objectName          minio中文件的路径
 * @return int
 * @description 媒资文件入库
 * @author Mr.M
 * @date 2022/9/13 16:27
 */
@Transactional
public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {

    MediaFiles mediaFiles = new MediaFiles();
    BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
    mediaFiles.setId(fileMd5);
    mediaFiles.setFileId(fileMd5);
    mediaFiles.setCompanyId(companyId);
    mediaFiles.setUrl(url);
    mediaFiles.setBucket(bucket);
    mediaFiles.setFilePath(objectName);
    mediaFiles.setCreateDate(LocalDateTime.now());
    mediaFiles.setAuditStatus("202004");
    mediaFiles.setStatus("1");//初始状态正常显示
    //保存文件信息到媒资数据库
    int insert = mediaFilesMapper.insert(mediaFiles);
   //取出文件扩展名
    String extension = objectName.substring(objectName.lastIndexOf(".") );
    //如果是avi则加入视频处理表
    if(extension.equalsIgnoreCase(".avi")){
        MediaProcess mediaProcess = new MediaProcess();
        BeanUtils.copyProperties(mediaFiles,mediaProcess);
        mediaProcessMapper.insert(mediaProcess);
        //更新url为空
        mediaFiles.setUrl(null);
        mediaFilesMapper.updateById(mediaFiles);
    }

    return mediaFiles;

}
7.6.2 视频处理调度策略
进入xxl-job调度中心添加执行器和任务
在xxl-job配置任务调度策略：
1）配置阻塞处理策略为：丢弃后续调度。
2）配置视频处理调度时间间隔不用根据视频处理时间去确定，可以配置的小一些，如：5分钟，即使到达调度时间如果视频没有处理完会丢失调度请求。
配置完成开始测试视频处理：
1、首先上传至少4个视频，非mp4格式。
2、启动媒资管理服务，观察日志
8 绑定媒资
8.1 需求分析
8.1.1 业务流程
到目前为止，媒资管理已完成文件上传、视频处理、我的媒资功能等基本功能，其它模块可以使用媒资文件，本节要讲解课程计划绑定媒资文件。
如何将课程计划绑定媒资呢？
首先进入课程计划界面，然后选择要绑定的视频进行绑定即可。
具体的业务流程如下：
1.教育机构用户进入课程管理页面并编辑某一个课程，在"课程大纲"标签页的某一小节后可点击”添加视频“。

2.弹出添加视频对话框，可通过视频关键字搜索已审核通过的视频媒资。

3.选择视频媒资，点击提交按钮，完成课程计划绑定媒资流程。

课程计划关联视频后如下图：

8.1.2 数据模型 
课程计划绑定媒资文件后存储至课程计划绑定媒资表

8.2 接口定义
根据业务流程，用户进入课程计划列表，首先确定向哪个课程计划添加视频，点击”添加视频“后用户选择视频，选择视频，点击提交，提交媒资文件id、文件名称、教学计划id，示例如下：
JSON{
  "mediaId": "70a98b4a2fffc89e50b101f959cc33ca",
  "fileName": "22-Hmily实现TCC事务-开发bank2的confirm方法.avi",
  "teachplanId": 257
}
此接口在内容管理模块提供。
在内容管理模块定义请求参数模型类型：
Java@Data
@ApiModel(value="BindTeachplanMediaDto", description="教学计划-媒资绑定提交数据")
public class BindTeachplanMediaDto {

 @ApiModelProperty(value = "媒资文件id", required = true)
 private Long mediaId;

 @ApiModelProperty(value = "媒资文件名称", required = true)
 private Long fileName;

 @ApiModelProperty(value = "课程计划标识", required = true)
 private Long teachplanId;


}
接口定义如下：
Java@ApiOperation(value = "课程计划和媒资信息绑定")
@PostMapping("/teachplan/association/media")
void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){

}

@ApiOperation(value = "课程计划和媒资信息解除绑定")
@DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
void delAssociationMedia(@PathVariable Long teachPlanId,@PathVariable Long mediaId){

}
8.3 接口开发
8.3.1 DAO开发
对teachplanMedia表自动生成Mapper。
8.3.2 Service开发
根据需求定义service接口
Java/**
 * @description 教学计划板顶媒资
 * @param bindTeachplanMediaDto
 * @return com.xuecheng.content.model.po.TeachplanMedia
 * @author Mr.M
 * @date 2022/9/14 22:20
*/
public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

/**
 * @description 删除教学计划与媒资之间的绑定关系
 * @param teachPlanId  教学计划id
 * @param mediaId  媒资文件id
 * @return void
 * @author Mr.M
 * @date 2022/9/14 22:21
*/
public void delAassociationMedia(Long teachPlanId, String mediaId);
定义接口实现
Java@Override
public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
 //教学计划id
 Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
 Teachplan teachplan = teachplanMapper.selectById(teachplanId);
 if(teachplan==null){
  XueChengPlusException.cast("教学计划不存在");
 }
 Integer grade = teachplan.getGrade();
 if(grade!=2){
  XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
 }
 //课程id
 Long courseId = teachplan.getCourseId();

 //先删除原因该教学计划绑定的媒资
 teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId));

 //再添加教学计划与媒资的绑定关系
 TeachplanMedia teachplanMedia = new TeachplanMedia();
 teachplanMedia.setCourseId(courseId);
 teachplanMedia.setTeachplanId(teachplanId);
 teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
 teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
 teachplanMedia.setCreateDate(LocalDateTime.now());
 teachplanMediaMapper.insert(teachplanMedia);
 return teachplanMedia;
}

@Override
public void delAassociationMedia(Long teachPlanId, String mediaId) {
 teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachPlanId).eq(TeachplanMedia::getMediaId,mediaId));
}

//交换位置
private void swapTeachplan(Teachplan left,Teachplan right){
 if(left==null || right==null){
  return ;
 }
 Integer orderby_left = left.getOrderby();
 Integer orderby_right = right.getOrderby();
 left.setOrderby(orderby_right);
 right.setOrderby(orderby_left);
 teachplanMapper.updateById(left);
 teachplanMapper.updateById(right);
 log.debug("课程计划交换位置，left:{},right:{}",left.getId(),right.getId());
}
8.3.3 接口层完善
完善接口层调用Service层的代码
Java@ApiOperation(value = "课程计划和媒资信息绑定")
@PostMapping("/teachplan/association/media")
void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
    teachplanService.associationMedia(bindTeachplanMediaDto);
}

@ApiOperation(value = "课程计划和媒资信息解除绑定")
@DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
void delAssociationMedia(@PathVariable Long teachPlanId,@PathVariable String mediaId){
    teachplanService.delAassociationMedia(teachPlanId,mediaId);
}
8.3.4 接口测试
1、使用httpclient测试
Plain Text### 课程计划绑定视频
POST {{media_host}}/media/teachplan/association/media
Content-Type: application/json

{
  "mediaId": "",
  "fileName": "",
  "teachplanId": ""
}

### 课程计划接触视频绑定
DELETE {{media_host}}/media/teachplan/association/media/{teachPlanId}/{mediaId}
2、前后端联调
此功能较为简单推荐直接前后端联调
向指定课程计划添加视频、解除视频绑定。
