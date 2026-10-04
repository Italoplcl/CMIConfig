package cl.charlys.cmiconfig;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CmiFiles {
    private final JavaPlugin plugin;
    private final File pluginsDir;
    private final File cmiDir;
    private final File cmiLibDir;

    public CmiFiles(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pluginsDir = plugin.getDataFolder().getParentFile();
        this.cmiDir = new File(pluginsDir, "CMI");
        this.cmiLibDir = new File(pluginsDir, "CMILib");
    }

    public boolean isAvailable() {
        return new File(cmiDir, "config.yml").isFile();
    }

    public File cmiDir() { return cmiDir; }
    public File cmiLibDir() { return cmiLibDir; }

    public List<File> yamlFiles(File root) {
        List<File> result = new ArrayList<>();
        collectYaml(root, result);
        result.sort(Comparator.comparing(f -> relative(root, f).toLowerCase(Locale.ROOT)));
        return result;
    }

    private void collectYaml(File file, List<File> out) {
        if (file == null || !file.exists()) return;
        if (file.isFile()) {
            String n = file.getName().toLowerCase(Locale.ROOT);
            if (n.endsWith(".yml") || n.endsWith(".yaml")) out.add(file);
            return;
        }
        File[] children = file.listFiles();
        if (children == null) return;
        for (File child : children) collectYaml(child, out);
    }

    public String relative(File root, File file) {
        return root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
    }

    public List<String> childKeys(File file, String path) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = path == null || path.isEmpty() ? yaml : yaml.getConfigurationSection(path);
        if (section == null) return List.of();
        List<String> keys = new ArrayList<>(section.getKeys(false));
        keys.sort(String.CASE_INSENSITIVE_ORDER);
        return keys;
    }

    public boolean isSection(File file, String path) {
        return YamlConfiguration.loadConfiguration(file).isConfigurationSection(path);
    }

    public Object get(File file, String path) {
        return YamlConfiguration.loadConfiguration(file).get(path);
    }

    public String listToYaml(List<?> value) {
        YamlConfiguration wrapper = new YamlConfiguration();
        wrapper.set("value", value);
        String raw = wrapper.saveToString();
        String[] lines = raw.split("\\R", -1);
        StringBuilder out = new StringBuilder();
        boolean afterValue = false;
        for (String line : lines) {
            if (!afterValue) {
                if (line.equals("value:")) afterValue = true;
                else if (line.startsWith("value: ")) return line.substring(7);
                continue;
            }
            if (line.startsWith("  ")) line = line.substring(2);
            out.append(line).append('\n');
        }
        return out.toString().stripTrailing();
    }

    public List<?> yamlToList(String text) {
        YamlConfiguration wrapper = new YamlConfiguration();
        StringBuilder raw = new StringBuilder("value:\n");
        for (String line : text.split("\\R", -1)) raw.append("  ").append(line).append('\n');
        try {
            wrapper.loadFromString(raw.toString());
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException("YAML de lista no valido: " + e.getMessage());
        }
        Object value = wrapper.get("value");
        if (!(value instanceof List<?> list)) throw new IllegalArgumentException("se esperaba una lista YAML");
        return list;
    }

    public synchronized void set(File file, String path, Object value) throws IOException {
        ensureAllowed(file);
        backup(file);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set(path, value);
        yaml.save(file);
    }

    private void ensureAllowed(File file) throws IOException {
        File canonical = file.getCanonicalFile();
        File cmi = cmiDir.getCanonicalFile();
        File lib = cmiLibDir.getCanonicalFile();
        boolean inCmi = canonical.toPath().startsWith(cmi.toPath());
        boolean inLib = canonical.toPath().startsWith(lib.toPath());
        String n = canonical.getName().toLowerCase(Locale.ROOT);
        if ((!inCmi && !inLib) || (!n.endsWith(".yml") && !n.endsWith(".yaml"))) {
            throw new IOException("Archivo fuera de CMI/CMILib: " + file);
        }
    }

    private void backup(File file) throws IOException {
        File dir = new File(plugin.getDataFolder(), "backups");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("No se pudo crear " + dir);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        String safe = file.getParentFile().getName() + "-" + file.getName();
        Files.copy(file.toPath(), new File(dir, safe + "." + stamp + ".bak").toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }
}
