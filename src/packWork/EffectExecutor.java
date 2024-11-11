package packWork;

public class EffectExecutor<T extends Effect> implements Interface {
    private Effect effect;

    public EffectExecutor(T t) {
        this.effect = t;
    }

    public void applyEffect() {
        effect.applyEffect();
        System.out.println("Effect saved in the image: " + effect.saveImage());
    }
}
