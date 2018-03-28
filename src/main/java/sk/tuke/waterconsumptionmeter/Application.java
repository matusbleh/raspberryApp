package sk.tuke.waterconsumptionmeter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import sk.tuke.waterconsumptionmeter.util.UtilMarker;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackageClasses = UtilMarker.class)
public class Application {

    public static void main(String[] args) {
        //        OpenCV.loadShared();
        SpringApplication.run(Application.class, args);
//        /*
//         *POST ENTITY
//         */
//
//        RestfulClient restfulClient = new RestfulClient();
//
//        restfulClient.postEntity();
//
//        /*
//         * GET ENTITY
//         */
//        restfulClient.getEntity();
    }
}
