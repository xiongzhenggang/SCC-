package com.xzg.test.isolationkafkatest;

import com.xzg.scccproducer.SpringcloudContractProviderRestApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;

/**
 * <p>
 * <b>Class name</b>:
 * </p>
 * <p>
 * <b>Class description</b>: Class description goes here.
 * </p>
 * <p>
 * <b>Author</b>: xiongzhenggang
 * </p>
 * <b>Change History</b>:<br/>
 * <p>
 *
 * <pre>
 * Date          Author       Revision     Comments
 * ----------    ----------   --------     ------------------
 * 2021/5/26       xiongzhenggang        1.0          Initial Creation
 *
 * </pre>
 *
 * @author xiongzhenggang
 * @date 2021/5/26
 * </p>
 */
// 创建topic
@EmbeddedKafka(topics = "topic1")
// 覆盖配置
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    // 消费更早的消息,因为可能消息已经发送但是bean还没注册
    "spring.kafka.consumer.auto-offset-reset=earliest"})
@SpringBootTest(classes = SpringcloudContractProviderRestApplication.class)
@Slf4j
public class KafkaTest {
    @Autowired KafkaTemplate<String, String> template;

    @KafkaListener(id = "webGroup1", topics = "topic1")
    public void listen(ConsumerRecord<String, String> input) {
        log.info("=============>>>>input value: {}", input.value());
    }
    @Test
    void kafka() throws InterruptedException, ExecutionException {
        log.info("=============>>>>start send");
        ListenableFuture<SendResult<String, String>> future = template
            .send("topic1", "time: " + System.currentTimeMillis());
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("=============>>>>onFailure {}", throwable);
            }
            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                log.info("=============>>>>onSuccess {}", stringStringSendResult);
            }
        });
        log.info("=============>>>>result: {}", future.get());
    }


}
