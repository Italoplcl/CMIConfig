# CMIConfigDialogs

Plugin propio para Paper 26.x que abre dialogs mediante la **Dialog API de Paper** y edita archivos de configuración de CMI.

## Importante

- `/cmiconfig` pertenece a este plugin.
- No ejecuta `/cmi dialogs`.
- No crea dialogs dentro de `plugins/CMI/Dialogs`.
- CMI y CMILib son el software/configuración administrados, no el motor de la interfaz.

## Estado 0.2.0

Implementado únicamente sobre decisiones ya revisadas:

- Menú principal.
- `customMessages` (si está activo): Login, Logout y Filter de `CMI/config.yml`.
- Editor de los módulos ya revisados con el usuario.
- `firstJoinMessages` se mantiene separado y todavía no tiene editor propio.
- `deathMessages` NO está dentro de customMessages: CMI lo declara como módulo independiente y todavía no se ha decidido su inclusión.
- Backups automáticos antes de guardar.

## Compilar

Sube el proyecto a GitHub. El workflow `.github/workflows/build.yml` compila con Java 25 y Paper 26.2 API. El JAR queda como artifact de GitHub Actions.

## Permiso

`cmiconfig.admin` (OP por defecto)
