package com.xzg.scc;

import com.xzg.scc.service.KafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;


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
 * 2021/5/24       xiongzhenggang        1.0          Initial Creation
 *
 * </pre>
 *
 * @author xiongzhenggang
 * @date 2021/5/24
 * </p>
 */
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
