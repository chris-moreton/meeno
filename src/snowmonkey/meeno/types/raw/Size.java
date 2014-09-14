package snowmonkey.meeno.types.raw;

import snowmonkey.meeno.types.MicroType;

public class Size extends MicroType<Double> {
    public Size(Double value) {
        super(value);
    }

    public double asDouble() {
        return value;
    }

    public static Size size(double v) {
        return new Size(v);
    }
}