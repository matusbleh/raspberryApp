package sk.tuke.waterconsumptionmeter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sk.tuke.waterconsumptionmeter.SampleJob;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by matus on 19.3.2018.
 */
@Component
public class ScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final SampleJob job = new SampleJob();

    //    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(fixedRate = 100L)
    public void scheduleTaskWithCronExpression() {
        logger.info("Cron Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        try {
            job.doSience(dateTimeFormatter.format(LocalDateTime.now()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
