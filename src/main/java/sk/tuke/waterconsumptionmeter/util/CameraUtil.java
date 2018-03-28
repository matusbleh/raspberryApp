package sk.tuke.waterconsumptionmeter.util;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.tuke.waterconsumptionmeter.SampleJob;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class CameraUtil {
    private final Logger log = LoggerFactory.getLogger(SampleJob.class);
    private RPiCamera piCamera;

    public CameraUtil() {
        String saveDir = "/home/pi/Pictures/dp/";
        try {
            piCamera = new RPiCamera(saveDir);
        } catch (FailedToRunRaspistillException e) {
            log.error("Failed to run raspistill.");
        } catch (Exception e) {
            log.error("Failed to take image.");
        }
    }

    public BufferedImage takePicture() {
        BufferedImage image = null;
        try {
//            name = name.replaceAll(" ","_");
            image = piCamera.takeBufferedStill();
//            log.warn("New image try.");
//            piCamera.takeStill(name+".png");
//            log.warn("New image saved. /home/pi/Pictures/dp/"+name+".png");
        } catch (InterruptedException | IOException e) {
            log.error("Failed taking Picture.");
        } catch (Exception e) {
            log.error("Failed to take image.");
        }
        return image;
    }
}
