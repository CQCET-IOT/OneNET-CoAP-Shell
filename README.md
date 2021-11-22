## CoAP Shell for OneNET

这是一个改编自 [CoAP Shell](https://github.com/tzolov/coap-shell) 项目的命令行工具，可以用于测试 OneNET Studio 平台的 CoAP 设备接入。原项目的文档可以参考 [这里]()。

### 编译

克隆本项目：

```
git clone https://github.com/CQCET-IOT/coap-shell.git
```

Maven 自动下载完成所有依赖库以后，将项目中的 *modified-jar\californium-core-2.6.3.jar* 拷贝到 Maven 仓库中，去替换自动下载的 *californium-core-2.6.3.jar*。比如我的依赖库会下载到 *F:\ProgramEnv\mvnRepo\org\eclipse\californium\californium-core\2.6.3\californium-core-2.6.3.jar*，其中的仓库路径 *F:\ProgramEnv\mvnRepo* 是在 Maven 配置文件 *settings.xml* 中配置的。

### 使用

编译得到 *coap-shell-1.1.2-SNAPSHOT.jar*，使用下述命令将其启动起来：

```
java -jar coap-shell-1.1.2-SNAPSHOT.jar
```

运行后，会打开 CoAP Shell 窗口，这是一个命令行工具。

```
  _____     ___   ___     ______       ____
 / ___/__  / _ | / _ \   / __/ /  ___ / / /
/ /__/ _ \/ __ |/ ___/  _\ \/ _ \/ -_) / /
\___/\___/_/ |_/_/     /___/_//_/\__/_/_/
CoAP Shell (v1.1.2-SNAPSHOT)
For assistance hit TAB or type "help".

server-unknown:>
```

首先需要连接到 OneNET 平台，通过下面指令进行连接：

```
connect coap://183.230.102.116:5683

```

连接成功后，就可以向服务器发送 GET/POST/PUT/DELETE 指令了。具体的指令参看 [OneNET 官方文档](https://open.iot.10086.cn/doc/iot_platform/book/device-connect&manager/CoAP/CoAP-connect.html)。

按文档要求，先进行设备登录：

```
post /$sys/XXXXXXXXXX/COAP-DEVICE/login --payload '{"lt":600,"st":"version=2018-10-31&res=products/XXXXXXXXXX/devices/COAP-DEVICE&sign=Lbdn9HNoqLBwajUYhVjsVMl4crY%3D&et=1645322317&method=sha1"}' --format 'application/json'
```

其中，登录操作需要使用 *POST* 指令，在命令行中就是 *post* 命令；URI 的格式为 *$sys/{pid}/{device-name}/login*，其中 *pid* 和 *device-name* 部分需要使用自己的 CoAP 设备进行替换。命令中为了安全起见，特意用 *XXXXXXXXXX* 来代替我的 *pid*；*payload* 部分是登录时需要提交给 OneNET 平台的 JSON 数据，其中的 *st* 字段需要使用 Token 计算器自行计算，Token 计算器可以从 [这里下载](https://open.iot.10086.cn/doc/iot_platform/book/device-connect&manager/device-auth.html)。

总之，这一步的命令行需要自行构建完毕，才能正确登录。登录成功以后会返回 Token 信息，如下：

```
----------------------------- Response -----------------------------
POST coap://183.230.102.116:5683/$sys/XXXXXXXXXX/COAP-DEVICE/login
MID: 38459, Type: ACK, Token: F82F6F4391D92C59, RTT: 20ms
Options: {"Location-Path":"$sys"}
Status : 201-Created, Payload: 8B
............................. Payload ..............................
69FA73187498C923
--------------------------------------------------------------------
```

此处返回的 Token 需要在后续命令中携带，比如上传设备属性数据时，就采用 `--token` 参数进行携带。但在继续执行指令之前，还需要确保 CoAP 设备的物模型中，拥有一个名为 Brand 的属性，类型为 string，下面的例子就是在更新 Brand 属性的数据。


```
post /$sys/XXXXXXXXXX/COAP-DEVICE/thing/property/post --payload '{"id": "53725","version": "1.0","params": {"Brand": {"value": "this is a string3"}}}' --format 'application/json' --accept 'application/json' --token '69FA73187498C923'
----------------------------- Response -----------------------------
POST coap://183.230.102.116:5683/$sys/XXXXXXXXXX/COAP-DEVICE/thing/property/post
MID: 38462, Type: ACK, Token: 69FA73187498C923, RTT: 82ms
Options: {}
Status : 205-Reset Content, Payload: 0B
............................. Payload ..............................
--------------------------------------------------------------------
```

可以看到，这个 POST 指令执行成功。此时前往 OneNET Studio 就可以看到刚刚上传的字符串了。

