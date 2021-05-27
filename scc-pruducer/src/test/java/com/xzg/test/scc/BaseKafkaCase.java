package com.xzg.test.scc;

import com.xzg.scccproducer.SpringcloudContractProviderRestApplication;
import com.xzg.scccproducer.kafka.KafkaController;
import com.xzg.scccproducer.reset.FraudController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
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
 * 2021/5/19       xiongzhenggang        1.0          Initial Creation
 *
 * </pre>
 *
 * @author xiongzhenggang
 * @date 2021/5/19
 * </p>
 */
@SpringBootTest(classes = SpringcloudContractProviderRestApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE)
// remove::start[]
@AutoConfigureMessageVerifier
@EmbeddedKafka(partitions = 1,topics = "kafka_topic")
// remove::end[]
@ActiveProfiles("test")
public class BaseKafkaCase {
    @Autowired KafkaController controller;

    public void trigger() {
        this.controller.sendFoo("example");
    }

    public void triggerMessage() {
        this.controller.sendFooAsMessage("example");
    }



}
