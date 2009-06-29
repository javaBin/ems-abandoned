/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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