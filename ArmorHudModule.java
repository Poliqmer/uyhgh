package pl.eclipse.client.modules;

import net.minecraft.class_1661;
import net.minecraft.class_1799;
import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class ArmorHudModule extends Module {
    private final Setting.Num px = num("X", 50, 0, 100, 1);
    private final Setting.Num py = num("Y", 88, 0, 100, 1);
    private final Setting.Bool durability = bool("Show durability %", true);

    public ArmorHudModule() {
        super("Armor HUD", "Elementy pancerza i ich wytrzymalosc", Category.HUD);
    }

    @Override public void onHud(Hud h) {
        class_1661 inv = h.mc.field_1724.method_31548();
        int w = 4 * 22, hgt = durability.value ? 28 : 18;
        int x0 = (int) ((h.sw - w) * px.value / 100.0), y0 = (int) ((h.sh - hgt) * py.value / 100.0);
        for (int i = 0; i < 4; i++) {
            class_1799 st = inv.method_7372(3 - i); // 3 = hełm ... 0 = buty
            if (st == null || st.method_7960()) continue;
            int x = x0 + i * 22;
            h.ctx.method_51427(st, x, y0);
            if (durability.value) {
                try {
                    int max = st.method_7936();
                    if (max > 0) {
                        int left = (max - st.method_7919()) * 100 / max;
                        String s = left + "%";
                        int c = left > 50 ? 0xFF7CFF8C : left > 20 ? 0xFFFFD166 : 0xFFFF5C5C;
                        Draw.text(h.ctx, h.mc, s, x + 8 - Draw.width(h.mc, s) / 2, y0 + 18, c);
                    }
                } catch (Throwable t) {
                    durability.value = false;
                }
            }
        }
    }
}
