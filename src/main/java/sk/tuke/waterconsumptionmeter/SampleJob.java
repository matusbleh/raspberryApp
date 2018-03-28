package sk.tuke.waterconsumptionmeter;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import sk.tuke.waterconsumptionmeter.model.Image;
import sk.tuke.waterconsumptionmeter.util.CameraUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

//import nu.pattern.OpenCV;
//import org.opencv.core.Mat;

public class SampleJob {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Logger log = LoggerFactory.getLogger(SampleJob.class);
    private CameraUtil cameraUtil = new CameraUtil();

    public static String imgToBase64String(final RenderedImage img, final String formatName) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, formatName, Base64.getEncoder().wrap(os));
            return os.toString(StandardCharsets.ISO_8859_1.name());
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static BufferedImage base64StringToImg(final String base64String) {
        try {
            return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public void doSience(String name) {
//        RPiCamera piCamera = initPiCamera("/home/pi/Pictures/dp/");
//        BufferedImage bufferedImage = takePicture(piCamera,name);
        BufferedImage bufferedImage = cameraUtil.takePicture();
        Image image = new Image(name, imgToBase64String(bufferedImage, "png"));
//        postEntity(new Image("obrazok",name));
        postEntity(image);
//        OpenCV.loadShared();
//        Mat imgGray = new Mat();
//        log.info("Opencv loaded.");
//        Imgproc.cvtColor(BufferedImage2Mat(image), imgGray, Imgproc.COLOR_BGR2GRAY);
//        Imgcodecs.imwrite("./Pictures/gray" + name + ".png", imgGray);

//        not working
//        String result = tesseract(image);
    }

    private String tesseract(BufferedImage image) {
        String result = null;
        ITesseract instance = new Tesseract();
        try {
            instance.setDatapath("./tessdata");
            instance.setLanguage("ENG");
            result = instance.doOCR(image);
            log.info("OCR", result);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        return result;
    }
//    public static opencv_core.Mat BufferedImage2Mat(BufferedImage image) throws IOException {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        ImageIO.write(image, "jpg", byteArrayOutputStream);
//        byteArrayOutputStream.flush();
//        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
//    }

    private BufferedImage takePicture(RPiCamera piCamera, String name) {
        BufferedImage image = null;
        try {
            name = name.replaceAll(" ", "_");
            image = piCamera.takeBufferedStill();
//            log.warn("New image try.");
//            piCamera.takeStill(name+".png");
//            log.warn("New image saved. /home/pi/Pictures/dp/"+name+".png");
        } catch (InterruptedException | IOException e) {
            log.error("Failed taking Picture.", e);
        }
        return image;
    }

    private RPiCamera initPiCamera(String saveDir) {
        RPiCamera piCamera = null;
        try {
            piCamera = new RPiCamera(saveDir);
        } catch (FailedToRunRaspistillException e) {
            log.error("Failed to run raspistill.", e);
        }
        return piCamera;
    }

    //    public void postEntity(Image image){
////        System.out.println("Begin /POST request!");
//        String postUrl = "http://192.168.10.2:9090/post";
////        String name = "1.png";
////        String imagePath = "C:\\client\\1.png";
////        String data = UtilBase64Image.encoder(imagePath);
////
////        System.out.println("Post Image'info: name = " + name + " ,data = " + data);
////        Image customer = new Image(name, data);
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> postResponse = restTemplate.postForEntity(postUrl, image, String.class);
//        System.out.println("Response for Post Request: " + postResponse.getBody());
//    }
    public void postEntity(Image image) {
        String postUrl = "http://192.168.10.2:9090/post";
        String postUrl2 = "http://192.168.1.2:9090/post";
        RestTemplate restTemplate = new RestTemplate();
        try {
            log.info("sending " + postUrl, image);
            ResponseEntity<String> postResponse = restTemplate.postForEntity(postUrl, image, String.class);
            log.info("Response for Post Request: " + postResponse.getBody());
        } catch (Exception e) {
            log.error("No response " + e.getMessage());
        }
        try {
            log.info("sending " + postUrl2, image);
            ResponseEntity<String> postResponse = restTemplate.postForEntity(postUrl2, image, String.class);
            log.info("Response for Post Request2: " + postResponse.getBody());
        } catch (Exception e) {
            log.error("No response " + e.getMessage());
        }
    }
}
