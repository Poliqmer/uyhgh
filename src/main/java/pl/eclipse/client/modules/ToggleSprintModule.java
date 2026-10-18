package pl.eclipse.client.modules;

import net.minecraft.class_310;
import net.minecraft.class_746;
import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class ToggleSprintModule extends Module {
    private final Setting.Bool needForward = bool("Only while moving forward (W)", true);

    public ToggleSprintModule() {
        super("Toggle Sprint", "Utrzymuje sprint podczas chodzenia do przodu (eksperymentalne)", Category.MOVEMENT);
    }

    @Override public void onTick(class_310 mc) {
        class_746 p = mc.field_1724;
        if (p == null || mc.field_1755 != null) return;
        if (!needForward.value || Input.key(87)) p.method_5728(true);
    }
}
