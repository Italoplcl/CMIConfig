package cl.charlys.cmiconfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

final class DialogWriter {
    private DialogWriter() {}

    static Path write(Path cmiFolder) throws IOException {
        Path dialogs = cmiFolder.resolve("Dialogs");
        Files.createDirectories(dialogs);
        Path out = dialogs.resolve("CMIConfigDialogs.yml");
        Files.writeString(out, yaml(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return out;
    }

    private static String yaml() {
        return """
cmiconfig_main:
  Enabled: true
  Label: '{#gold}Configuración CMI'
  Description:
    Width: 320
    Lines:
    - '{#gray}Panel visual para administrar la configuración de CMI.'
    - '{#yellow}Solo aparecen aquí las secciones que ya estamos trabajando.'
  Buttons:
    Columns: 1
    List:
    - Label: '{#green}Mensajes personalizados'
      Width: 300
      OpenDialog: cmiconfig_custommessages
    - Label: '{#aqua}Módulos revisados'
      Width: 300
      OpenDialog: cmiconfig_modules
  Close:
    Label: '{#red}Cerrar'
    Width: 150

cmiconfig_custommessages:
  Enabled: true
  Label: '{#gold}Mensajes personalizados'
  Description:
    Width: 320
    Lines:
    - '{#gray}Login, logout, muerte y filtros.'
  Buttons:
    Columns: 1
    List:
    - Label: '{#green}Mensajes de entrada'
      Width: 300
      OpenDialog: cmiconfig_login
    - Label: '{#green}Mensajes de salida'
      Width: 300
      OpenDialog: cmiconfig_logout
    - Label: '{#red}Mensajes de muerte'
      Width: 300
      OpenDialog: cmiconfig_death
    - Label: '{#yellow}Filtro de nombres'
      Width: 300
      OpenDialog: cmiconfig_filter
  Close:
    Label: '{#gray}← Volver'
    OpenDialog: cmiconfig_main
    Width: 150

cmiconfig_login:
  Enabled: true
  Label: '{#gold}Mensajes de entrada'
  Description:
    Width: 340
    Lines:
    - '{#gray}Messages.Login del config.yml'
  Buttons:
    Columns: 2
    List:
    - Label: '{#green}Mostrar: SÍ'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Login.Disabled false']
    - Label: '{#red}Mostrar: NO'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Login.Disabled true']
    - Label: '{#green}Personalizado: SÍ'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Login.Custom.Use true']
    - Label: '{#red}Personalizado: NO'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Login.Custom.Use false']
    - Label: '{#green}Cambio servidor: SÍ'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Login.Custom.ServerSwitch true']
    - Label: '{#red}Cambio servidor: NO'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Login.Custom.ServerSwitch false']
    - Label: '{#yellow}AutoHide: -1'
      Width: 165
      Commands: ['cmiconfig setint config Messages.Login.AutoHideFrom -1']
    - Label: '{#yellow}AutoHide: 50'
      Width: 165
      Commands: ['cmiconfig setint config Messages.Login.AutoHideFrom 50']
  Close:
    Label: '{#gray}← Volver'
    OpenDialog: cmiconfig_custommessages

cmiconfig_logout:
  Enabled: true
  Label: '{#gold}Mensajes de salida'
  Description:
    Width: 340
    Lines:
    - '{#gray}Messages.Logout del config.yml'
  Buttons:
    Columns: 2
    List:
    - Label: '{#green}Mostrar: SÍ'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Logout.Disabled false']
    - Label: '{#red}Mostrar: NO'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Logout.Disabled true']
    - Label: '{#green}Personalizado: SÍ'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Logout.Custom.Use true']
    - Label: '{#red}Personalizado: NO'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Logout.Custom.Use false']
    - Label: '{#green}Cambio servidor: SÍ'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Logout.Custom.ServerSwitch true']
    - Label: '{#red}Cambio servidor: NO'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Logout.Custom.ServerSwitch false']
    - Label: '{#yellow}AutoHide: -1'
      Width: 165
      Commands: ['cmiconfig setint config Messages.Logout.AutoHideFrom -1']
    - Label: '{#yellow}AutoHide: 50'
      Width: 165
      Commands: ['cmiconfig setint config Messages.Logout.AutoHideFrom 50']
  Close:
    Label: '{#gray}← Volver'
    OpenDialog: cmiconfig_custommessages

cmiconfig_filter:
  Enabled: true
  Label: '{#gold}Filtro de nombres'
  Description:
    Width: 340
    Lines:
    - '{#gray}Oculta mensajes cuando el nombre coincide con el filtro Regex.'
    - '{#yellow}La lista Regex todavía no se altera desde este Dialog.'
  Buttons:
    Columns: 2
    List:
    - Label: '{#green}Login: ACTIVAR'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Filter.ForLogin true']
    - Label: '{#red}Login: DESACTIVAR'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Filter.ForLogin false']
    - Label: '{#green}Logout: ACTIVAR'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Filter.ForLogout true']
    - Label: '{#red}Logout: DESACTIVAR'
      Width: 165
      Commands: ['cmiconfig setbool config Messages.Filter.ForLogout false']
  Close:
    Label: '{#gray}← Volver'
    OpenDialog: cmiconfig_custommessages

cmiconfig_death:
  Enabled: true
  Label: '{#red}Mensajes de muerte'
  Description:
    Width: 350
    Lines:
    - '{#gray}Settings/DeathMessages.yml'
    - '{#yellow}Las listas de mundos/jugadores quedan visibles como siguiente tipo de editor.'
  Buttons:
    Columns: 2
    List:
    - Label: '{#green}Custom: ACTIVAR'
      Width: 170
      Commands: ['cmiconfig setbool death EnableCustom true']
    - Label: '{#red}Custom: DESACTIVAR'
      Width: 170
      Commands: ['cmiconfig setbool death EnableCustom false']
    - Label: '{#yellow}AutoHide: -1'
      Width: 170
      Commands: ['cmiconfig setint death AutoHideFrom -1']
    - Label: '{#yellow}AutoHide: 50'
      Width: 170
      Commands: ['cmiconfig setint death AutoHideFrom 50']
    - Label: '{#yellow}Rango: -1'
      Width: 170
      Commands: ['cmiconfig setint death Range -1']
    - Label: '{#yellow}Rango: 100'
      Width: 170
      Commands: ['cmiconfig setint death Range 100']
    - Label: '{#aqua}Destino: CHAT'
      Width: 170
      Commands: ['cmiconfig setenum death Destination plain plain actionBar']
    - Label: '{#aqua}Destino: ACTIONBAR'
      Width: 170
      Commands: ['cmiconfig setenum death Destination actionBar plain actionBar']
    - Label: '{#yellow}AntiSpam tiempo: 30s'
      Width: 170
      Commands: ['cmiconfig setint death AntiSpam.TimeRange 30']
    - Label: '{#yellow}AntiSpam cantidad: 3'
      Width: 170
      Commands: ['cmiconfig setint death AntiSpam.Count 3']
  Close:
    Label: '{#gray}← Volver'
    OpenDialog: cmiconfig_custommessages

cmiconfig_modules:
  Enabled: true
  Label: '{#aqua}Módulos revisados'
  Description:
    Width: 360
    Lines:
    - '{#yellow}Los cambios de Modules.yml requieren reinicio completo.'
    - '{#gray}Estos son únicamente módulos que ya decidiste explícitamente.'
  Buttons:
    Columns: 2
    List:
    - {Label: '{#red}silentChest OFF', Width: 175, Commands: ['cmiconfig module silentChest false']}
    - {Label: '{#red}versionCheck OFF', Width: 175, Commands: ['cmiconfig module versionCheck false']}
    - {Label: '{#green}elevator ON', Width: 175, Commands: ['cmiconfig module elevator true']}
    - {Label: '{#red}damageControl OFF', Width: 175, Commands: ['cmiconfig module damageControl false']}
    - {Label: '{#green}portalCreation ON', Width: 175, Commands: ['cmiconfig module portalCreation true']}
    - {Label: '{#red}vanish OFF', Width: 175, Commands: ['cmiconfig module vanish false']}
    - {Label: '{#green}armorstand ON', Width: 175, Commands: ['cmiconfig module armorstand true']}
    - {Label: '{#red}selection OFF', Width: 175, Commands: ['cmiconfig module selection false']}
    - {Label: '{#red}noTarget OFF', Width: 175, Commands: ['cmiconfig module noTarget false']}
    - {Label: '{#green}chatBubble ON', Width: 175, Commands: ['cmiconfig module chatBubble true']}
    - {Label: '{#green}tablist ON', Width: 175, Commands: ['cmiconfig module tablist true']}
    - {Label: '{#green}namePlates ON', Width: 175, Commands: ['cmiconfig module namePlates true']}
  Close:
    Label: '{#gray}← Volver'
    OpenDialog: cmiconfig_main
""";
    }
}
