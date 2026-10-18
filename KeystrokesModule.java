package pl.eclipse.client.modules;

import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class KeystrokesModule extends Module {
    private final Setting.Num px = num("X", 3, 0, 100, 1);
    private final Setting.Num py = num("Y", 55, 0, 100, 1);
    private final Setting.Num size = num("Key size", 20, 14, 30, 1);
    private final Setting.Bool mouse = bool("Show mouse + CPS", true);

    public KeystrokesModule() {
        super("Keystrokes", "Wizualizacja klawiszy WASD, spacji i myszy", Category.HUD);
    }

    private void key(Hud h, int x, int y, int w, int hgt, String label, boolean down, String sub) {
        int bg = down ? (0xCC000000 | Interface.accent.rgb()) : Hud.bg();
        Draw.fill(h.ctx, x, y, x + w, y + hgt, bg);
        Draw.outline(h.ctx, x, y, w, hgt, down ? 0xFFFFFFFF : 0x40FFFFFF);
        int tw = Draw.width(h.mc, label);
        int ty = sub == null ? y + (hgt - 8) / 2 : y + 3;
        Draw.text(h.ctx, h.mc, label, x + (w - tw) / 2, ty, 0xFFFFFFFF);
        if (sub != null) {
            int sw = Draw.width(h.mc, sub);
            Draw.text(h.ctx, h.mc, sub, x + (w - sw) / 2, y + hgt - 11, 0xFFB8BECC);
        }
    }

    @Override public void onHud(Hud h) {
        int k = size.i(), g = 2;
        int totalW = k * 3 + g * 2;
        int totalH = k * 2 + g * 2 + (mouse.value ? k + g : 0) + 0;
        int x0 = (int) ((h.sw - totalW) * px.value / 100.0);
        int y0 = (int) ((h.sh - totalH) * py.value / 100.0);
        key(h, x0 + k + g, y0, k, k, "W", Input.key(87), null);
        key(h, x0, y0 + k + g, k, k, "A", Input.key(65), null);
        key(h, x0 + k + g, y0 + k + g, k, k, "S", Input.key(83), null);
        key(h, x0 + 2 * (k + g), y0 + k + g, k, k, "D", Input.key(68), null);
        if (mouse.value) {
            int y = y0 + 2 * (k + g);
            int bw = (totalW - g) / 2;
            key(h, x0, y, bw, k, "LMB", Input.mouse(0), Clicks.cpsLeft() + " cps");
            key(h, x0 + bw + g, y, bw, k, "RMB", Input.mouse(1), Clicks.cpsRight() + " cps");
        }
        int sy = y0 + (mouse.value ? 3 : 2) * (k + g);
        key(h, x0, sy, totalW, k / 2 + 2, "SPACE", Input.key(32), null);
    }
}
