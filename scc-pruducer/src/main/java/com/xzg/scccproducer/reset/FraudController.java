package com.xzg.scccproducer.reset;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
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


@RestController
public class FraudController {

    @PutMapping(value = "/fraudcheck", consumes="application/json", produces="application/json")
    public String check(@RequestBody LoanRequest loanRequest) {

        if (loanRequest.getLoanAmount() > 10000) {
            return "{fraudCheckStatus: FRAUD, rejection.reason: Amount too high}";
        } else {
            return "{fraudCheckStatus: OK, acceptance.reason: Amount OK}";
        }
    }
}