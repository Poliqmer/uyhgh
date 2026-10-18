package pl.eclipse.client;

/** Ustawienia wyglądu klienta (kolor akcentu, przezroczystość HUD, tempo animacji GUI). */
public final class Interface extends Module {
    public static final Setting.Color accent = new Setting.Color("Accent color", 0x7C5CFF);
    public static final Setting.Num opacity = new Setting.Num("HUD opacity", 0.55, 0.1, 1.0, 0.05);
    public static final Setting.Num animSpeed = new Setting.Num("GUI animation speed", 1.0, 0.5, 2.0, 0.1);

    public Interface() {
        super("Interface", "Wyglad klienta: kolor akcentu, przezroczystosc HUD, tempo animacji", Category.CLIENT);
        alwaysOn = true;
        enabled = true;
        settings.add(accent);
        settings.add(opacity);
        settings.add(animSpeed);
    }
}
