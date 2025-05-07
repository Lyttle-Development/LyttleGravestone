# LyttleGravestone

LyttleGravestone is a simple plugin that adds gravestones to Minecraft. When a player dies, a gravestone is placed at their death location containing their items. The player can then break the gravestone to retrieve their items.

## Features

- **Gravestones**: When a player dies, a gravestone is placed at their death location containing their items.
- **Item Retrieval**: Players can break or open their gravestone to retrieve their items.
- **Protection**: Gravestones are protected from other players breaking or opening them.
- **Command retrieval**: Players can use a command to retrieve their items if they are unable to break the gravestone.

## Commands

- `/retrieve-gravestone <world> <x> <y> <z> <?confirm> <?cost>`: Retrieves the items from a gravestone at the specified location.

## Permissions

- `lyttlegravestone.staff`: Allows bypassing the protection on gravestones.

## Installation

1. Download the latest version of the plugin from the [releases page](https://github.com/Lyttle-Development/LyttleGravestone/releases).
2. Place the downloaded `.jar` file into your server's `plugins` folder.
3. Restart your server or use a plugin manager to load the plugin.

## Configuration

After the first run, a `config.yml` file will be created in the `plugins/LyttleGravestone` directory. This file can be modified to change the plugin's settings and messages.

## Support

If you encounter any issues or have any questions, please [open an issue](https://github.com/Lyttle-Development/LyttleGravestone/issues) on GitHub.

## Contributing

Contributions are welcome! Please read the [contributing guidelines](CONTRIBUTING.md) before getting started.
