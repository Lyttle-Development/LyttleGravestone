<div align="center">
  
# Lyttle Gravestone

[![Paper](https://img.shields.io/badge/Paper-1.21.x-blue)](https://papermc.io)
[![Hangar](https://img.shields.io/badge/Hangar-download-success)](https://hangar.papermc.io/Lyttle-Development)
[![Discord](https://img.shields.io/discord/941334383216967690?color=7289DA&label=Discord&logo=discord&logoColor=ffffff)](https://discord.gg/QfqFFPFFQZ)

> ✨ **Secure your items on death with player-specific gravestones and flexible retrieval options!** ✨

[📚 Features](#--features) • [⌨️ Commands](#-%EF%B8%8F-commands) • [🔑 Permissions](#--permissions) • [📥 Installation](#--installation) • [⚙️ Configuration](#%EF%B8%8F-configuration) • [📱 Support](#--support)

</div>

![Divider](https://raw.githubusercontent.com/Lyttle-Development/LyttleUtils/refs/heads/main/line.png)

## 🌟 Features

### 🎯 Core Plugin Features
- Automatic gravestone creation upon player death
- Secure item storage in player-specific gravestones
- Protected gravestones that only the owner can access
- Remote item retrieval system with configurable costs
- Multi-world support with additional retrieval costs
- Economy integration through Vault

---

### 🤌 Lyttle Certified
- Basic plugin without fluff
- No unnecessary features
- Full flexibility and configurability
- Open source and free to use (MIT License)

---

## ⌨️ Commands

> 💡 `<required>` `[optional]`

| Command                                                    | Permission                    | Description                               |
|:----------------------------------------------------------|:------------------------------|:------------------------------------------|
| `/retrieve-gravestone <world> <x> <y> <z> [confirm] [cost]` | `lyttlegravestone.retrieve`  | Retrieve items from a remote gravestone   |

---

## 🔑 Permissions

| Permission Node              | Description                                  | Default |
|:----------------------------|:---------------------------------------------|:--------|
| `lyttlegravestone.staff`    | Allows bypassing gravestone protection       | `❌`     |
| `lyttlegravestone.retrieve` | Allows using the retrieve-gravestone command | `✔️`    |

---

## 📥 Installation

### Quick Start
1. Download the latest version from [Hangar](https://hangar.papermc.io/Lyttle-Development/LyttleGravestone)
2. Place the `.jar` file in your server's `plugins` folder
3. Restart your server
4. Edit the configuration file to customize the plugin to your needs
5. Use `/plugin reload` to apply changes

---

### 📋 Requirements
- Java 21 or newer
- Paper 1.21.x+
- Vault (for economy features)

---

### 💫 Dependencies
- [Vault](https://www.spigotmc.org/resources/vault.34315/) - For economy integration (optional)

---

### 📝 Configuration Files
#### 🔧 `config.yml`
```yaml
# Retrieve Command Settings
retrieve_command_active: true 
retrieve_command_blocks: 100 
retrieve_command_price: 3 
retrieve_command_price__other_world: 100

# Economy Integration
use_vault: true
``` 

### 🔄 The #defaults Folder
The folder contains original configuration files and serves as:
1. **Backup Reference**: Original configuration files
2. **Reset Option**: Default settings restoration
3. **Update Safety**: Preserved during updates
4. **Documentation**: Complete configuration options with comments

> 💡 **Never modify files in the #defaults folder!** They are automatically overwritten during server restarts.

---

## 💬 Support

<div align="center">

### 🤝 Need Help?

[![Discord](https://img.shields.io/discord/941334383216967690?color=7289DA&label=Join%20Our%20Discord&logo=discord&logoColor=ffffff&style=for-the-badge)](https://discord.gg/QfqFFPFFQZ)

🐛 Found a bug? [Open an Issue](https://github.com/Lyttle-Development/LyttleGravestone/issues)  
💡 Have a suggestion? [Share your idea](https://github.com/Lyttle-Development/LyttleGravestone/issues)

</div>

---

## 📜 License

<div align="center">

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

### 🌟 Made with the lyttlest details in mind by [Lyttle Development](https://www.lyttledevelopment.com)

If you enjoy this plugin, please consider:

⭐ Giving it a star on GitHub <br>
💬 Sharing it with other server owners<br>
🎁 Supporting development through [Donations](https://github.com/LyttleDevelopment)

![Divider](https://raw.githubusercontent.com/Lyttle-Development/LyttleUtils/refs/heads/main/line.png)

</div>
