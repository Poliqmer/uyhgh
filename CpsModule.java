package pl.eclipse.client.modules;

import java.util.List;
import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class CpsModule extends Module {
    private final Setting.Num px = num("X", 1, 0, 100, 1);
    private final Setting.Num py = num("Y", 15, 0, 100, 1);
    private final Setting.Bool both = bool("Show right button", true);

    public CpsModule() {
        super("CPS Counter", "Klikniecia na sekunde (LPM / PPM)", Category.HUD);
        enabled = true;
    }

    @Override public void onHud(Hud h) {
        h.box(px, py, List.of("CPS: " + Clicks.cpsLeft() + (both.value ? " | " + Clicks.cpsRight() : "")));
    }
}
