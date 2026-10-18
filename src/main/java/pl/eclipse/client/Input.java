package pl.eclipse.client;

import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;

/** Odczyt klawiatury i myszy bezpośrednio z GLFW (bez haków na zdarzenia gry). */
public final class Input {
    private static boolean broken;

    private Input() {}

    private static long handle() {
        return class_310.method_1551().method_22683().method_4490();
    }

    public static boolean key(int code) {
        if (broken || code < 0) return false;
        try {
            return GLFW.glfwGetKey(handle(), code) == 1;
        } catch (Throwable t) {
            broken = true;
            EclipseClient.LOGGER.warn("odczyt klawiatury niedostepny: {}", t.toString());
            return false;
        }
    }

    public static boolean mouse(int button) {
        if (broken) return false;
        try {
            return GLFW.glfwGetMouseButton(handle(), button) == 1;
        } catch (Throwable t) {
            broken = true;
            EclipseClient.LOGGER.warn("odczyt myszy niedostepny: {}", t.toString());
            return false;
        }
    }

    public static String keyName(int k) {
        if (k < 0) return "NONE";
        if (k >= 65 && k <= 90) return String.valueOf((char) k);
        if (k >= 48 && k <= 57) return String.valueOf((char) k);
        if (k >= 290 && k <= 301) return "F" + (k - 289);
        if (k >= 320 && k <= 329) return "NUM" + (k - 320);
        switch (k) {
            case 32: return "SPACE";
            case 256: return "ESC";
            case 257: return "ENTER";
            case 258: return "TAB";
            case 259: return "BACKSPACE";
            case 260: return "INSERT";
            case 261: return "DELETE";
            case 262: return "RIGHT";
            case 263: return "LEFT";
            case 264: return "DOWN";
            case 265: return "UP";
            case 268: return "HOME";
            case 269: return "END";
            case 280: return "CAPS";
            case 340: return "LSHIFT";
            case 341: return "LCTRL";
            case 342: return "LALT";
            case 344: return "RSHIFT";
            case 345: return "RCTRL";
            case 346: return "RALT";
            case 39: return "'";
            case 44: return ",";
            case 45: return "-";
            case 46: return ".";
            case 47: return "/";
            case 59: return ";";
            case 61: return "=";
            case 91: return "[";
            case 92: return "\\";
            case 93: return "]";
            case 96: return "`";
            default: return "KEY" + k;
        }
    }
}
