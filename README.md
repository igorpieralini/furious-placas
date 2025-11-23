# FURIOUS-PLACAS Plugin 🛒

**FURIOUS-PLACAS** is a lightweight Minecraft Paper plugin (version 1.8.8) that lets players **create and use shop signs**. Developed in **Java** with **Maven**, it’s easy to configure and enhances your server with a simple in-game shop system.

## Features ✨

* Create shops using signs for buying and selling items.
* Fully configurable via `config.yml`.
* Seamless integration with Paper 1.8.8 servers.
* Minimal performance impact.

## Installation 🚀

1. Download the latest `FURIOUS-PLACAS.jar` from the repository or build it using Maven:

```bash
mvn clean package
```

2. Place the `.jar` file in your server’s `plugins` folder.
3. Restart or reload your server.

---

## Configuration ⚙️

Edit the `config.yml` file (generated on first run):

---

## Permissions 🔑

* `furiousplacas.use` – Allows players to interact with shop signs.
* `furiousplacas.create` – Allows players to create shop signs.
* `furiousplacas.reload` – Allows reloading discount configuration.

> Ensure player groups have appropriate permissions to create or use shops.

---

## Usage 🎮

1. Place a sign and write the configured `[Furious Loja]` text on the first line.
2. Configure the item, quantity, and price on the remaining lines according to your server rules.
3. Players can now buy or sell items directly via the sign.

---

## Development 🛠️

* Java 8 compatible.
* Built with Maven.
* Developer: **Igor Pieralini**
