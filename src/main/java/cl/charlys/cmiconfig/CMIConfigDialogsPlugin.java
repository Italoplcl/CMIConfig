package cl.charlys.cmiconfig;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class CMIConfigDialogsPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {
    private Path cmiFolder;
    private Path configFile;
    private Path modulesFile;
    private Path deathFile;

    private static final Set<String> REVIEWED_MODULES = Set.of(
            "silentChest", "versionCheck", "elevator", "damageControl", "portalCreation",
            "vanish", "armorstand", "selection", "noTarget", "chatBubble", "tablist", "namePlates"
    );

    private static final Map<String, Set<String>> BOOL_PATHS = Map.of(
            "config", Set.of(
                    "Messages.Login.Disabled", "Messages.Login.Custom.Use", "Messages.Login.Custom.ServerSwitch",
                    "Messages.Logout.Disabled", "Messages.Logout.Custom.Use", "Messages.Logout.Custom.ServerSwitch",
                    "Messages.Filter.ForLogin", "Messages.Filter.ForLogout"
            ),
            "death", Set.of("EnableCustom")
    );

    private static final Map<String, Set<String>> INT_PATHS = Map.of(
            "config", Set.of("Messages.Login.AutoHideFrom", "Messages.Logout.AutoHideFrom"),
            "death", Set.of("AutoHideFrom", "Range", "AntiSpam.TimeRange", "AntiSpam.Count")
    );

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("CMI") == null || Bukkit.getPluginManager().getPlugin("CMILib") == null) {
            getLogger().severe("CMI y CMILib son dependencias obligatorias.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        cmiFolder = getDataFolder().toPath().getParent().resolve("CMI");
        configFile = cmiFolder.resolve("config.yml");
        modulesFile = cmiFolder.resolve("Settings").resolve("Modules.yml");
        deathFile = cmiFolder.resolve("Settings").resolve("DeathMessages.yml");

        if (!Files.isRegularFile(configFile) || !Files.isRegularFile(modulesFile) || !Files.isRegularFile(deathFile)) {
            getLogger().severe("No se encontró la estructura esperada de plugins/CMI/. No se modificará ningún archivo.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            Path dialog = DialogWriter.write(cmiFolder);
            getLogger().info("Dialogs escritos en " + dialog);
        } catch (IOException ex) {
            getLogger().log(java.util.logging.Level.SEVERE, "No se pudo crear CMIConfigDialogs.yml", ex);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        PluginCommand command = getCommand("cmiconfig");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
        getLogger().info("CMIConfigDialogs habilitado. Usa /cmiconfig.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cmiconfig.admin")) {
            sender.sendMessage(color("&cNo tienes permiso para usar CMIConfigDialogs."));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Este panel debe abrirse desde un jugador.");
                return true;
            }
            player.performCommand("cmi dialogs cmiconfig_main");
            return true;
        }

        try {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "setbool" -> setBoolean(sender, args);
                case "setint" -> setInteger(sender, args);
                case "setenum" -> setEnum(sender, args);
                case "module" -> setModule(sender, args);
                case "regeneratedialogs" -> regenerate(sender);
                case "status" -> status(sender, args);
                default -> {
                    sender.sendMessage(color("&eUso: /cmiconfig"));
                    yield true;
                }
            };
        } catch (Exception ex) {
            getLogger().log(java.util.logging.Level.SEVERE, "Error modificando CMI", ex);
            sender.sendMessage(color("&cNo se pudo guardar el cambio. Revisa la consola."));
            return true;
        }
    }

    private boolean setBoolean(CommandSender sender, String[] args) throws IOException {
        if (args.length != 4) return usage(sender, "setbool <config|death> <ruta> <true|false>");
        String target = args[1].toLowerCase(Locale.ROOT);
        String path = args[2];
        if (!BOOL_PATHS.getOrDefault(target, Set.of()).contains(path)) return denied(sender);
        if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) return usage(sender, "valor true/false");
        return write(sender, fileFor(target), path, Boolean.toString(Boolean.parseBoolean(args[3])), false);
    }

    private boolean setInteger(CommandSender sender, String[] args) throws IOException {
        if (args.length != 4) return usage(sender, "setint <config|death> <ruta> <número>");
        String target = args[1].toLowerCase(Locale.ROOT);
        String path = args[2];
        if (!INT_PATHS.getOrDefault(target, Set.of()).contains(path)) return denied(sender);
        int value;
        try { value = Integer.parseInt(args[3]); }
        catch (NumberFormatException ex) { return usage(sender, "el valor debe ser un número entero"); }
        if (value < -1 || value > 1_000_000) return usage(sender, "valor permitido: -1 a 1000000");
        return write(sender, fileFor(target), path, Integer.toString(value), false);
    }

    private boolean setEnum(CommandSender sender, String[] args) throws IOException {
        if (args.length < 6 || !args[1].equalsIgnoreCase("death") || !args[2].equals("Destination")) return denied(sender);
        String value = args[3];
        Set<String> allowed = new HashSet<>(Arrays.asList(args).subList(4, args.length));
        if (!allowed.contains(value) || !(value.equals("plain") || value.equals("actionBar"))) return denied(sender);
        return write(sender, deathFile, "Destination", value, false);
    }

    private boolean setModule(CommandSender sender, String[] args) throws IOException {
        if (args.length != 3) return usage(sender, "module <módulo> <true|false>");
        String module = args[1];
        if (!REVIEWED_MODULES.contains(module)) return denied(sender);
        if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) return usage(sender, "valor true/false");
        boolean result = write(sender, modulesFile, module, Boolean.toString(Boolean.parseBoolean(args[2])), true);
        if (result) sender.sendMessage(color("&eModules.yml requiere reinicio completo del servidor para aplicar el cambio."));
        return result;
    }

    private boolean regenerate(CommandSender sender) throws IOException {
        DialogWriter.write(cmiFolder);
        sender.sendMessage(color("&aCMIConfigDialogs.yml regenerado. Recarga CMI para que relea los Dialogs."));
        return true;
    }

    private boolean status(CommandSender sender, String[] args) throws IOException {
        if (args.length != 3) return usage(sender, "status <config|death|modules> <ruta>");
        Path file = switch (args[1].toLowerCase(Locale.ROOT)) {
            case "config" -> configFile;
            case "death" -> deathFile;
            case "modules" -> modulesFile;
            default -> null;
        };
        if (file == null) return denied(sender);
        Optional<String> value = YamlScalarEditor.readScalar(file, args[2]);
        sender.sendMessage(color(value.map(v -> "&a" + args[2] + " = &f" + v).orElse("&cNo se encontró esa ruta.")));
        return true;
    }

    private boolean write(CommandSender sender, Path file, String path, String value, boolean restart) throws IOException {
        if (!YamlScalarEditor.setScalar(file, path, value)) {
            sender.sendMessage(color("&cNo encontré la ruta '&f" + path + "&c'. No se modificó el archivo."));
            return true;
        }
        sender.sendMessage(color("&aGuardado: &f" + path + " &7→ &f" + value));
        if (!restart) sender.sendMessage(color("&7El archivo fue guardado. CMI debe releer su configuración para aplicar cambios en memoria."));
        return true;
    }

    private Path fileFor(String target) { return target.equals("config") ? configFile : deathFile; }

    private boolean usage(CommandSender sender, String text) {
        sender.sendMessage(color("&eUso interno: &f/cmiconfig " + text));
        return true;
    }

    private boolean denied(CommandSender sender) {
        sender.sendMessage(color("&cEsa ruta/operación no está habilitada en esta versión del panel."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("cmiconfig.admin")) return List.of();
        if (args.length == 1) return prefix(args[0], List.of("status", "regeneratedialogs"));
        return List.of();
    }

    private static List<String> prefix(String typed, List<String> values) {
        String t = typed.toLowerCase(Locale.ROOT);
        return values.stream().filter(v -> v.toLowerCase(Locale.ROOT).startsWith(t)).toList();
    }

    @SuppressWarnings("deprecation")
    private static String color(String text) { return ChatColor.translateAlternateColorCodes('&', text); }
}
