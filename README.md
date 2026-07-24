<p align="center"><img src="https://github.com/user-attachments/assets/f36c5f43-2852-40fb-8535-4e7ad269eeda" alt="Logo" width="500"></p>
<h1 align="center">Create: Mobile Packages  <br>
  <a href="https://www.curseforge.com/minecraft/mc-mods/create-mobile-packages"><img src="https://cf.way2muchnoise.eu/1232978.svg" alt="CF"></a>
  <a href="https://modrinth.com/mod/create-mobile-packages"><img src="https://img.shields.io/modrinth/dt/create-mobile-packages?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
  <a href="https://crowdin.com/project/create-mobile-packages"><img src="https://badges.crowdin.net/create-mobile-packages/localized.svg" alt="Crowdin"></a>
<br></br>
</h1>

A Minecraft mod that adds support for delivering Create Mod Packages directly to the player.

## Requirements

### 1.21.1 NeoForge

- NeoForge 21.1.219 or newer
- Create 6.0.10 or newer

### 1.20.1 Forge (no longer supported)
- Forge 47.3.33 or newer
- Create 6.0.8 or newer

## Download?
- Modrinth: https://modrinth.com/mod/create-mobile-packages
- Curseforge: https://www.curseforge.com/minecraft/mc-mods/create-mobile-packages

## Overview

### Logistics Network

The core of the mod is the **Logistics Network**.

- **Linking**: Bee Ports must be linked to a network. Place a new one to create a network, or link to an existing one.
- **Security**: Robo Bees only fly to ports and players within the same network.
- **Membership**: Players can join a network via the Bee Port GUI. View your networks with the `H` hotkey.

### Bee Port

The hub for your logistics network.

- **Send & Receive**: Automates package delivery to addresses (players or other ports).
- **Requirement**: Requires a **Robo Bee** item in its internal inventory to send packages.
- **Return Mode**: Toggle to have Robo Bees return to this port after delivering to a player.
- **Filter Mode**: Toggle between all items, only packages, only bees can be exported and imported by hoppers, chutes,
  funnels, etc.
- **Automation**: By default, it pulls packages from adjacent inventories. When powered by **Redstone**, it pushes items
  to adjacent inventories.

![Bee Port](https://github.com/user-attachments/assets/3b15287e-44fc-4ebc-9e59-a38fc2a5da49)

### Robo Bee

The courier entity.

- **Delivery**: Carries packages to their destination address.
- **Spawning**: Right-click with a **Robo Bee** item to spawn.
- **Network**: The bee binds to the network the item is linked to. If the item is unlinked, the bee will target any
  available port.

![Robo Bee](https://github.com/user-attachments/assets/9b78670f-a2f8-4343-bd58-5936103a9596)

### Portable Stock Ticker

A handheld device for remote item management.

- **Request Items**: Access your stock remotely by linking to Create Logistics Network.
- **Send Items**: Use the **Trash Slots** to send items from your inventory to a specific address via Robo Bee. (The Bee Port needs to be in the same network as the Portable Stock Ticker)
- **Crafting support** within the request interface
- **JEI Support**: Synchronized item search.

![Stock Ticker](https://github.com/user-attachments/assets/d8a85e58-3ffa-4c2a-8b74-48f6c2b76642)

### Mobile Packager

A utility for managing packages on the go.

- **Create**: Pack up to 9 item stacks into an addressed package.
- **Edit**: View and modify existing package contents (Sneak + Use).

![Mobile Packager](https://github.com/user-attachments/assets/9d21daf4-f64e-4df8-9ad3-a689e6f83ab5)

## Commands

Admin commands for managing logistics networks.

- `/cmp network list` - Display all networks, player counts, and owners.
- `/cmp network add <player> <networkId>` - Force add a player to a network.
- `/cmp network remove <player> <networkId>` - Force remove a player from a network.
- `/cmp robos clear` - Remove all Robo Bees from the world.

## Translations

[![Crowdin](https://badges.crowdin.net/create-mobile-packages/localized.svg)](https://crowdin.com/project/create-mobile-packages)

Help translate the mod on [Crowdin](https://crowdin.com/project/create-mobile-packages).

## Gallery

### Simple Setup
Link the Port and the Stock Link, by clicking with one item on the other block. You can link your Portable Stock Ticker by Shift-Clicking with it on the Stock Link, Port or other Linked Blocks
<img width="1920" height="1043" alt="2026-03-17_21 28 01" src="https://github.com/user-attachments/assets/9ceff20f-8445-4408-9e41-d7f28a7e6831" />

### Robo Bee on its way delivering to you
<img width="1920" height="1043" alt="2026-03-17_21 28 22" src="https://github.com/user-attachments/assets/f59466f4-b04c-48cd-9a37-b65bbeb907ee" />

### Robo Bee on its way picking up your Trash Slot items to deliver them to "Port1"
<img width="1920" height="1043" alt="2026-03-17_21 34 10" src="https://github.com/user-attachments/assets/1ef583d9-b4c8-4716-bf8e-9e120f2532e8" />

### Portable Stock Ticker selecting items to be send to you
<img width="1920" height="1043" alt="2026-03-17_21 29 00" src="https://github.com/user-attachments/assets/2abb559d-e87e-41b2-ab21-e0382ac81a77" />

### Admin Console Command to manage Networks (can also be done via UI)
<img width="1004" height="281" alt="2026-03-17_21 36 13" src="https://github.com/user-attachments/assets/b9db957e-8e66-4759-9d57-4f1c6ead4e5e" />


