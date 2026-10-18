package pl.eclipse.client;

import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_332;

/** Kontekst rysowania HUD i wspólne pomocniki (ramka z tekstem, pozycja z ustawień X/Y). */
public final class Hud {
    public final class_332 ctx;
    public final class_310 mc;
    public final int sw, sh;

    public Hud(class_332 ctx, class_310 mc, int sw, int sh) {
        this.ctx = ctx;
        this.mc = mc;
        this.sw = sw;
        this.sh = sh;
    }

    public static int accent() {
        return 0xFF000000 | Interface.accent.rgb();
    }

    public static int bg() {
        return ((int) (Interface.opacity.value * 255) << 24) | 0x0A0C12;
    }

    /** Ramka z listą linii; X/Y w procentach wolnego miejsca na ekranie. */
    public void box(Setting.Num xs, Setting.Num ys, List<String> lines) {
        int w = 0;
        for (String l : lines) w = Math.max(w, Draw.width(mc, l));
        int pad = 4;
        int bw = w + pad * 2 + 3, bh = lines.size() * 10 + pad * 2 - 1;
        int x = (int) ((sw - bw) * xs.value / 100.0);
        int y = (int) ((sh - bh) * ys.value / 100.0);
        Draw.fill(ctx, x, y, x + bw, y + bh, bg());
        Draw.fill(ctx, x, y, x + 2, y + bh, accent());
        for (int i = 0; i < lines.size(); i++) {
            Draw.text(ctx, mc, lines.get(i), x + pad + 3, y + pad + i * 10, 0xFFEFF2F8);
        }
    }
}
