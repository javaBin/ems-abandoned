package no.java.ems.client.swing;

import no.java.swing.ApplicationTask;
import no.java.ems.domain.Binary;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


public class ImageLoaderTask extends ApplicationTask<BufferedImage, Void> {
    private final Binary binary;

    public ImageLoaderTask(Binary binary) {
        super("no.java.ems.client.swing.ImageLoaderTask");
        this.binary = binary;
    }

    protected BufferedImage doInBackground() throws Exception {
        BufferedImage bufferedImage = ImageIO.read(binary.getDataStream());
        for (int i = 0; i < binary.getSize(); i++) {
            setProgress(i, 0, binary.getSize());            
        }
        return bufferedImage;
    }
    
}