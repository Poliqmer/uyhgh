package pl.eclipse.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_7157;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.eclipse.client.modules.WaypointsModule;

public class EclipseClient implements ClientModInitializer {
    public static final String MOD_ID = "eclipse_client";
    public static final Logger LOGGER = LoggerFactory.getLogger("EclipseClient");
    private static volatile boolean openRequested;
    private boolean guiKeyPrev;
    private static String hudHook = "brak";

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        EclipseConfig.load();

        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> EclipseConfig.save());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.field_1724 == null) return;
            boolean down = client.field_1755 == null && Input.key(344); // prawy Shift otwiera GUI
            if (openRequested || (down && !guiKeyPrev)) {
                openRequested = false;
                client.method_1507(new EclipseScreen());
            }
            guiKeyPrev = down;
            ModuleManager.tick(client);
        });
        registerHud();
        LOGGER.info("Eclipse Client zaladowany ({} modulow). GUI: prawy Shift albo /eclipse. HUD hook: {}",
                ModuleManager.modules.size(), hudHook);
    }

    /** HUD podpinamy refleksją, bo API Fabric zmieniało nazwę haka (HudRenderCallback -> HudElementRegistry). */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerHud() {
        InvocationHandler handler = (proxy, method, args) -> {
            String n = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                if (n.equals("equals")) return proxy == args[0];
                if (n.equals("hashCode")) return System.identityHashCode(proxy);
                return "EclipseHud";
            }
            if (args != null) {
                for (Object a : args) {
                    if (a instanceof class_332 c) {
                        ModuleManager.hud(c);
                        break;
                    }
                }
            }
            return null;
        };
        try {
            Class<?> cb = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback");
            Object proxy = Proxy.newProxyInstance(cb.getClassLoader(), new Class<?>[]{cb}, handler);
            ((Event) cb.getField("EVENT").get(null)).register(proxy);
            hudHook = "HudRenderCallback";
            return;
        } catch (Throwable t) {
            LOGGER.info("HudRenderCallback niedostepny ({}), probuje HudElementRegistry", t.toString());
        }
        try {
            Class<?> reg = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry");
            Class<?> el = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement");
            Class<?> idc = Class.forName("net.minecraft.class_2960");
            Object id = idc.getMethod("method_60655", String.class, String.class).invoke(null, MOD_ID, "hud");
            Object proxy = Proxy.newProxyInstance(el.getClassLoader(), new Class<?>[]{el}, handler);
            reg.getMethod("addLast", idc, el).invoke(null, id, proxy);
            hudHook = "HudElementRegistry";
        } catch (Throwable t) {
            LOGGER.warn("Nie udalo sie podpiac HUD - moduly HUD nie beda widoczne: {}", t.toString());
        }
    }

    private static void say(FabricClientCommandSource s, String msg, class_124 c) {
        s.sendFeedback(class_2561.method_43470("[Eclipse] " + msg).method_27692(c));
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, class_7157 registryAccess) {
        dispatcher.register(ClientCommandManager.literal("eclipse")
                .executes(ctx -> {
                    openRequested = true;
                    return 1;
                })
                .then(ClientCommandManager.literal("wp")
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            var p = net.minecraft.class_310.method_1551().field_1724;
                                            String name = StringArgumentType.getString(ctx, "name");
                                            if (p == null) return 0;
                                            try {
                                                WaypointsModule.points.removeIf(w -> w.name.equalsIgnoreCase(name));
                                                WaypointsModule.points.add(new WaypointsModule.Point(name, p.method_23317(), p.method_23318(), p.method_23321()));
                                                EclipseConfig.save();
                                                say(ctx.getSource(), "Dodano waypoint " + name + ".", class_124.field_1060);
                                            } catch (Throwable t) {
                                                say(ctx.getSource(), "Nie mozna odczytac pozycji gracza: " + t, class_124.field_1061);
                                            }
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("del")
                                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            boolean removed = WaypointsModule.points.removeIf(w -> w.name.equalsIgnoreCase(name));
                                            EclipseConfig.save();
                                            say(ctx.getSource(), removed ? "Usunieto " + name + "." : "Nie ma waypointu " + name + ".",
                                                    removed ? class_124.field_1060 : class_124.field_1061);
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("list")
                                .executes(ctx -> {
                                    if (WaypointsModule.points.isEmpty()) say(ctx.getSource(), "Brak waypointow.", class_124.field_1054);
                                    for (var w : WaypointsModule.points) {
                                        say(ctx.getSource(), w.name + ": " + Math.round(w.x) + " " + Math.round(w.y) + " " + Math.round(w.z), class_124.field_1054);
                                    }
                                    return 1;
                                }))));
    }
}
