# CMIConfigDialogs

Panel administrativo para editar configuraciones de **CMI** mediante los Dialogs nativos de Minecraft/CMI.

## Estado

Primera versión de prueba basada en la instalación analizada:

- CMI `9.8.10.3`
- CMILib `1.6.0.1`
- Java 21
- Paper/Purpur compatible mediante Paper API

Esta versión implementa únicamente lo que ya se revisó para validar el enfoque antes de ampliar el proyecto.

## Incluido ahora

- `/cmiconfig` abre el Dialog principal.
- Genera `plugins/CMI/Dialogs/CMIConfigDialogs.yml`.
- Editor de `customMessages`:
  - Login: Disabled, AutoHideFrom, Custom.Use, Custom.ServerSwitch.
  - Logout: Disabled, AutoHideFrom, Custom.Use, Custom.ServerSwitch.
  - Filtro: ForLogin y ForLogout.
  - DeathMessages: EnableCustom, AutoHideFrom, Range, Destination y AntiSpam.
- Editor de los módulos que ya fueron decididos explícitamente.
- Backups automáticos antes de modificar YAML.
- Escritura puntual de valores sin reserializar el YAML completo, para conservar comentarios y estructura de CMI.
- Lista blanca interna: los botones no pueden escribir rutas arbitrarias.

## Instalación

1. Compila el proyecto con GitHub Actions o `gradle build`.
2. Copia el JAR de `build/libs/` a `plugins/`.
3. Deben estar instalados CMI y CMILib.
4. Inicia el servidor.
5. CMIConfigDialogs creará `plugins/CMI/Dialogs/CMIConfigDialogs.yml`.
6. Haz que CMI relea sus Dialogs/configuración si es necesario.
7. Ejecuta `/cmiconfig` como OP o con `cmiconfig.admin`.

## Seguridad

Los archivos originales se respaldan en una carpeta `CMIConfigDialogs-backups` junto al archivo modificado. El editor solo permite rutas expresamente incluidas en el código.

## Importante

Los cambios de `Settings/Modules.yml` requieren reinicio completo del servidor, según el propio archivo de CMI.

Esta versión no edita todavía listas YAML como `Messages.Filter.Regex`, `DisabledWorlds`, `MutedWorlds` o `IgnoredPlayers`. No se eliminaron del diseño por considerarlas innecesarias: requieren un editor de listas que todavía debe definirse.

## Compilación en GitHub

El workflow `.github/workflows/build.yml` compila automáticamente y publica el JAR como artifact de GitHub Actions.
