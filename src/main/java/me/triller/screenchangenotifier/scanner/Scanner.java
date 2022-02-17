package me.triller.screenchangenotifier.scanner;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;

@EnableScheduling
@Configuration
public class Scanner {

    private boolean positionAlreadyTriggered = false;
    private boolean iobrokerTriggered = false;
    private boolean comparisonInvalid = true;
    private int[] prevPos = new int[2];
    private int[] prevRGBA = new int[4];

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void scheduledScan() {
        try {
            doScan();
        } catch (AWTException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void doScan() throws AWTException, URISyntaxException {
        if(iobrokerTriggered) {
            triggerIOBrokerEvent(false);
            iobrokerTriggered = false;
        }

        Point cursor = MouseInfo.getPointerInfo().getLocation();
        int[] pos = new int[]{cursor.x, cursor.y};
        if(positionChanged(pos)) {
            System.out.println("Position changed, aborting scan");
            comparisonInvalid = true;
            positionAlreadyTriggered = false;
        } else {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage img = new Robot().createScreenCapture(screenRect);
            FastRGB fastRGB = new FastRGB(img);
            int[] rgba = fastRGB.getRGBA(pos[0], pos[1]);

            System.out.println("Scanning...");
            if (isTriggered(pos, rgba)) {
                if(!positionAlreadyTriggered) {
                    System.out.println("Value changed on same position!");
                    printInfo(prevPos, prevRGBA);
                    printInfo(pos, rgba);
                    triggerIOBrokerEvent(true);
                    iobrokerTriggered = true;
                    positionAlreadyTriggered = true;
                } else {
                    System.out.println("Already triggered here!");
                }

            }

            prevRGBA = rgba;
            comparisonInvalid = false;
        }

        prevPos = pos;
    }

    private void printInfo(int[] pos, int[] rgba) {
        System.out.printf("X: %d, Y: %d, RGBA: %d;%d;%d;%d;\n", pos[0], pos[1], rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    private boolean positionChanged(int[] pos) {
        return !(prevPos[0] == pos[0] && prevPos[1] == pos[1]);
    }

    private boolean colorChanged(int[] rgba) {
        if(comparisonInvalid) {
            return false;
        }
        return !(prevRGBA[0] == rgba[0] && prevRGBA[1] == rgba[1] && prevRGBA[2] == rgba[2] && prevRGBA[3] == rgba[3]);
    }

    private boolean isTriggered(int[] pos, int[] rgba) {
        return !positionChanged(pos) && colorChanged(rgba);
    }

    private void triggerIOBrokerEvent(boolean value) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        URI uri = new URI("http://192.168.0.201:8087/set/javascript.0.screenchangenotifier.lostark?value=" + value);
        restTemplate.getForEntity(uri, String.class);
    }
}
