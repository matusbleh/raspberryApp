package sk.tuke.waterconsumptionmeter;

import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.lept.pixReadMem;

/**
 * Created by matus on 17.3.2018.
 */
public class OldSampleJob extends QuartzJobBean {

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
//        api.SetVariable("tessedit_char_whitelist", "0123456789");

        Mat img = Imgcodecs.imread("src/main/resources/images/9.png");
        find(img, api, ".1numbers");

        Mat imgGray = new Mat();
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
        find(imgGray, api);

//        Mat imgGaussianBlur = new Mat();
//        Imgproc.GaussianBlur(imgGray, imgGaussianBlur, new Size(5, 5), 0);
//        find(imgGaussianBlur, api, ".2numbers");
//
//        Mat imgAdaptiveThreshold = new Mat();
//        Imgproc.adaptiveThreshold(imgGaussianBlur, imgAdaptiveThreshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
//        find(imgAdaptiveThreshold, api, ".4");
//
//        Mat imgM = new Mat();
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
//        Imgproc.morphologyEx(imgAdaptiveThreshold, imgM, Imgproc.MORPH_OPEN, kernel);
//        find(imgM, api, ".5");
//
//        Mat imgGaussianBlur2 = new Mat();
//        Imgproc.GaussianBlur(imgGray, imgGaussianBlur2, new Size(3, 3), 0);
//        find(imgGaussianBlur2, api, ".6");
//
//        Mat imgC = new Mat();
//        Imgproc.threshold(imgGaussianBlur2, imgC, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
//        find(imgC, api, ".7");
//
//        Mat imgD = new Mat();
//        Imgproc.threshold(imgGaussianBlur, imgD, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
//        find(imgD, api, ".8");
//
//        Mat canny_output;
//
//        /// Find contours
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(imgC, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        System.out.println("contours size " + contours.size());
//        /// Draw contours
//
//        System.out.println("size:" + contours.size());

        api.End();
        System.out.println("end");
    }

    private void find(@Nullable Mat image, tesseract.TessBaseAPI api, String name) {
        Imgcodecs.imwrite("src/main/resources/images/" + name + ".png", image);
//        lept.PIX img = pixRead("src/main/resources/images/" + name + ".png");
//		lept.PIX img = pixRead("src/main/resources/images/morph_closed.png");
//		lept.PIX img = convertMatToPix(image);
        System.out.println(name);
//        if (img != null) {
//            api.SetImage(img);
//            // Get OCR result
//            BytePointer outText;
//            outText = api.GetUTF8Text();
//            String string = outText.getString();
//            System.out.println(name + ",OCR output:\n" + string);
//            outText.deallocate();
//        }
    }

    private void test() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // reading image
        Mat image = Imgcodecs.imread("src/main/resources/images/x.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        // clone the image
        Mat original = image.clone();
        // thresholding the image to make a binary image
        Imgproc.threshold(image, image, 100, 128, Imgproc.THRESH_BINARY_INV);
        // find the center of the image
        double[] centers = {(double) image.width() / 2, (double) image.height() / 2};
        Point image_center = new Point(centers);

        // finding the contours
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // finding best bounding rectangle for a contour whose distance is closer to the image center that other ones
        double d_min = Double.MAX_VALUE;
        Rect rect_min = new Rect();
        System.out.println(contours.size());
        for (MatOfPoint contour : contours) {
            Rect rec = Imgproc.boundingRect(contour);
            MatOfPoint2f dst = new MatOfPoint2f();
            contour.convertTo(dst, CvType.CV_32F);
            RotatedRect rect = Imgproc.minAreaRect(dst);
            System.out.println("find the best candidates");

            Mat result = original.submat(rec);
            Imgcodecs.imwrite("src/main/resources/images/c/" + contours.indexOf(contour) + ".png", result);
        }
//        int pad = 5;
//        rect_min.x = rect_min.x - pad;
//        rect_min.y = rect_min.y - pad;
//
//        rect_min.width = rect_min.width + 2*pad;
//        rect_min.height = rect_min.height + 2*pad;
//
//        Mat result = original.submat(rect_min);
//        Imgcodecs.imwrite("src/main/resources/images/xx.png", result);


    }

