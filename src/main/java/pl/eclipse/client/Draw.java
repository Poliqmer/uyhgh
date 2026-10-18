package pl.eclipse.client;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

/** Cienka warstwa nad DrawContext - każde wywołanie jest osłonięte, żeby błąd API nie wywalał gry. */
public final class Draw {
    private static boolean fillBroken, textBroken;
    private static class_327 fallbackFont;

    private Draw() {}

    public static void setFont(class_327 f) {
        if (f != null) fallbackFont = f;
    }

    public static class_327 font(class_310 mc) {
        if (fallbackFont != null) return fallbackFont;
        try {
            class_327 f = mc.field_1772; // textRenderer (nazwa pośrednia z pamięci - przy błędzie zostaje czcionka z GUI)
            if (f != null) fallbackFont = f;
        } catch (Throwable ignored) {
            // brak czcionki - tekst HUD nie zostanie narysowany, dopóki nie otworzysz GUI
        }
        return fallbackFont;
    }

    public static void fill(class_332 c, int x1, int y1, int x2, int y2, int argb) {
        if (fillBroken || x2 <= x1 || y2 <= y1 || (argb >>> 24) == 0) return;
        try {
            c.method_25294(x1, y1, x2, y2, argb);
        } catch (Throwable t) {
            fillBroken = true;
            EclipseClient.LOGGER.warn("fill niedostepne: {}", t.toString());
        }
    }

    public static void vgrad(class_332 c, int x, int y, int w, int h, int top, int bottom) {
        int step = h > 120 ? 3 : 1;
        for (int i = 0; i < h; i += step) {
            float t = h <= 1 ? 0 : (float) i / (h - 1);
            fill(c, x, y + i, x + w, y + Math.min(h, i + step), lerp(top, bottom, t));
        }
    }

    public static void hgrad(class_332 c, int x, int y, int w, int h, int left, int right) {
        int seg = Math.max(1, Math.min(w, 48));
        for (int i = 0; i < seg; i++) {
            int x1 = x + w * i / seg, x2 = x + w * (i + 1) / seg;
            fill(c, x1, y, x2, y + h, lerp(left, right, seg <= 1 ? 0 : (float) i / (seg - 1)));
        }
    }

    public static void outline(class_332 c, int x, int y, int w, int h, int argb) {
        fill(c, x, y, x + w, y + 1, argb);
        fill(c, x, y + h - 1, x + w, y + h, argb);
        fill(c, x, y, x + 1, y + h, argb);
        fill(c, x + w - 1, y, x + w, y + h, argb);
    }

    public static void text(class_332 c, class_310 mc, String s, int x, int y, int argb) {
        class_327 f = font(mc);
        text(c, f, s, x, y, argb);
    }

    public static void text(class_332 c, class_327 f, String s, int x, int y, int argb) {
        if (textBroken || f == null || s == null || s.isEmpty() || (argb >>> 24) == 0) return;
        try {
            c.method_27535(f, class_2561.method_43470(s), x, y, argb);
        } catch (Throwable t) {
            textBroken = true;
            EclipseClient.LOGGER.warn("tekst niedostepny: {}", t.toString());
        }
    }

    public static int width(class_310 mc, String s) {
        return width(font(mc), s);
    }

    public static int width(class_327 f, String s) {
        if (f == null || s == null) return (s == null ? 0 : s.length() * 6);
        try {
            return f.method_27525(class_2561.method_43470(s));
        } catch (Throwable t) {
            return s.length() * 6;
        }
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));
        int aa = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
        int rr = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int gg = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bb = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (aa << 24) | (rr << 16) | (gg << 8) | bb;
    }

    public static int alpha(int argb, float mul) {
        int a = (int) (((argb >>> 24) & 255) * Math.max(0, Math.min(1, mul)));
        return (a << 24) | (argb & 0xFFFFFF);
    }
}
