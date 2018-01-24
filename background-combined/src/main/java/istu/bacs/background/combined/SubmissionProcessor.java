package istu.bacs.background.combined;

import istu.bacs.background.combined.db.SubmissionService;
import istu.bacs.db.submission.Submission;
import istu.bacs.db.submission.Verdict;
import istu.bacs.rabbit.QueueName;
import istu.bacs.rabbit.RabbitService;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SubmissionProcessor implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private static final int tickDelay = 500;
    //print state once per 5 minutes
    private static final int printStateEveryNTicks = (1000 / tickDelay) * 60 * 5;
    private static final int maxSubmissionsPerBatch = 10;

    private final SubmissionService submissionService;
    private final RabbitService rabbitService;

    private final Queue<Integer> q = new ConcurrentLinkedDeque<>();
    private final AtomicInteger tickCount = new AtomicInteger();

    private SubmissionProcessor self;
    private AtomicBoolean initialized = new AtomicBoolean();

    public SubmissionProcessor(SubmissionService submissionService, RabbitService rabbitService) {
        this.submissionService = submissionService;
        this.rabbitService = rabbitService;
    }

    @Scheduled(fixedDelay = tickDelay)
    public void tick() {
        if (q.isEmpty()) {
            if (tickCount.incrementAndGet() == printStateEveryNTicks) {
                log().info("Nothing to process");
                tickCount.set(0);
            }
            return;
        }

        tickCount.set(0);

        log().info(processorName() + " tick started");
        self.processAll();
        log().info(processorName() + " tick finished");
    }

    @Transactional
    public void processAll() {
        Set<Integer> ids = new HashSet<>();
        while (!q.isEmpty() && ids.size() < maxSubmissionsPerBatch) ids.add(q.poll());

        try {
            List<Submission> submissions = submissionService.findAllByIds(new ArrayList<>(ids));

            process(submissions);

            for (Submission submission : submissions) {
                int submissionId = submission.getSubmissionId();
                if (submission.getVerdict() != incomingVerdict()) {
                    rabbitService.send(outcomingQueueName(), submissionId);
                    log().debug("Submission {} processed: {}", submissionId, submission.getVerdict());
                } else {
                    log().debug("Submission {} NOT processed: {}", submissionId, incomingVerdict());
                    q.add(submissionId);
                }
            }
        } catch (Exception e) {
            log().warn("Unable to process submissions", e);
            q.addAll(ids);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        self = applicationContext.getBean(processorName(), SubmissionProcessor.class);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialized.getAndSet(true))
            return;

        submissionService.findAllByVerdict(incomingVerdict())
                .forEach(s -> q.add(s.getSubmissionId()));
        rabbitService.subscribe(incomingQueueName(), q::add);
    }

    protected abstract void process(List<Submission> submissions);

    protected abstract Verdict incomingVerdict();

    protected abstract QueueName incomingQueueName();

    protected abstract QueueName outcomingQueueName();

    protected abstract String processorName();

    protected abstract Logger log();
}