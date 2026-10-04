package cl.charlys.cmiconfig;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class CmiFiles {
    private final JavaPlugin plugin;
    private final File configFile;
    private final File modulesFile;

    public CmiFiles(JavaPlugin plugin) {
        this.plugin = plugin;
        File plugins = plugin.getDataFolder().getParentFile();
        File cmi = new File(plugins, "CMI");
        this.configFile = new File(cmi, "config.yml");
        this.modulesFile = new File(new File(cmi, "Settings"), "Modules.yml");
    }

    public boolean isAvailable() {
        return configFile.isFile() && modulesFile.isFile();
    }

    public boolean getConfigBoolean(String path, boolean fallback) {
        return YamlConfiguration.loadConfiguration(configFile).getBoolean(path, fallback);
    }

    public int getConfigInt(String path, int fallback) {
        return YamlConfiguration.loadConfiguration(configFile).getInt(path, fallback);
    }

    public List<String> getConfigStringList(String path) {
        return YamlConfiguration.loadConfiguration(configFile).getStringList(path);
    }

    public boolean getModule(String name, boolean fallback) {
        return YamlConfiguration.loadConfiguration(modulesFile).getBoolean(name, fallback);
    }

    public synchronized void setConfig(String path, Object value) throws IOException {
        save(configFile, path, value);
    }

    public synchronized void setModule(String path, boolean value) throws IOException {
        save(modulesFile, path, value);
    }

    private void save(File file, String path, Object value) throws IOException {
        backup(file);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set(path, value);
        yaml.save(file);
    }

    private void backup(File file) throws IOException {
        File dir = new File(plugin.getDataFolder(), "backups");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("No se pudo crear " + dir);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        Files.copy(file.toPath(), new File(dir, file.getName() + "." + stamp + ".bak").toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }
}
