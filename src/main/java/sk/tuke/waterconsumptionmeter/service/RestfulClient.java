package sk.tuke.waterconsumptionmeter.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import sk.tuke.waterconsumptionmeter.model.Image;
import sk.tuke.waterconsumptionmeter.util.UtilBase64Image;

/**
 * Created by matus on 19.3.2018.
 */
public class RestfulClient {
    RestTemplate restTemplate;

    public RestfulClient() {
        restTemplate = new RestTemplate();
    }

    /**
     * post entity
     */
    public void postEntity(Image image) {
//        System.out.println("Begin /POST request!");
        String postUrl = "http://192.168.10.2:9090/post";
//        String name = "1.png";
//        String imagePath = "C:\\client\\1.png";
//        String data = UtilBase64Image.encoder(imagePath);
//
//        System.out.println("Post Image'info: name = " + name + " ,data = " + data);
//        Image customer = new Image(name, data);

        ResponseEntity<String> postResponse = restTemplate.postForEntity(postUrl, image, String.class);
        System.out.println("Response for Post Request: " + postResponse.getBody());
    }

    /**
     * get entity
     */
    public void getEntity() {
        System.out.println("Begin /GET request!");
        String getUrl = "http://localhost:9090/get?name=1.png";
        ResponseEntity<Image> getResponse = restTemplate.getForEntity(getUrl, Image.class);

        if (getResponse.getBody() != null) {
            Image image = getResponse.getBody();
            System.out.println("Response for Get Request: " + image.toString());
            System.out.println("Save Image to C:\\client\\get");
            UtilBase64Image.decoder(image.getData(), "C:\\client\\get\\" + image.getName());
            System.out.println("Done!");
        } else {
            System.out.println("Response for Get Request: NULL");
        }
    }
}
