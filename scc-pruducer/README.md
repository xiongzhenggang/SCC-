## http spring cloud contract 使用 product 
[https://docs.spring.io/spring-cloud-contract]
### 生产端步骤
https://docs.spring.io/spring-cloud-contract/docs/3.0.2/reference/htmlsingle/#getting-started-first-application-producer

1. pom输入
   To start working with Spring Cloud Contract, you can add the Spring Cloud Contract Verifier dependency and plugin to your build file, as the following example shows:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-verifier</artifactId>
    <scope>test</scope>
</dependency>
```

以下清单显示了如何添加插件，该插件应放在文件的buildplugins部分中：```xml
```xml
<plugin>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-contract-maven-plugin</artifactId>
    <extensions>true</extensions>
    <configuration>
        <!--用于构建过程中插件自动生成测试用例的基类，BaseCase使用http基类-->
        <baseClassForTests> com.xzg.test.scc.BaseCase</baseClassForTests>
    </configuration>
</plugin>
```
2. 对于HTTPstubs，契约定义了应针对给定请求返回的响应类型（考虑到HTTP方法，URL，标头，状态码等）。以下示例显示了Groovy和YAML中的HTTPstubs协定：
契约默认存放在test下resource的contracts目录下
```yml
request:
  method: PUT
  url: /fraudcheck
  body:
    "client.id": 1234567890
    loanAmount: 99999
  headers:
    Content-Type: application/json
  matchers:
    body:
      - path: $.['client.id']
        type: by_regex
        value: "[0-9]{10}"
response:
  status: 200
  body:
    fraudCheckStatus: "FRAUD"
    "rejection.reason": "Amount too high"
  headers:
    Content-Type: application/json;charset=UTF-8
```
3. 执行命令将生成测试stubs测试类，用于测试，请求是否正确
> mvn clean install
可以看到日志：
```angular2html
....
[INFO] Installing /some/path/http-server/target/http-server-0.0.1-SNAPSHOT.jar to /path/to/your/.m2/repository/com/example/http-server/0.0.1-SNAPSHOT/http-server-0.0.1-SNAPSHOT.jar
[INFO] Installing /some/path/http-server/pom.xml to /path/to/your/.m2/repository/com/example/http-server/0.0.1-SNAPSHOT/http-server-0.0.1-SNAPSHOT.pom
[INFO] Installing /some/path/http-server/target/http-server-0.0.1-SNAPSHOT-stubs.jar to /path/to/your/.m2/repository/com/example/http-server/0.0.1-SNAPSHOT/http-server-0.0.1-SNAPSHOT-stubs.jar
```
同时可以在target的generated-test-source目录下生成测试文件
```java
@Test
public void validate_shouldMarkClientAsFraud() throws Exception {
    // given:
        MockMvcRequestSpecification request = given()
                .header("Content-Type", "application/vnd.fraud.v1+json")
                .body("{\"client.id\":\"1234567890\",\"loanAmount\":99999}");

    // when:
        ResponseOptions response = given().spec(request)
                .put("/fraudcheck");

    // then:
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.header("Content-Type")).matches("application/vnd.fraud.v1.json.*");
    // and:
        DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
        assertThatJson(parsedJson).field("['fraudCheckStatus']").matches("[A-Z]{5}");
        assertThatJson(parsedJson).field("['rejection.reason']").isEqualTo("Amount too high");
}
```
* 完成后消费端既可直接使用

##  使用kafka消息中间件的scc

我们应考虑三种主要情况:
方案1：没有输入消息会生成输出消息。输出消息由应用程序内部的组件（例如，调度程序）触发。
方案2：输入消息触发输出消息。
方案3：输入消息已被使用，并且没有输出消息。


* 注意spring cloud 以及maven plug版本问题
  https://github.com/spring-cloud/spring-cloud-contract/issues/1664
  
测试使用方案2， 
1. 修改pom支持kafka
```xml
  <dependencies>
  <dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
    <dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
    </dependency>
</dependencies>
<!--。。。-->
<build>
 <plugins>
            <plugin>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-contract-maven-plugin</artifactId>
                <version>${spring-cloud-contract-plugin.version}</version>
                <extensions>true</extensions>
                <configuration>
                    <!--用于构建过程中插件自动生成测试用例的基类，BaseCase使用http基类-->
                    <baseClassForTests> com.xzg.test.scc.BaseCase</baseClassForTests>
                    <!--用于构建过程中插件自动生成测试用例的基类，下面使用test mesage下的契约的基类，用于kafka-->
                    <baseClassMappings>
                        <baseClassMapping>
                            <contractPackageRegex>.*message.*</contractPackageRegex>
                            <baseClassFQN>com.xzg.test.scc.BaseKafkaCase</baseClassFQN>
                        </baseClassMapping>
                    </baseClassMappings>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <executions>
                    <!--用于指定自动生成测试类的目录-->
                    <execution>
                        <id>add-source</id>
                        <phase>generate-test-sources</phase>
                        <goals>
                            <goal>add-test-source</goal>
                        </goals>
                        <configuration>
                            <sources>
                                <source>${project.build.directory}/generated-test-sources/contracts/</source>
                            </sources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
</build>
```

2. 编写契约
```yaml
# Human readable description
description: Some description
# Label by means of which the output message can be triggered
label: some_label
# input is a message
input:
  messageFrom: kafka_topic
  # has the following body
  messageBody:
    bookName: 'foo'
  # and the following headers
  messageHeaders:
    sample: 'header'
# output message of the contract
outputMessage:
  # destination to which the output message will be sent
  sentTo: kafka_topic
  # the body of the output message
  body:
    bookName: foo
  # the headers of the output message
  headers:
    BOOK-NAME: foo
```
3. 测试生成测试
>mvn clean install -DTest
4. 查看编译后自动生成测试类
target/generated-test-source/contracts/
```java
package com.xzg.test.scc;

import com.xzg.test.scc.BaseKafkaCase;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class MessageTest extends BaseKafkaCase {
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_inputmessage() throws Exception {
		// given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					"{\"bookName\":\"foo\"}"
						, headers()
							.header("sample", "header")
			);

		// when:
			contractVerifierMessaging.send(inputMessage, "kafka_topic");

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("kafka_topic");
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("BOOK-NAME")).isNotNull();
			assertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
	}

	@Test
	public void validate_trigger() throws Exception {
		// when:
			trigger();

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("kafka_topic");
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("BOOK-NAME")).isNotNull();
			assertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");
			assertThat(response.getHeader("contentType")).isNotNull();
			assertThat(response.getHeader("contentType").toString()).isEqualTo("application/json");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
	}

}
```
**注意：编译会自动将生成stub.jar上传到本地maven仓库。消费者端使用也是通过拉去本地测试。如果想要推送到远程仓库，需要单独修改Spring Cloud Contract Stub Runner properties。另一种方式也可以使用git推送，本地拉去编译**


