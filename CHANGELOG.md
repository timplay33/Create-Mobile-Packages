------------------------------------------------------
Create: Mobile Packages - v0.7.7 - 1.21.1 - unreleased
------------------------------------------------------

### Changes

- BeePort name is shown instead of coordinates if posible (#345)
- fix Portable Stock Ticker loses categories after restart (hotkey) (#348, #174)
- Fix discord Curseforge link in ci
- Fix BeePortBlockEntity.onChunkUnloaded() synchronously loading a chunk (#350)
- Fix Owners are now members of a network by default (#346)

------------------------------------------------------
Create: Mobile Packages - v0.7.6 - 1.21.1 - 08.05.2026
------------------------------------------------------

### Changes

- Update Toast design (#338)
- Fix address bar text key (#339)
- Fix item count on Toast for fluids with FluidLogistic (#342)
- Add discord release notification workflow to CI pipeline

------------------------------------------------------
Create: Mobile Packages - v0.7.5 - 1.21.1 - 07.05.2026
------------------------------------------------------

### Changes

- Added support for FluidLogistic (#336, #333)
- Changed CI pipeline to include the Changelog in the release description

------------------------------------------------------
Create: Mobile Packages - v0.7.4 - 1.21.1 - 22.04.2026
------------------------------------------------------

### Changes
- Fix support for Create 6.0.10 (#324, #323)

------------------------------------------------------
Create: Mobile Packages - v0.7.3 - 1.21.1 - 20.04.2026
------------------------------------------------------

### Changes
- Fix bees not being able to return when bee port is not aligned properly on the world grid (enter / exit loop) (#317, #318)

------------------------------------------------------
Create: Mobile Packages - v0.7.2 - 1.21.1 - 19.04.2026
------------------------------------------------------

### Changes
- Add support for Sable & Create Aeronautics (#315, #313)

------------------------------------------------------
Create: Mobile Packages - v0.7.1 - 1.21.1 - 19.04.2026
------------------------------------------------------

### Changes
- Update Translations (#310)
- allow for custom Toast positioning (#311, #312)

------------------------------------------------------
Create: Mobile Packages - v0.7.0 - 1.21.1 - 17.03.2026
------------------------------------------------------

### Additions

- Added Bee Port Return Mode toggle in the Bee Port GUI (#136)
  - New button that toggles between normal delivery and return mode
  - In return mode, Robo Bees spawned from that port fly back to their origin port after successful player delivery

- Added Admin/OP Commands for Network Management (#305)
  - `/cmp network list` - Display all logistics networks with names, player counts, lock status, and owner information
  - `/cmp network add <player> <networkId>` - Add a player to a network (auto-filters to named networks only)
  - `/cmp network remove <player> <networkId>` - Remove a player from a network (auto-filters to networks the player is
    part of)
  - Full command auto-completion with intelligent filtering

- Added TrashSlots to the Portable Stock Ticker allowing players to send items (#299)
    - Set an address
    - Put items in the TrashSlots
    - A Robo Bee will come and pick up the items and send them to the address

### Bug Fixes

- Fixed Port with full packages inventory can't accept RoboBee without package (#301)
- Fixed Portable Stock Ticker not updating to an empty item list if the last item is removed (#302)
- Fixed Portable Stock Ticker losing address after restart (#287)

------------------------------------------------------
Create: Mobile Packages - v0.6.1 - 1.21.1 - 20.02.2026
------------------------------------------------------

### Bug Fixes
- Fixed Stock quantity not visible in the portable stock ticker (#290)
- Fixed Fix Inconsistent amount of push and popPose (#292)
- Add Bee Port clearing recipe

### Changes
- Update Translations (#285, #297)

------------------------------------------------------
Create: Mobile Packages - v0.6.0 - 1.21.1 - 15.02.2026
------------------------------------------------------

### Breaking Changes
#### Logistics Network support

**Warning! This will break all existing ports. They need to be replaced to link to a logistics network!**

- Link a bee port to a logistics network or place one to create a new one
- Robo Bees will only fly to Robo Bee Ports and Players within their network
- The Robo Bee Item can be linked to a logistics network
- If a Robo Bee Item is not connected to a network, then the bee will fly to any port (closest)
- Players can add themselves to a logistics network by clicking the new button on the bee port GUI
- Players can see all networks they are part of with the hotkey `H`
- Players can pick up Robo Bees in their network by hitting them

### Changes
- Simplify Robo behavior state system
- Add nametag `No valid target` to Robos
- Add Abstractions for the target
- Bump create_version to 6.0.9-215
- Disable dimension travel for robos (nether & end portals) (#250)
- Update Translations (#243, #269)

### Bug Fixes
- Add support for Create `6.0.7` (#241)
- Fixed item duplication in the Mobile Packager (#270)
- Fixed missing Robo Bee clear recipe (#276)
- Fixed recipe freezes (#279)
- Fixed bee port duplication bug (#252)

### Additions

- Added support for EMI (#279)
- Added `/cmp robos clear` command to remove all robos (#261)
- Added Mobile Packager for packaging items on the go

------------------------------------------------------
Create: Mobile Packages - v0.5.5 - 1.21.1 - 13.09.2025
------------------------------------------------------

### Bug Fixes
- Fix Hotkey for Portable Stock Ticker Network Error if no Portable Stock Ticker is in the inventory

------------------------------------------------------
Create: Mobile Packages - v0.5.4 - 1.21.1 - 13.07.2025
------------------------------------------------------

### Changes
- Update Create Factory Abstractions to v1.4.6

------------------------------------------------------
Create: Mobile Packages - v0.5.3 - 1.21.1 - 01.07.2025
------------------------------------------------------

### Known not yet fixed issues
- Crafting with the Portable Stock Ticker doesn't work properly without Create: Factory Logistics

### Additions
- Added always suggesting the player's name as the target address.

### Bug Fixes
- Fix handling of blank addresses
- Fix misuse of `getStackInSlot`
- Add "Not linked" error message when trying to open an unlinked Portable Stock Ticker with the hotkey
- Fix Robo Bees are now invincible to everything else than `PLAYER_ATTACK` 
- Fix Robo Bees can no longer be set on fire
- Fix Items in the Portable Stock Ticker are now sorted by count
- Fix Log spamming of "Releasing entity on travel: ..."
- Fix Create Factory Logistics support with Factory Abstractions
- Fix Packages inside Robo Bee Port get voided when redstone is applied
- Fix Robo Bees drops of Bee Ports (Contraptions & CarryOn)
- Fix CarryOn support for Robo Bees
- Changed Robo Bee Port now outputs all Items in adjacent inventories when redstone powered
- Changed Robo Bee Port now no longer pulls Items from adjacent inventories when redstone powered

------------------------------------------------------
Create: Mobile Packages - v0.5.2 1.21.1 - 09.06.2025
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
