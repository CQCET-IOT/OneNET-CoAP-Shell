## CoAP Shell for OneNET

这是一个改编自 [CoAP Shell](https://github.com/tzolov/coap-shell) 项目的命令行工具，可以用于测试 OneNET Studio 平台的 CoAP 设备接入。

OneNET 官方提供了一个 Studio 模拟器，在模拟器中可以采用 CoAP 协议接入设备，但那个模拟器太自动化了，替用户完成了大量工作，体会不到 CoAP 交互的过程。本项目比起官方模拟器来，能使用户更深入地了解 CoAP 接入的细节。你可以把本项目看成是 CoAP 版的 POSTMAN。

### 编译

> 直接使用编译好的 compiled-jar\coap-shell-1.1.2-SNAPSHOT.jar 也行，可以跳过编译环节

克隆本项目：

```
git clone https://github.com/CQCET-IOT/OneNET-CoAP-Shell.git
```

使用 Maven package 打包之后，会自动下载完成所有依赖库。此时会有编译错误，提示找不到合适的方法，这是因为我修改了 *californium-core-2.6.3.jar* 中的 *CoapClient.java* 文件，添加了一个重载的 `post(String, int, int, Token)` 函数。

将项目中的 *modified-jar\californium-core-2.6.3.jar* 拷贝到 Maven 仓库中，替换自动下载的 *californium-core-2.6.3.jar*。

比如我的依赖库会下载到 *F:\ProgramEnv\mvnRepo\org\eclipse\californium\californium-core\2.6.3\californium-core-2.6.3.jar*，其中：

- 仓库路径 *F:\ProgramEnv\mvnRepo* 是在 Maven 配置文件 *settings.xml* 中配置的
- 库路径 *org\eclipse\californium\californium-core\2.6.3* 是 Maven 按照库的 groupId, artifactId 和 version 三个字段创建的

替换完成之后，关闭 IEDA，重新打开项目，再次 package 打包，错误提示会消失，也能够编译打包成功。

### 使用

编译得到 *coap-shell-1.1.2-SNAPSHOT.jar*，在 cmd 窗口中使用下述命令将其启动起来：

```
java -jar coap-shell-1.1.2-SNAPSHOT.jar
```

运行后，会打开 CoAP Shell 窗口，这是一个命令行工具，其 Banner 如下：

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

此处 Payload 中返回的就是 Token，需要在后续命令中携带，比如上传设备属性数据时，就采用 `--token` 参数进行携带。但在继续执行指令之前，还需要确保 CoAP 设备的物模型中，拥有一个名为 Brand，类型为 string 的属性，下面的例子就是在更新 Brand 属性的数据。


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

### 改造过程（供开发者参考）

CoAP Shell 原项目在不修改任何代码的情况下，是可以连接到 OneNET Studio 平台的，而且可以完成设备登录，平台上也会显示设备在线。

但是原项目将登录后服务器返回的 Token 按照 UTF-8 输出，显示为乱码（未能以十六进制打印），也不能手动设置报文的 Token。要想实现向 OneNET Studio 上报数据，就必须对其进行改造。

打印十六进制的 Token 比较简单，在 CoAP Shell 上修改就可以完成；手动设置报文的 Token，则必须修改 CoAP Shell 所依赖的 [californium](https://github.com/eclipse/californium) 库。


#### 打印十六进制Token

在 *src/main/java/io/datalake/coap/coapshell/util/PrintUtils.java* 中增加下述函数，用于将 Token 按照十六进制打印输出：

```
/**
 * 字节流类型的payload，转化为十六进制进行显示
 * @param text
 * @return
 */
private static String prettyStream(byte[] text) {
	try {
		Token token = new Token(text);
		return token.getAsString();
	}
	catch (Exception e) {
		return text.toString();
	}
}
```

并且在 `prettyPayload()` 函数中增加一个 else if 判断，并且调用上面的 `prettyStream()`：

```
public static String prettyPayload(Response r) {
	...
	else if (r.getOptions().toString().contains("$sys")) { /* OneNET登录返回此种类型的ACK，payload中为后续通信的Token */
		return cyan(prettyStream(r.getPayload()));
	}
	return r.getPayloadString();
}
```

这里修改完成以后，CoAP Shell 打印的 Token 就可以正常显示出来了。前面已经看到过 Token 输出的效果。


#### 手动设置报文Token

CoAP Shell 依赖于 californium 2.6.3，因此要下载 californium 2.6.3 版本的代码。

查看代码发现，californium 重载了多种 `post()` 方法，但无一例外都使用了类似下面的方法：

```
public CoapResponse post(String payload, int format, int accept) throws ConnectorException, IOException {
    Request request = this.newPost();
    request.setPayload(payload);
    request.getOptions().setContentFormat(format);
    request.getOptions().setAccept(accept);
    this.assignClientUriIfEmpty(request);
    return this.synchronous(request);
}
```

这些 `post()` 方法中，都是首先初始化一个 *Request* 对象，然后设置 payload 和 option，但都没有设置 Token，所以需要重载一个新的 `post()` 方法才行，将 Token 作为参数传进去，在里面设置 Token 然后发送出去。

修改 *org/eclipse/californium/core/CoapClient.java*，在其中加入重载的 `post(String, int, int, Token)` 函数，该函数支持携带 Token 作为参数，修改后的 *CoapClient.java* 放到了 [这里](https://gist.github.com/morgengc/13fb8137dc2d84e6eb670a49f334334b)，L602-L623 就是重载的 `post()` 函数，内容如下：

```
/**
 * Sends a POST request with the specified payload, the specified content
 * format and the specified Accept option and blocks until the response is
 * available.
 *
 * @param payload the payload
 * @param format the Content-Format
 * @param accept the Accept option
 * @param token token returned by OneNET
 * @return the CoAP response
 * @throws ConnectorException if an issue specific to the connector occurred
 * @throws IOException if any other issue (not specific to the connector) occurred
 */
public CoapResponse post(String payload, int format, int accept, Token token) throws ConnectorException, IOException {
	Request request = newPost();
	request.setToken(token);
	request.setPayload(payload);
	request.getOptions().setContentFormat(format);
	request.getOptions().setAccept(accept);
	assignClientUriIfEmpty(request);
	return synchronous(request);
}
```

对 californium 库的修改，只需要添加上面的重载函数就完成了。不过重新编译这个库有点小麻烦。

起初设想是重新完整编译 californium 2.6.3 版本的代码，重载后重新编译得到 jar 包，再手动安装到 Maven 仓库中。但后来发现，2.6.3 版本的代码并没有严格对应 POM 中依赖那个 2.6.3 版本库，它编译出来是 2.6.0 版本的 jar 包。

也不想多花时间验证其他版本了。所以我只能进行局部替换，把 2.6.3 源码编译出来的 *CoapClient.class* 手动替换到 Maven 自动下载的 *californium-core-2.6.3.jar* 库中。理论上这样做可以了。但是 jar 包对 class 文件做了 RSA 校验，因此还需要把 *META-INF* 目录下的 *\*.RSA*, *\*.SF* 文件删除掉，强制程序在执行时不进行校验。这就是本项目 *modified-jar\californium-core-2.6.3.jar* 的由来。

依赖库增加了设置 Token 功能，回过来，CoAP Shell 中就需要调用这个功能。

修改 CoAP Shell 的 *src/main/java/io/datalake/coap/coapshell/command/CoapShellCommands.java* 文件中 POST 命令的逻辑，增加一个 Token 参数，并且调用前面重载的 `post()` 函数。

```
public String post(
        ...
		@ShellOption(defaultValue = ShellOption.NULL, help = "OneNET Token") String token) throws IOException, ConnectorException {

        ...

		} else {
		    CoapResponse response;
		    if (token == null) {
				response = coapClient.post(payloadContent, coapContentType(format), coapContentType(accept));
			} else {
				Token realToken = new Token(StringUtil.hex2ByteArray(token));
				response = coapClient.post(payloadContent, coapContentType(format), coapContentType(accept), realToken);
			}

			result.append(PrintUtils.prettyPrint(response, requestInfo("POST", baseUri + path, async)));
		}
	}
	
	...
}
```

最后在执行上报属性的指令时，增加一个 `--token` 参数，携带上登录返回的十六进制 Token 值，就可以啦！

