package pl.eclipse.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_332;
import pl.eclipse.client.modules.*;

public final class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();
    public static volatile int fps;
    private static int frames;
    private static long fpsSince = System.nanoTime();
    private static final boolean[] keyPrev = new boolean[400];

    private ModuleManager() {}

    public static void init() {
        modules.add(new Interface());
        modules.add(new FpsModule());
        modules.add(new CoordsModule());
        modules.add(new CpsModule());
        modules.add(new KeystrokesModule());
        modules.add(new ArmorHudModule());
        modules.add(new CrosshairModule());
        modules.add(new WaypointsModule());
        modules.add(new CombatHudModule());
        modules.add(new ClickTrainer());
        modules.add(new PlacementTrainer());
        modules.add(new ToggleSprintModule());
    }

    public static Module byName(String n) {
        for (Module m : modules) if (m.name.equalsIgnoreCase(n)) return m;
        return null;
    }

    public static void fail(Module m, Throwable t) {
        m.enabled = false;
        m.failed = true;
        m.failReason = t.toString();
        EclipseClient.LOGGER.warn("Modul {} wylaczony po bledzie: {}", m.name, t.toString());
    }

    public static void tick(class_310 mc) {
        Clicks.poll(mc);
        boolean inGame = mc.field_1755 == null;
        for (Module m : modules) {
            if (inGame && m.key >= 0 && m.key < keyPrev.length) {
                boolean down = Input.key(m.key);
                if (down && !keyPrev[m.key] && !m.alwaysOn) m.toggle();
                keyPrev[m.key] = down;
            }
            if (!m.enabled) continue;
            try {
                m.onTick(mc);
            } catch (Throwable t) {
                fail(m, t);
            }
        }
    }

    public static void hud(class_332 ctx) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) return;
        Clicks.poll(mc);
        frames++;
        long now = System.nanoTime();
        if (now - fpsSince >= 1_000_000_000L) {
            fps = (int) (frames * 1e9 / (now - fpsSince));
            frames = 0;
            fpsSince = now;
        }
        int sw, sh;
        try {
            sw = ctx.method_51421(); // scaledWindowWidth (nazwa pośrednia z pamięci)
            sh = ctx.method_51443();
        } catch (Throwable t) {
            sw = 427;
            sh = 240;
        }
        Hud h = new Hud(ctx, mc, sw, sh);
        for (Module m : modules) {
            if (!m.enabled) continue;
            try {
                m.onHud(h);
            } catch (Throwable t) {
                fail(m, t);
            }
        }
    }
}
