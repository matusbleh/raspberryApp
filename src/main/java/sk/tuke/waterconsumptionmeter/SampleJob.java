package sk.tuke.waterconsumptionmeter;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.lept.pixRead;
import static org.bytedeco.javacpp.lept.pixReadMem;

public class SampleJob extends QuartzJobBean {

    private String name;

    public static lept.PIX convertMatToPix(Mat mat) {
        MatOfByte bytes = new MatOfByte();
        Imgcodecs.imencode(".png", mat, bytes);
        ByteBuffer buff = ByteBuffer.wrap(bytes.toArray());
        return pixReadMem(buff, buff.capacity());
    }

    // Invoked if a Job data map entry with that name
    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(".", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }
//		api.SetVariable("tessedit_char_whitelist","0123456789,.");

        Mat img = Imgcodecs.imread("src/main/resources/images/2.jpg");
        find(img, api, ".1original");

        Mat imgGray = new Mat();
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
        find(imgGray, api, ".2gray");

//		Mat imgGaussianBlur = new Mat();
//		Imgproc.GaussianBlur(imgGray,imgGaussianBlur,new Size(3, 3),0);
//		find(imgGaussianBlur, api, ".3gaussian_blur");

        Mat imgGaussianBlur = new Mat();
        Imgproc.medianBlur(imgGray, imgGaussianBlur, 5);
        find(imgGaussianBlur, api, ".3gaussian_blur");
        Mat imgGaussianBlur2 = new Mat();
        Imgproc.medianBlur(imgGaussianBlur, imgGaussianBlur2, 5);
        find(imgGaussianBlur2, api, ".31gaussian_blur");

        Mat imgAdaptiveThreshold = new Mat();
        Imgproc.adaptiveThreshold(imgGaussianBlur, imgAdaptiveThreshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 1, 1);
        find(imgAdaptiveThreshold, api, ".4adaptive_threshold");

        Mat imgM = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(15, 3));
        Imgproc.morphologyEx(imgAdaptiveThreshold, imgM, Imgproc.MORPH_OPEN, kernel);
        find(imgM, api, ".5morph_erode");

        Mat imgM2 = new Mat();
        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(5, 5));
        Imgproc.morphologyEx(imgM, imgM2, Imgproc.MORPH_CLOSE, kernel2);
        find(imgM2, api, ".6morph_closed");

        Mat imgM3 = new Mat();
        Mat kernel3 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
        Imgproc.morphologyEx(imgAdaptiveThreshold, imgM3, Imgproc.MORPH_DILATE, kernel3);
        find(imgM3, api, ".7morph_open");
        api.End();
    }

    private void find(@Nullable Mat image, tesseract.TessBaseAPI api, String name) {
        Imgcodecs.imwrite("src/main/resources/images/" + name + ".png", image);
        lept.PIX img = pixRead("src/main/resources/images/" + name + ".png");
//		lept.PIX img = pixRead("src/main/resources/images/morph_closed.png");
//		lept.PIX img = convertMatToPix(image);

        if (img != null) {
            api.SetImage(img);
            // Get OCR result
            BytePointer outText;
            outText = api.GetUTF8Text();
            String string = outText.getString();
            System.out.println(name + ",OCR output:\n" + string);
            outText.deallocate();
        }
    }
}
