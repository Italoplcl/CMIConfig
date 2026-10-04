package cl.charlys.cmiconfig;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DialogMenus {
    private final CMIConfigDialogsPlugin plugin;
    private final CmiFiles files;

    public DialogMenus(CMIConfigDialogsPlugin plugin, CmiFiles files) {
        this.plugin = plugin;
        this.files = files;
    }

    public void openMain(Player player) {
        List<ActionButton> actions = new ArrayList<>();
        if (files.getModule("customMessages", true)) {
            actions.add(button("Mensajes personalizados", "Editar Login, Logout y filtros de customMessages.", p -> openCustomMessages(p)));
        }
        actions.add(button("Modulos revisados", "Ver y cambiar solamente los modulos que ya decidimos revisar.", p -> openModules(p)));

        Dialog dialog = Dialog.create(b -> b.empty()
            .base(DialogBase.builder(Component.text("Configuracion CMI"))
                .body(List.of(io.papermc.paper.registry.data.dialog.body.DialogBody.plainMessage(
                    Component.text("Editor propio de CMIConfigDialogs. No usa /cmi dialogs."))))
                .build())
            .type(DialogType.multiAction(actions).columns(1).build()));
        player.showDialog(dialog);
    }

    public void openCustomMessages(Player player) {
        List<ActionButton> actions = List.of(
            button("Mensajes de entrada", "Configurar Messages.Login", this::openLogin),
            button("Mensajes de salida", "Configurar Messages.Logout", this::openLogout),
            button("Filtro de nombres", "Configurar Messages.Filter", this::openFilter),
            button("Volver", "Volver al menu principal", this::openMain)
        );
        player.showDialog(Dialog.create(b -> b.empty()
            .base(DialogBase.builder(Component.text("Mensajes personalizados")).build())
            .type(DialogType.multiAction(actions).columns(1).build())));
    }

    private void openLogin(Player player) {
        boolean show = !files.getConfigBoolean("Messages.Login.Disabled", false);
        int autoHide = files.getConfigInt("Messages.Login.AutoHideFrom", -1);
        boolean custom = files.getConfigBoolean("Messages.Login.Custom.Use", false);
        boolean serverSwitch = files.getConfigBoolean("Messages.Login.Custom.ServerSwitch", true);

        List<DialogInput> inputs = List.of(
            DialogInput.bool("show", Component.text("Mostrar mensajes de entrada"), show, "true", "false"),
            DialogInput.text("autohide", Component.text("Ocultar desde esta cantidad de jugadores"))
                .initial(String.valueOf(autoHide)).width(200).build(),
            DialogInput.bool("custom", Component.text("Usar mensaje personalizado"), custom, "true", "false"),
            DialogInput.bool("serverswitch", Component.text("Detectar cambio entre servidores"), serverSwitch, "true", "false")
        );
        showSaveBack(player, "Mensajes de entrada", inputs, (view, p) -> {
            Integer value = parseInt(view.getText("autohide"));
            if (value == null) { p.sendRichMessage("<red>AutoHideFrom debe ser un numero entero.</red>"); openLogin(p); return; }
            try {
                files.setConfig("Messages.Login.Disabled", !view.getBoolean("show"));
                files.setConfig("Messages.Login.AutoHideFrom", value);
                files.setConfig("Messages.Login.Custom.Use", view.getBoolean("custom"));
                files.setConfig("Messages.Login.Custom.ServerSwitch", view.getBoolean("serverswitch"));
                p.sendRichMessage("<green>Configuracion de Login guardada.</green> <yellow>CMI puede requerir reload/reinicio para aplicar determinados cambios.</yellow>");
                openCustomMessages(p);
            } catch (IOException e) { fail(p, e); }
        }, this::openCustomMessages);
    }

    private void openLogout(Player player) {
        boolean show = !files.getConfigBoolean("Messages.Logout.Disabled", false);
        int autoHide = files.getConfigInt("Messages.Logout.AutoHideFrom", -1);
        boolean custom = files.getConfigBoolean("Messages.Logout.Custom.Use", false);
        boolean serverSwitch = files.getConfigBoolean("Messages.Logout.Custom.ServerSwitch", true);
        List<DialogInput> inputs = List.of(
            DialogInput.bool("show", Component.text("Mostrar mensajes de salida"), show, "true", "false"),
            DialogInput.text("autohide", Component.text("Ocultar desde esta cantidad de jugadores")).initial(String.valueOf(autoHide)).width(200).build(),
            DialogInput.bool("custom", Component.text("Usar mensaje personalizado"), custom, "true", "false"),
            DialogInput.bool("serverswitch", Component.text("Detectar cambio entre servidores"), serverSwitch, "true", "false")
        );
        showSaveBack(player, "Mensajes de salida", inputs, (view, p) -> {
            Integer value = parseInt(view.getText("autohide"));
            if (value == null) { p.sendRichMessage("<red>AutoHideFrom debe ser un numero entero.</red>"); openLogout(p); return; }
            try {
                files.setConfig("Messages.Logout.Disabled", !view.getBoolean("show"));
                files.setConfig("Messages.Logout.AutoHideFrom", value);
                files.setConfig("Messages.Logout.Custom.Use", view.getBoolean("custom"));
                files.setConfig("Messages.Logout.Custom.ServerSwitch", view.getBoolean("serverswitch"));
                p.sendRichMessage("<green>Configuracion de Logout guardada.</green>");
                openCustomMessages(p);
            } catch (IOException e) { fail(p, e); }
        }, this::openCustomMessages);
    }

    private void openFilter(Player player) {
        boolean login = files.getConfigBoolean("Messages.Filter.ForLogin", false);
        boolean logout = files.getConfigBoolean("Messages.Filter.ForLogout", false);
        String regex = String.join("\n", files.getConfigStringList("Messages.Filter.Regex"));
        List<DialogInput> inputs = List.of(
            DialogInput.bool("login", Component.text("Aplicar filtro al Login"), login, "true", "false"),
            DialogInput.bool("logout", Component.text("Aplicar filtro al Logout"), logout, "true", "false"),
            DialogInput.text("regex", Component.text("Regex (una expresion por linea)"))
                .initial(regex).width(400).maxLength(8192).multiline(TextDialogInput.MultilineOptions.create(120, 240)).build()
        );
        showSaveBack(player, "Filtro de nombres", inputs, (view, p) -> {
            List<String> values = view.getText("regex").lines().filter(s -> !s.isBlank()).toList();
            try {
                files.setConfig("Messages.Filter.ForLogin", view.getBoolean("login"));
                files.setConfig("Messages.Filter.ForLogout", view.getBoolean("logout"));
                files.setConfig("Messages.Filter.Regex", values);
                p.sendRichMessage("<green>Filtro guardado.</green>");
                openCustomMessages(p);
            } catch (IOException e) { fail(p, e); }
        }, this::openCustomMessages);
    }

    public void openModules(Player player) {
        List<DialogInput> inputs = List.of(
            module("silentChest", false), module("versionCheck", false), module("customMessages", true),
            module("firstJoinMessages", true), module("cuffed", false), module("elevator", true),
            module("nightSpeedup", true), module("damageControl", false), module("portalCreation", true),
            module("teleportWith", true), module("durabilityLoss", true), module("mirror", false),
            module("paintingEditor", false), module("vanish", false), module("armorstand", true),
            module("noTarget", false), module("selection", false), module("sitAnimation", true),
            module("rideAnimation", true), module("hat", true), module("chatBubble", true),
            module("tablist", true), module("namePlates", true)
        );
        showSaveBack(player, "Modulos revisados", inputs, (view, p) -> {
            try {
                for (DialogInput input : inputs) {
                    String key = input.key();
                    files.setModule(key, view.getBoolean(key));
                }
                p.sendRichMessage("<green>Modules.yml guardado.</green> <yellow>CMI requiere reinicio completo para aplicar cambios de modulos.</yellow>");
                openMain(p);
            } catch (IOException e) { fail(p, e); }
        }, this::openMain);
    }

    private DialogInput module(String key, boolean fallback) {
        return DialogInput.bool(key, Component.text(key), files.getModule(key, fallback), "true", "false");
    }

    private void showSaveBack(Player player, String title, List<DialogInput> inputs, SaveHandler save, java.util.function.Consumer<Player> back) {
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

    private Integer parseInt(String text) {
        try { return Integer.parseInt(text.trim()); } catch (Exception e) { return null; }
    }

    private void fail(Player player, Exception e) {
        plugin.getLogger().severe("No se pudo guardar CMI: " + e.getMessage());
        player.sendRichMessage("<red>No se pudo guardar. Revisa la consola; el archivo original no se elimina.</red>");
    }

    @FunctionalInterface
    private interface SaveHandler {
        void run(io.papermc.paper.dialog.DialogResponseView view, Player player);
    }
}
