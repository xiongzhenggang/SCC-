package com.xzg.scc.service;

import com.xzg.scc.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

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
@Component
@Slf4j
public class KafkaConsumer {

    public static Message<Book> msg ;

    @KafkaListener(topics = "kafka_topic",groupId = "group02")
    public void listen(Message<Book> fooMsg) {
        Book book = fooMsg.getPayload();
        log.info("======> consumer Received group:{} data: {}",fooMsg.getHeaders() ,book);
        msg = fooMsg;
    }
}
