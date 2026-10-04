# DOCUMENTATION

## Objetivo

CMIConfigDialogs no reemplaza CMI. Su función es presentar configuraciones de CMI mediante Dialogs y escribir únicamente los valores autorizados en los archivos originales.

## Arquitectura actual

- `CMIConfigDialogsPlugin`: comando, permisos, validación y acceso a archivos.
- `DialogWriter`: genera los Dialogs que CMI carga desde `plugins/CMI/Dialogs/CMIConfigDialogs.yml`.
- `YamlScalarEditor`: lee y reemplaza valores escalares conservando comentarios y el resto del archivo.

## Archivos de CMI utilizados

- `plugins/CMI/config.yml`
- `plugins/CMI/Settings/Modules.yml`
- `plugins/CMI/Settings/DeathMessages.yml`
- `plugins/CMI/Dialogs/CMIConfigDialogs.yml` (generado por este plugin)

## customMessages mapeado

### config.yml

- `Messages.Login.Disabled`
- `Messages.Login.AutoHideFrom`
- `Messages.Login.Custom.Use`
- `Messages.Login.Custom.ServerSwitch`
- `Messages.Logout.Disabled`
- `Messages.Logout.AutoHideFrom`
- `Messages.Logout.Custom.Use`
- `Messages.Logout.Custom.ServerSwitch`
- `Messages.Filter.ForLogin`
- `Messages.Filter.ForLogout`
- `Messages.Filter.Regex` — pendiente de definir editor de listas.

`Messages.FirstJoinMessage.Use` no se incluye dentro de customMessages porque CMI posee el módulo independiente `firstJoinMessages`.

### Settings/DeathMessages.yml

- `EnableCustom`
- `AutoHideFrom`
- `Range`
- `Destination`
- `AntiSpam.TimeRange`
- `AntiSpam.Count`
- `DisabledWorlds` — pendiente de editor de listas.
- `MutedWorlds` — pendiente de editor de listas.
- `IgnoredPlayers` — pendiente de editor de listas.

## Módulos decididos por el usuario e incluidos

- `silentChest`: OFF
- `versionCheck`: OFF
- `elevator`: ON
- `damageControl`: OFF
- `portalCreation`: ON
- `vanish`: OFF
- `armorstand`: ON
- `selection`: OFF
- `noTarget`: OFF
- `chatBubble`: ON
- `tablist`: ON
- `namePlates`: ON

## Corrección detectada durante la implementación

En el `Settings/Modules.yml` suministrado no existen claves de módulo llamadas `chat` ni `signs`. Por eso no se escriben desde este plugin. Sí existen `chatBubble`, `tablist`, `namePlates`, `dynamicSigns` y `signEdit`. No se ha decidido reinterpretar `chat` o `signs` como otra clave sin confirmación del usuario.

## Regla del proyecto

El código no debe incorporar, excluir o reinterpretar opciones funcionales sin decisión del usuario. Las ampliaciones se hacen módulo por módulo después de revisar la configuración real de CMI.
