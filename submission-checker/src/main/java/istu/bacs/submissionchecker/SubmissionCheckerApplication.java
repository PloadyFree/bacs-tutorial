package istu.bacs.submissionchecker;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableRabbit
@EnableScheduling
@EnableTransactionManagement
@EntityScan("istu.bacs.db")
@ComponentScan({
        "istu.bacs.submissionchecker",
        "istu.bacs.commons",
        "istu.bacs.db",
        "istu.bacs.externalapi",
        "istu.bacs.sybon",
        "istu.bacs.rabbit"})
public class SubmissionCheckerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubmissionCheckerApplication.class, args);
    }
}