    private void find(Mat imgGray, tesseract.TessBaseAPI api) {

//		Mat imgGaussianBlur = new Mat();
//		Imgproc.GaussianBlur(imgGray,imgGaussianBlur,new Size(3, 3),0);
//		find(imgGaussianBlur, api, ".3gaussian_blur");

        Mat imgGaussianBlur = new Mat();
        Imgproc.GaussianBlur(imgGray, imgGaussianBlur, new Size(3, 3), 0);
        find(imgGaussianBlur, api, ".3");

        Mat imgAdaptiveThreshold = new Mat();
        Imgproc.adaptiveThreshold(imgGaussianBlur, imgAdaptiveThreshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 153, 11);
        find(imgAdaptiveThreshold, api, ".4");

        Mat imgM = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
        Imgproc.morphologyEx(imgAdaptiveThreshold, imgM, Imgproc.MORPH_OPEN, kernel);
        find(imgM, api, ".5");

        Mat imgGaussianBlur2 = new Mat();
        Imgproc.GaussianBlur(imgGray, imgGaussianBlur2, new Size(5, 5), 0);
        find(imgGaussianBlur2, api, ".6");

        Mat imgC = new Mat();
        Imgproc.threshold(imgGaussianBlur2, imgC, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        find(imgC, api, ".7");

        Mat imgD = new Mat();
        Imgproc.threshold(imgGaussianBlur, imgD, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        find(imgD, api, ".8");

        Mat canny_output;

        /// Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgC, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("contours size " + contours.size());
        /// Draw contours

        System.out.println("size:" + contours.size());
        for (int i = 0; i < contours.size(); i++) {
//        for (int i = 0; i < 50; i++) {

            Mat dest = new Mat();
            imgD.convertTo(dest, CvType.CV_32SC1);
            Mat contourImg = new Mat(dest.size(), dest.type());
            Imgproc.drawContours(contourImg, contours, i, new Scalar(255, 255, 255), -1);
//            find(contourImg, api, "c/contour" + i);
            Rect rec = Imgproc.boundingRect(contours.get(i));

            Mat result = new Mat();
            Mat result2 = new Mat();
//            Imgcodecs.imwrite("src/main/resources/images/c/contour"+ i +"x.png", result);
            //
            // rect is the RotatedRect (I got it from a contour...)
            MatOfPoint2f dst = new MatOfPoint2f();
            contours.get(i).convertTo(dst, CvType.CV_32F);
            RotatedRect rect = Imgproc.minAreaRect(dst);
            if (rect.size.width > 5 && rect.size.height > 5) {
                // matrices we'll use
                Mat M, rotated = new Mat(), rotated2 = new Mat();
                // get angle and size from the bounding box
                double angle = rect.angle;
                Size rect_size = rect.size;
                // thanks to http://felix.abecassis.me/2011/10/opencv-rotation-deskewing/
                if (rect.angle < -45.) {
                    angle += 90.0;
                    double x = rect_size.width;
                    rect_size.width = rect_size.height;
                    rect_size.height = x;
                }
                //get the rotation matrix
                M = Imgproc.getRotationMatrix2D(rect.center, angle, 1.0);
                ////////////////////////////////////////////////////////////////////////////////
                // perform the affine transformation
                Imgproc.warpAffine(imgC, rotated, M, imgC.size(), Imgproc.INTER_CUBIC);
                Imgproc.warpAffine(imgD, rotated2, M, imgD.size(), Imgproc.INTER_CUBIC);
                // crop the resulting image
                Imgproc.getRectSubPix(rotated, rect_size, rect.center, result);
                Imgproc.getRectSubPix(rotated2, rect_size, rect.center, result2);

            /*MatOfPoint2f dst = new MatOfPoint2f();
            contours.get(i).convertTo(dst, CvType.CV_32F);
            RotatedRect rect = Imgproc.minAreaRect(dst);
            if (rect.angle < -45.) {
                rect.angle += 90.0;
                double x = rect.size.width;
                rect.size.width = rect.size.height;
                rect.size.height = x;
            }

            Mat m = Imgproc.getRotationMatrix2D(rect.center, rect.angle, 1.0);
            // perform the affine transformation
            Imgproc.warpAffine(imgC, result, m, imgC.size(), Imgproc.INTER_CUBIC);
            // crop the resulting image
            Mat result2 = new Mat();
            Imgproc.getRectSubPix(result, rect.size, rect.center, result2);*/
                if (result.size().width < result.size().height) {
                    Core.rotate(result, result, Core.ROTATE_90_COUNTERCLOCKWISE);
                    Core.rotate(result2, result2, Core.ROTATE_90_COUNTERCLOCKWISE);
                }
//                if(result.size().width<result.size().height*6&&result.size().width>result.size().height*3) {
//                    find(result, api, "c/contour" + i );
//                    find(result2, api, "c/contour" + i +"x");
//                }
            }
        }

//        find(contourImg, api, ".10");
        // approximates a polygonal curve with the specified precision
//            MatOfPoint2f approxCurve = null;
//            Imgproc.approxPolyDP(
//                    curve,
//                    approxCurve,
//                    0.02 * Imgproc.arcLength(curve, true),
//                    true
//            );

//        test();
    }
}