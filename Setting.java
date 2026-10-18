package pl.eclipse.client;

/** Ustawienie modułu: przełącznik, suwak albo kolor. Wartości zapisują się do JSON-a jako Boolean/Double/String. */
public abstract class Setting {
    public final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public abstract Object save();

    public abstract void load(Object o);

    public static final class Bool extends Setting {
        public boolean value;

        public Bool(String name, boolean def) {
            super(name);
            this.value = def;
        }

        @Override public Object save() { return value; }

        @Override public void load(Object o) {
            if (o instanceof Boolean b) value = b;
        }
    }

    public static final class Num extends Setting {
        public double value;
        public final double min, max, step;

        public Num(String name, double def, double min, double max, double step) {
            super(name);
            this.value = def;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        public void set(double v) {
            if (step > 0) v = Math.round(v / step) * step;
            value = Math.max(min, Math.min(max, v));
        }

        public int i() { return (int) Math.round(value); }

        @Override public Object save() { return value; }

        @Override public void load(Object o) {
            if (o instanceof Number n) set(n.doubleValue());
        }
    }

    public static final class Color extends Setting {
        public float hue, sat, bri;

        public Color(String name, int rgb) {
            super(name);
            setRgb(rgb);
        }

        public int rgb() {
            return hsb(hue, sat, bri);
        }

        public void setRgb(int rgb) {
            float r = ((rgb >> 16) & 255) / 255f, g = ((rgb >> 8) & 255) / 255f, b = (rgb & 255) / 255f;
            float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
            float d = max - min;
            bri = max;
            sat = max == 0 ? 0 : d / max;
            float h;
            if (d == 0) h = 0;
            else if (max == r) h = ((g - b) / d) % 6f;
            else if (max == g) h = (b - r) / d + 2f;
            else h = (r - g) / d + 4f;
            h /= 6f;
            if (h < 0) h += 1f;
            hue = h;
        }

        public static int hsb(float h, float s, float v) {
            h = h - (float) Math.floor(h);
            float c = v * s;
            float x = c * (1 - Math.abs((h * 6f) % 2f - 1));
            float m = v - c;
            float r, g, b;
            int sector = (int) (h * 6f);
            switch (sector) {
                case 0: r = c; g = x; b = 0; break;
                case 1: r = x; g = c; b = 0; break;
                case 2: r = 0; g = c; b = x; break;
                case 3: r = 0; g = x; b = c; break;
                case 4: r = x; g = 0; b = c; break;
                default: r = c; g = 0; b = x; break;
            }
            int ri = Math.round((r + m) * 255), gi = Math.round((g + m) * 255), bi = Math.round((b + m) * 255);
            return (ri << 16) | (gi << 8) | bi;
        }

        @Override public Object save() { return String.format("#%06X", rgb() & 0xFFFFFF); }

        @Override public void load(Object o) {
            if (o instanceof String s && s.startsWith("#")) {
                try {
                    setRgb(Integer.parseInt(s.substring(1), 16));
                } catch (NumberFormatException ignored) {
                    // zostaje poprzednia wartość
                }
            }
        }
    }
}
