
------------------------------------------------------
Create: Mobile Packages Unreleased
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

### Translations

- Added German translation
- Added Chinese (Simplified Han script) translation
- Added Russian translation
- Added French translation
- Added Japanese translation
- Added Portuguese (Brazilian) translation

