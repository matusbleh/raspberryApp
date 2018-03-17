package sk.tuke.waterconsumptionmeter;

import org.bytedeco.javacpp.tesseract;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;

public class SampleJob extends QuartzJobBean {

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
            }
        }

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
