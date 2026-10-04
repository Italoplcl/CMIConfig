package cl.charlys.cmiconfig;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DialogMenus {
    private static final int PAGE_SIZE = 12;
    private final CMIConfigDialogsPlugin plugin;
    private final CmiFiles files;

    public DialogMenus(CMIConfigDialogsPlugin plugin, CmiFiles files) {
        this.plugin = plugin;
        this.files = files;
    }

    public void openMain(Player player) {
        List<ActionButton> actions = new ArrayList<>();
        actions.add(button("CMI - config.yml", "Editar todas las opciones de CMI/config.yml",
            p -> openSection(p, new File(files.cmiDir(), "config.yml"), "", "CMI/config.yml", this::openMain, 0)));
        actions.add(button("CMI - todos los YAML", "Settings, Saves, traducciones, DeathMessages, Kits, Alias, Dialogs y demas YAML de CMI",
            p -> openFileList(p, files.cmiDir(), "CMI", 0, this::openMain)));
        if (files.cmiLibDir().isDirectory()) {
            actions.add(button("CMILib - config.yml", "Editar todas las opciones de CMILib/config.yml",
                p -> openSection(p, new File(files.cmiLibDir(), "config.yml"), "", "CMILib/config.yml", this::openMain, 0)));
            actions.add(button("CMILib - todos los YAML", "Configuracion, Saves y traducciones de CMILib",
                p -> openFileList(p, files.cmiLibDir(), "CMILib", 0, this::openMain)));
        }
        actions.add(button("Mensajes", "Acceso a mensajes de CMI y mensajes de muerte", this::openMessages));
        showActions(player, "Configuracion CMI", "Editor completo de los YAML instalados de CMI y CMILib.", actions);
    }

    private void openMessages(Player player) {
        List<ActionButton> actions = new ArrayList<>();
        File config = new File(files.cmiDir(), "config.yml");
        if (config.isFile()) {
            actions.add(button("Messages (config.yml)", "Editar la seccion Messages completa",
                p -> openSection(p, config, "Messages", "CMI/config.yml > Messages", this::openMessages, 0)));
        }
        File death = new File(new File(files.cmiDir(), "Settings"), "DeathMessages.yml");
        if (death.isFile()) {
            actions.add(button("DeathMessages.yml", "Editar configuracion de mensajes de muerte",
                p -> openSection(p, death, "", "CMI/Settings/DeathMessages.yml", this::openMessages, 0)));
        }
        File deathTranslations = new File(new File(files.cmiDir(), "Translations"), "DeathMessages");
        if (deathTranslations.isDirectory()) {
            actions.add(button("Traducciones DeathMessages", "Editar traducciones de mensajes de muerte",
                p -> openFileList(p, deathTranslations, "CMI/Translations/DeathMessages", 0, this::openMessages)));
        }
        actions.add(button("Volver", "Volver al menu principal", this::openMain));
        showActions(player, "Mensajes", "Mensajes y configuraciones relacionadas.", actions);
    }

    private void openFileList(Player player, File root, String title, int page, java.util.function.Consumer<Player> back) {
        List<File> all = files.yamlFiles(root);
        int pages = Math.max(1, (all.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int safePage = Math.max(0, Math.min(page, pages - 1));
        int from = safePage * PAGE_SIZE;
        int to = Math.min(all.size(), from + PAGE_SIZE);
        List<ActionButton> actions = new ArrayList<>();
        for (int i = from; i < to; i++) {
            File f = all.get(i);
            String rel = files.relative(root, f);
            actions.add(button(rel, "Editar " + rel,
                p -> openSection(p, f, "", title + "/" + rel, q -> openFileList(q, root, title, safePage, back), 0)));
        }
        if (safePage > 0) actions.add(button("Pagina anterior", "Pagina " + safePage, p -> openFileList(p, root, title, safePage - 1, back)));
        if (safePage + 1 < pages) actions.add(button("Pagina siguiente", "Pagina " + (safePage + 2), p -> openFileList(p, root, title, safePage + 1, back)));
        actions.add(button("Volver", "Volver", back));
        showActions(player, title + " - YAML " + (safePage + 1) + "/" + pages,
            all.size() + " archivos YAML encontrados.", actions);
    }

    private void openSection(Player player, File file, String path, String title,
                             java.util.function.Consumer<Player> back, int page) {
        List<String> keys = files.childKeys(file, path);
        if (keys.isEmpty()) {
            showActions(player, title, "Esta seccion no contiene opciones editables.", List.of(button("Volver", "Volver", back)));
            return;
        }
        int pages = Math.max(1, (keys.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int safePage = Math.max(0, Math.min(page, pages - 1));
        int from = safePage * PAGE_SIZE;
        int to = Math.min(keys.size(), from + PAGE_SIZE);
        List<ActionButton> actions = new ArrayList<>();
        for (int i = from; i < to; i++) {
            String key = keys.get(i);
            String childPath = path == null || path.isEmpty() ? key : path + "." + key;
            if (files.isSection(file, childPath)) {
                actions.add(button(key, childPath,
                    p -> openSection(p, file, childPath, title + " > " + key,
                        q -> openSection(q, file, path, title, back, safePage), 0)));
            } else {
                Object value = files.get(file, childPath);
                actions.add(button(key, preview(value),
                    p -> openValue(p, file, childPath, title + " > " + key,
                        q -> openSection(q, file, path, title, back, safePage))));
            }
        }
        if (safePage > 0) actions.add(button("Pagina anterior", "Pagina " + safePage,
            p -> openSection(p, file, path, title, back, safePage - 1)));
        if (safePage + 1 < pages) actions.add(button("Pagina siguiente", "Pagina " + (safePage + 2),
            p -> openSection(p, file, path, title, back, safePage + 1)));
        actions.add(button("Volver", "Volver", back));
        showActions(player, title + (pages > 1 ? " " + (safePage + 1) + "/" + pages : ""),
            path == null || path.isEmpty() ? file.getName() : path, actions);
    }

    private void openValue(Player player, File file, String path, String title, java.util.function.Consumer<Player> back) {
        Object current = files.get(file, path);
        if (current instanceof Boolean b) {
            DialogInput input = DialogInput.bool("value", Component.text(path), b, "true", "false");
            showSaveBack(player, title, List.of(input), (view, p) -> {
                try {
                    files.set(file, path, view.getBoolean("value"));
                    p.sendRichMessage("<green>Guardado:</green> <yellow>" + escape(path) + "</yellow>");
                    back.accept(p);
                } catch (IOException e) { fail(p, e); }
            }, back);
            return;
        }

        String text = valueToText(current);
        DialogInput input = DialogInput.text("value", Component.text(path))
            .initial(text).width(500).maxLength(8192)
            .multiline(TextDialogInput.MultilineOptions.create(160, 300)).build();
        showSaveBack(player, title, List.of(input), (view, p) -> {
            try {
                Object parsed = parseLike(current, view.getText("value"));
                files.set(file, path, parsed);
                p.sendRichMessage("<green>Guardado:</green> <yellow>" + escape(path) + "</yellow>");
                back.accept(p);
            } catch (IllegalArgumentException e) {
                p.sendRichMessage("<red>Valor no valido para esta opcion:</red> " + escape(e.getMessage()));
                openValue(p, file, path, title, back);
            } catch (IOException e) { fail(p, e); }
        }, back);
    }

    private Object parseLike(Object original, String text) {
        String t = text.trim();
        if (original instanceof Integer) {
            try { return Integer.parseInt(t); } catch (NumberFormatException e) { throw new IllegalArgumentException("se esperaba un entero"); }
        }
        if (original instanceof Long) {
            try { return Long.parseLong(t); } catch (NumberFormatException e) { throw new IllegalArgumentException("se esperaba un numero entero largo"); }
        }
        if (original instanceof Double) {
            try { return Double.parseDouble(t); } catch (NumberFormatException e) { throw new IllegalArgumentException("se esperaba un decimal"); }
        }
        if (original instanceof Float) {
            try { return Float.parseFloat(t); } catch (NumberFormatException e) { throw new IllegalArgumentException("se esperaba un decimal"); }
        }
        if (original instanceof List<?>) {
            return files.yamlToList(text);
        }
        return text;
    }

    private String valueToText(Object value) {
        if (value == null) return "";
        if (value instanceof List<?> list) return files.listToYaml(list);
        return String.valueOf(value);
    }

    private String preview(Object value) {
        if (value == null) return "null";
        if (value instanceof List<?> list) return "Lista: " + list.size() + " elementos";
        String s = String.valueOf(value).replace('\n', ' ');
        return s.length() > 100 ? s.substring(0, 97) + "..." : s;
    }

    private void showActions(Player player, String title, String body, List<ActionButton> actions) {
        player.showDialog(Dialog.create(b -> b.empty()
            .base(DialogBase.builder(Component.text(title))
                .body(List.of(DialogBody.plainMessage(Component.text(body)))).build())
            .type(DialogType.multiAction(actions).columns(1).build())));
    }

    private void showSaveBack(Player player, String title, List<DialogInput> inputs, SaveHandler save,
                              java.util.function.Consumer<Player> back) {
        ActionButton saveButton = ActionButton.builder(Component.text("Guardar"))
            .tooltip(Component.text("Guardar cambios"))
            .action(io.papermc.paper.registry.data.dialog.action.DialogAction.customClick((view, audience) -> {
                if (audience instanceof Player p) save.run(view, p);
            }, ClickCallback.Options.builder().uses(1).build())).build();
        ActionButton backButton = button("Volver", "Descartar y volver", back);
        player.showDialog(Dialog.create(b -> b.empty()
            .base(DialogBase.builder(Component.text(title)).inputs(inputs).build())
            .type(DialogType.confirmation(saveButton, backButton))));
    }

    private ActionButton button(String label, String tooltip, java.util.function.Consumer<Player> action) {
        return ActionButton.builder(Component.text(label))
            .tooltip(Component.text(tooltip))
            .action(io.papermc.paper.registry.data.dialog.action.DialogAction.customClick((view, audience) -> {
                if (audience instanceof Player p) action.accept(p);
            }, ClickCallback.Options.builder().uses(1).build())).build();
    }

    private String escape(String text) {
        return text == null ? "" : text.replace("<", "\\<").replace(">", "\\>");
    }

    private void fail(Player player, Exception e) {
        plugin.getLogger().severe("No se pudo guardar CMI/CMILib: " + e.getMessage());
        player.sendRichMessage("<red>No se pudo guardar. Revisa la consola; el archivo original no se elimina.</red>");
    }

    @FunctionalInterface
    private interface SaveHandler {
        void run(io.papermc.paper.dialog.DialogResponseView view, Player player);
    }
}
