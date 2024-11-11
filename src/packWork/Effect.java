package packWork;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class Effect {
    private File imageFile;
    private BufferedImage image;

    protected abstract void executeEffect();

    public void applyEffect() {
        if (getImage() != null)
            executeEffect();
    }

    public void applyEffect(File imageFile) {
        setPhoto(imageFile);

        if (getImage() != null)
            executeEffect();
    }

    private void readImage() {
        try {
            setImage(ImageIO.read(getImageFile()));
        } catch (IOException e) {
            setImage(null);
            e.printStackTrace();
        }
    }

    public String saveImage() {

        long startTime = System.currentTimeMillis();

        File fileToSave = null;
        int index = 0;
        do {
            String newName = imageFile.getName().substring(0, imageFile.getName().lastIndexOf("."));

            newName = imageFile.getParent() + File.separator + newName + "_" + index + ".bmp";
            fileToSave = new File(newName);
            index++;
        } while (fileToSave.exists());

        try {
            ImageIO.write(image, "BMP", fileToSave);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Execution time: " + (System.currentTimeMillis() - startTime));
        return fileToSave != null ? fileToSave.getName() : null;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setPhoto(File imageFile) {
        if (imageFile == null)
            return;

        if (this.getImageFile() == null || !imageFile.getAbsolutePath().equals(this.getImageFile().getAbsolutePath())) {
            this.imageFile = imageFile;
            readImage();
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    protected void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
