package packWork;

import java.awt.image.RescaleOp;

public class Contrast extends Effect {

    private float level;

    public Contrast() {
    }

    public Contrast(float level) {
        this.level = level;
    }

    protected void executeEffect() {
        RescaleOp rescaleOp = new RescaleOp(this.level, 0, null);
        rescaleOp.filter(getImage(), getImage());
    }
}
