package cl.charlys.cmiconfig;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CMIConfigDialogsPlugin extends JavaPlugin {
    private CmiFiles cmiFiles;
    private DialogMenus menus;

    @Override
    public void onEnable() {
        this.cmiFiles = new CmiFiles(this);
        this.menus = new DialogMenus(this, cmiFiles);

        if (!cmiFiles.isAvailable()) {
            getLogger().severe("No se encontro plugins/CMI/config.yml. /cmiconfig no podra editar CMI.");
        }
        getLogger().info("CMIConfigDialogs 0.2.0 habilitado. Usa /cmiconfig.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("cmiconfig")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse dentro del juego.");
            return true;
        }
        if (!player.hasPermission("cmiconfig.admin")) {
            player.sendRichMessage("<red>No tienes permiso para usar este comando.</red>");
            return true;
        }
        if (!cmiFiles.isAvailable()) {
            player.sendRichMessage("<red>No encontre los archivos de CMI. Revisa la consola.</red>");
            return true;
        }
        menus.openMain(player);
        return true;
    }
}
