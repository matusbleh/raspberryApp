package sk.tuke.waterconsumptionmeter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sk.tuke.waterconsumptionmeter.SampleJob;

@Component
public class Printer {
    private final Logger log = LoggerFactory.getLogger(SampleJob.class);

    public void print() {
        log.info("printer");
    }
}
