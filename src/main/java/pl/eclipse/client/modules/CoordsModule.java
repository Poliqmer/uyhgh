package pl.eclipse.client.modules;

import java.util.List;
import net.minecraft.class_746;
import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class CoordsModule extends Module {
    private final Setting.Num px = num("X", 1, 0, 100, 1);
    private final Setting.Num py = num("Y", 8, 0, 100, 1);
    private final Setting.Bool facing = bool("Show facing", true);

    public CoordsModule() {
        super("Coordinates", "Pozycja gracza i kierunek", Category.HUD);
        enabled = true;
    }

    public static String dir(float yaw) {
        float y = ((yaw % 360) + 360) % 360;
        String[] n = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return n[Math.round(y / 45f) % 8];
    }

    @Override public void onHud(Hud h) {
        class_746 p = h.mc.field_1724;
        String s = String.format("XYZ: %.1f / %.1f / %.1f", p.method_23317(), p.method_23318(), p.method_23321());
        if (facing.value) {
            try {
                s += " " + dir(p.method_36454());
            } catch (Throwable ignored) {
                facing.value = false;
            }
        }
        h.box(px, py, List.of(s));
    }
}
