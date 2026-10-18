package pl.eclipse.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    public boolean enabled;
    /** Kod klawisza GLFW albo -1. */
    public int key = -1;
    /** Moduł bez przełącznika (np. ustawienia interfejsu). */
    public boolean alwaysOn = false;
    public boolean failed;
    public String failReason = "";
    public final List<Setting> settings = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected Setting.Bool bool(String n, boolean def) {
        Setting.Bool s = new Setting.Bool(n, def);
        settings.add(s);
        return s;
    }

    protected Setting.Num num(String n, double def, double min, double max, double step) {
        Setting.Num s = new Setting.Num(n, def, min, max, step);
        settings.add(s);
        return s;
    }

    protected Setting.Color color(String n, int rgb) {
        Setting.Color s = new Setting.Color(n, rgb);
        settings.add(s);
        return s;
    }

    public void toggle() {
        if (alwaysOn) return;
        setEnabled(!enabled);
    }

    public void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        if (on) {
            failed = false;
            failReason = "";
        }
        try {
            if (on) onEnable(); else onDisable();
        } catch (Throwable t) {
            ModuleManager.fail(this, t);
        }
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(class_310 mc) {}
    public void onHud(Hud h) {}
}
