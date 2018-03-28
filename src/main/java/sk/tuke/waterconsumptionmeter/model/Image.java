package sk.tuke.waterconsumptionmeter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by matus on 19.3.2018.
 */
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String data;

    @Override
    public String toString() {
        String info = String.format("Image name = %s, data = %s", name, data);
        return info;
    }
}
