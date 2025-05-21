<p align="center"><img src="https://github.com/user-attachments/assets/f36c5f43-2852-40fb-8535-4e7ad269eeda" alt="Logo" width="500"></p>
<h1 align="center">Create: Mobile Packages  <br>
  <a href="https://www.curseforge.com/minecraft/mc-mods/create-mobile-packages"><img src="https://cf.way2muchnoise.eu/1232978.svg" alt="CF"></a>
  <a href="https://modrinth.com/mod/create-mobile-packages"><img src="https://img.shields.io/modrinth/dt/create-mobile-packages?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
  <br></br>
</h1>

A Minecraft mod that adds support for delivering Create Mod Packages directly to the player.

## Requirements

### 1.20.1 Forge
- Forge 47.1.3 or newer
- Create 6.0.4 or newer

### 1.21.1 NeoForge
- NeoForge 21.1.159 or newer
- Create 6.0.4 or newer


## Where to Download?
- Modrinth: https://modrinth.com/mod/create-mobile-packages
- Curseforge: https://www.curseforge.com/minecraft/mc-mods/create-mobile-packages

## Translations 
This project is translated using Hosted Weblate. https://hosted.weblate.org/engage/create-mobile-packages/

You're welcome to contribute to the translations or add your language.

<a href="https://hosted.weblate.org/engage/create-mobile-packages/">
<img src="https://hosted.weblate.org/widget/create-mobile-packages/mc1-20-1dev/multi-auto.svg" alt="translation state" />
</a>

## Items
### Bee Port

The **Bee Port** is a specialized block that automates the delivery of Create mod Packages to players or other Bee Ports.

Key Features:
- **Package delivery system** that reads address labels and send a bee with the packages to players or other Bee Ports
- **Insert packages manually or automatically** using Funnels, Chutes, or Hoppers
- **Pull packages** from adjacent inventories
- **Push packages** to adjacent inventories if:
    - The address matches the Bee Port's configured addresses
    - The Bee Port receives a redstone signal

![bee Port](https://github.com/user-attachments/assets/3b15287e-44fc-4ebc-9e59-a38fc2a5da49)

#### Robo Bee
The **Robo Bee** is an entity that delivers Create mod Packages to their destination.
It follows the address on the package, delivering directly to players or between Bee Ports as needed.

Key Features:
- **Spawned using the item**
- **Required by Bee Ports** to send packages
- **Carries packages** directly to players or between Bee Ports

![robo_bee](https://github.com/user-attachments/assets/9b78670f-a2f8-4343-bd58-5936103a9596)

### Portable Stock Ticker

The **Portable Stock Ticker** is a handheld device that integrates with a Create mod network, allowing players to request packages remotely.

Once linked to a **Stock Ticker** or a **Stock Link**, it provides a **Stockkeeper interface**, enabling players to request items on the go without needing direct access to a Stockkeeper.

Key Features:
- **Remote Item Requesting** via Stockkeeper interface
- **JEI-synchronized search**
- **Crafting support** within the request interface
- **Category synchronization**: If linked to a Stock Ticker with categories, those categories will be copied to the Portable Stock Ticker

![Controller](https://github.com/user-attachments/assets/d8a85e58-3ffa-4c2a-8b74-48f6c2b76642)

## Gallery

![image](https://github.com/user-attachments/assets/80b6f028-61f9-415a-aa4d-bd911d1d1997)
![image](https://github.com/user-attachments/assets/9c9afb41-4671-4092-9a4f-0e23dbf155bb)
