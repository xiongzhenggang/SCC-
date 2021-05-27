package com.xzg.scc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
/**
 * 注意
 * 1、如果报错No stubs or contracts were found for [XXX]，可能是当前maven环境变量找的仓库不对，需要指定正确地址
 *  idea启动配置环境变量org.apache.maven.user-settings=xxx\settings.xml
 * 2 、关于ids也需要指定正确：groupId:artifactId
 * 3、端口为stub端口，客户端调用要一致
 */

@SpringBootTest(classes = SpringcloudContractConsumerApplication.class,webEnvironment= SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(ids = {"org.xzg:scc-pruducer:+:stubs:8000"},
    stubsMode = StubRunnerProperties.StubsMode.LOCAL)
public class LoanApplicationServiceTests {
    @Autowired
    private RestTemplate restTemplate;

        @Test
        public void shouldBeRejectedDueToAbnormalLoanAmount() {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json");
            ResponseEntity<String> response = restTemplate.exchange("http://localhost:8000/fraudcheck", HttpMethod.PUT,
                new HttpEntity<>("{\"client.id\":\"1234567890\",\"loanAmount\":99999}", httpHeaders),String.class);
            assertThat(response.getBody()).isEqualTo("{\"fraudCheckStatus\":\"FRAUD\",\"rejection.reason\":\"Amount too high\"}");
        }


}
