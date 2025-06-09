------------------------------------------------------
Create: Mobile Packages - v0.5.2 1.21.1 - unreleased
------------------------------------------------------

### Bug Fixes
- Fixed Portable Stock Ticker hotkey crashing the game
- Fixed Robo Bee Port Bee slot limit
- Fixed multiple Robo Bees being able to fly to the same Port at the same time
- Fixed Robo Bee spawned by summon command flying to the closest Port to 0,0,0
- Fixed rendering of non-standard packages carried by Robo Bees
- Fixed Support for multiple Portable Stock Tickers in the same inventory By preferring the item in the Main Hand
- Fixed Funnels and Chutes not being able to insert and extract from a Bee Port

### Additions
- Added "@" prefix for player names. If the package address contains an "@" only everything behind it is considered a player name. Eg: `some text goes hier@PlayerName` will send the package to the player with the name `PlayerName`

------------------------------------------------------
Create: Mobile Packages - v0.5.1 1.21.1 - 04.06.2025
------------------------------------------------------

### Bug Fixes
- Fixed Crafting recipes


------------------------------------------------------
Create: Mobile Packages - v0.5.0 1.21.1 - 04.06.2025
------------------------------------------------------

### Changes

- Changed **Drone Port** to **Bee Port**
- Changed **Drone Controller** to **Portable Stock Ticker**
- Added Bee Ports can Pull Packages from an adjacent Inventory
- Added Bee Ports can Push Packages to an adjacent Inventory if the Port is redstone powered and the package address matches the one on the port
- Added JEI Search Synchronization in Portable Stock Ticker
- Added JEI Crafting support in Portable Stock Ticker
- Added Robo Bee Item with a crating recipe
- Added sending a Package requires a Robo Bee item in the Bee Port
- Added if a Bee enters a Port, it will be added to the Port's Bee Slot
- Added a Bee Port can request a Bee from other Ports if it needs one but doesn't has one.
- Added Robo Bees can't take damage anymore they now behave like a minecart
- Changed the crafting recipes for the Bee Port and the Portable Stock Ticker
- Added Bee Port can now be placed with the front facing the player rather than north
- Added a Config option to disable Port-to-Port sending
- Added a Config option to set max travel distance.
- Added status of a traveling Bee is displayed in the target Bee Port
- Added target Address of a traveling Bee is displayed as the Nametag
- Added a Config option to disable Bee nametags
- Added Categories to the Portable Stock Ticker (added by linking to a Stock Ticker with Categories)
- Added rigging to a package carried by a Robo Bee
- Added when spawning a Robo Bee with the Robo Bee Item while holding a package in the offhand, the package will be rigged to the Robo Bee allowing Player to Player and Player to Port transfer
- Added Keybind to open a Portable Stock Ticker being in the player inventory (default: `G`)
- Updated Ponder for the Robo Bee Port
- Added Tooltip to the Robo Bee Item
- Automated Publishing to Modrinth & CurseForge

### Bug Fixes

- Fixed Robo Bees and the Package they carry not being saved
- Fixed Bee Port now only allows one Bee to fly to it at a time
- Fixed Bee Port particle size being too detailed
- Fixed Portable Stock Ticker address not being saved to the NBT
- Fixed applied Foil effect to a linked Portable Stock Ticker
- Changed Packages are no longer their own entity but are now a part of the Robo Bee entity
- Fixed Robo Bees can't fly through unloaded chunks, by adding a global RoboManager that keeps track of all Robo Bees and ticks them
- Fixed Offhand block is placed when opening a Portable Stock Ticker
- Fixed Portable Stock Ticker not working with Create: Factory Logistics
- Fixed Portable Stock Ticker showing Fluids from Create: Factory Logistics as Air by not showing them at all
- Fixed Portable Stock Ticker not saving the address when the player closes the GUI
- Fixed Robo Bee Port is considered full at 63 Robo Bees instead of 64
- Added Robo Bee in a Robo Bee Port can now be refilled with a hopper or a chute
- Fixed java.util.ConcurrentModificationException: null in RoboManager
- Fixed Robo Bees not Stopping when the target becomes null (Example: when Port becomes full while a Robo Bee is landing)
- Added missing Robo Bee Port rotation Textures
- Fixed a manually placed Robo Bee flies to the closest Port to 0,0,0 instead of the closest Port to the Robo Bee
- Fixed Port not beeing able to request a bee

### Translations

- Added German translation
- Added Chinese (Simplified Han script) translation
- Added Russian translation
- Added French translation
- Added Japanese translation
- Added Portuguese (Brazilian) translation
- Updated English translation
- Added Polish translation
- Added Swedish translation
