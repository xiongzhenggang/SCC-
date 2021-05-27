## spring cloud contract 使用 consumer 
[https://docs.spring.io/spring-cloud-contract]
### 使用步骤
1. pom
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
    <scope>test</scope>
</dependency>
```
2. 执行编辑
>./mvnw clean install -DskipTests
3. 测试类如下
```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringcloudContractConsumerApplication.class,webEnvironment= SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(ids = {"org.xzg:scc-pruducer:+:stubs:8800"},
    stubsMode = StubRunnerProperties.StubsMode.LOCAL)
public class LoanApplicationServiceTests {
    @Autowired
    private RestTemplate restTemplate;
        @Test
        public void shouldBeRejectedDueToAbnormalLoanAmount() throws IOException {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json");
            ResponseEntity<String> response = restTemplate.exchange("http://localhost:8800/fraudcheck", HttpMethod.PUT,
                new HttpEntity<>("{\"client.id\":\"1234567890\",\"loanAmount\":99999}", httpHeaders),String.class);
            assertThat(response.getBody()).isEqualTo("{\"fraudCheckStatus\":\"FRAUD\",\"rejection.reason\":\"Amount too high\"}");
        }

}
```
4 .执行查看结果


###注意事项 
1. 如果报错No stubs or contracts were found for [XXX]，可能是当前maven环境变量找的仓库不对，需要指定正确地址 idea启动配置环境变量org.apache.maven.user-settings=xxx\settings.xml
2. 关于ids也需要指定正确：groupId:artifactId
3. 端口为stub端口，客户端调用要一致


## kafka scc测试部分

需要 spring-kafka-test 依赖)

```angular2html
通过Kafka集成，为了轮询单个消息，我们需要在Spring上下文启动时注册consumer 。这可能会导致一种情况，当您在consumer 方面时，Stub Runner可以为相同的组ID和主题注册其他使用者。
这可能会导致这样一种情况，即只有一个组件会实际轮询该消息。由于在消费者方面，您同时具有Spring Cloud Contract Stub Runner和Spring Cloud Contract Verifier类路径，因此我们需要能够关闭此类行为。这是通过stubrunner.kafka.initializer自动完成的。
enabled标志，它禁用了Contact Verifier消费者注册。如果您的应用程序既是Kafka消息的使用者又是生产者，则可能需要在生成的测试的基类中将该属性手动切换为false。
```
使用步骤
1. 增加pom
```xml
<dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.awaitility</groupId>
            <artifactId>awaitility</artifactId>
            <version>4.0.3</version>
            <scope>test</scope>
        </dependency>
```
2. 增加测试配置

```yaml
server:
  port: 8880
logging.level.org.springframework.cloud.contract: debug
#stubrunner:
#  repository-root: stubs:classpath:/stubs/
#  ids: my:stubs
#  stubs-mode: remote
#kafka:
#  initializer:
#    enabled: true
spring:
  kafka:
    bootstrap-servers: 127.0.0.1:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        max:
          poll:
            interval:
              ms: 900000
```
3. 编写测试类
```java
@SpringBootTest(classes = SpringcloudContractConsumerApplication.class,webEnvironment= SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(ids = "org.xzg:scc-pruducer", stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@EmbeddedKafka(partitions = 1,topics = {"kafka_topic"},ports = 9092)
@ActiveProfiles("test")
@Slf4j
public class ConsumerKafkaTest {

    @Autowired StubTrigger trigger;

    @Test
    public void consumerNoInput() {
        //        Trigger by Label
        this.trigger.trigger("some_label");
        //        Trigger by Group and Artifact IDs
        //        trigger.trigger('org.springframework.cloud.contract.verifier.stubs:streamService', 'return_book_1')
        //        Trigger by Artifact IDs
        //        trigger.trigger('streamService', 'return_book_1')
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BDDAssertions.then(KafkaConsumer.msg).isNotNull();
        BDDAssertions.then(KafkaConsumer.msg.getPayload().getBookName()).contains("foo");
        BDDAssertions.then(KafkaConsumer.msg.getHeaders().get("BOOK-NAME"))
                .isEqualTo("foo");
    }
}
```
4. 执行测试观察结果