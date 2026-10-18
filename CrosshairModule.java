package pl.eclipse.client.modules;

import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class CrosshairModule extends Module {
    private final Setting.Num size = num("Size", 5, 1, 15, 1);
    private final Setting.Num thick = num("Thickness", 1, 1, 4, 1);
    private final Setting.Num gap = num("Gap", 2, 0, 10, 1);
    private final Setting.Bool dot = bool("Center dot", false);
    private final Setting.Bool outline = bool("Outline", true);
    private final Setting.Color col = color("Color", 0x00FFB3);

    public CrosshairModule() {
        super("Custom Crosshair", "Wlasny celownik rysowany na srodku ekranu (nad domyslnym)", Category.HUD);
    }

    private void rect(Hud h, int x1, int y1, int x2, int y2, int c) {
        if (outline.value) Draw.fill(h.ctx, x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        Draw.fill(h.ctx, x1, y1, x2, y2, c);
    }

    @Override public void onHud(Hud h) {
        int cx = h.sw / 2, cy = h.sh / 2, t = thick.i(), s = size.i(), g = gap.i();
        int a = t / 2, b = t - a;
        int c = 0xFF000000 | col.rgb();
        rect(h, cx - a, cy - g - s, cx + b, cy - g, c);
        rect(h, cx - a, cy + g, cx + b, cy + g + s, c);
        rect(h, cx - g - s, cy - a, cx - g, cy + b, c);
        rect(h, cx + g, cy - a, cx + g + s, cy + b, c);
        if (dot.value) rect(h, cx - a, cy - a, cx + b, cy + b, c);
    }
}
