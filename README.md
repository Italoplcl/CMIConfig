# CMIConfigDialogs

Plugin propio para Paper 26.x que abre dialogs mediante la Dialog API de Paper y permite navegar y editar las configuraciones YAML instaladas de CMI y CMILib.

## Funcionamiento

- `/cmiconfig` pertenece a este plugin.
- No ejecuta `/cmi dialogs`.
- No crea dialogs dentro de `plugins/CMI/Dialogs`.
- CMI y CMILib son los archivos administrados; CMI no controla la interfaz.
- El editor descubre los archivos `.yml`/`.yaml` existentes dentro de `plugins/CMI` y `plugins/CMILib`.
- Las secciones YAML se navegan como submenus.
- Los booleanos se editan como opciones booleanas.
- Numeros, textos y listas conservan su tipo al guardarse.
- Las listas complejas se editan como YAML, incluyendo listas con mapas anidados.
- Los archivos y secciones extensos se paginan.
- El area Mensajes incluye acceso a `Messages` de `config.yml`, `Settings/DeathMessages.yml` y `Translations/DeathMessages`.
- Antes de guardar se crea una copia de respaldo en `plugins/CMIConfigDialogs/backups`.

## Archivos cubiertos

El plugin no mantiene una lista cerrada de opciones: recorre los YAML que realmente existen en la instalacion. Por ello cubre `CMI/config.yml`, todos los YAML de `CMI/Settings`, `CMI/Saves`, `CMI/Translations`, `CMI/CustomAlias`, `CMI/Kits`, `CMI/Dialogs` y cualquier otro YAML dentro de CMI; hace lo mismo con CMILib.

## Compilar

El workflow `.github/workflows/build.yml` compila con Java 25 y Paper 26.2 API. El JAR queda como artifact de GitHub Actions.

## Permiso

`cmiconfig.admin` (OP por defecto)
