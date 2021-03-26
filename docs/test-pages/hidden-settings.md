?> These are some settings you can configure (toggle) manually to control XClipper behaviors.

## Table of contents <!-- {docsify-ignore} -->

- [Why & how to edit them?](#why--how-to-edit-them)
- [List of all settings](#list-of-all-settings)

## Why & how to edit them?

The reason why these settings are not directly controlled from the main application is that they are too technical & are cease to exist for only one specific behavior.

Each of these can be modified from XClipper's config.xml located in `%appData%\XClipper\config.xml`.

## List of all settings?

| Name                        | Default | What can be controlled?                                                                                                                               |
| --------------------------- | ------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ExitOnCrash`               | `True`  | If set to `False` application will not crash when an internal major failure occurs.                                                                   |
| `NoNotifyChanges`           | `False` | If set to `True` XClipper will not show notification about any changes occurs to database.                                                            |
| `UseExperimentalKeyCapture` | `True`  | If set to `True` XClipper will use an experimental method of hot key detection goes by stream of key events for faster response (App restart needed). |
