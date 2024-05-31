# PlayerPig

Server-side Minecraft Fabric mod that spawns a pig with your username when you log out and teleports you to that pig when you log in. (Created for 1.16.5)

### Commands

* "/pigfix" - removes one player pig within 5 blocks of the player. (Anybody can use)
    * Maybe chance of duplicate pigs spawning???
    * More testing required before this requires OP to execute.
* "/piglist" - shows a list of all player pigs in the world. Click on the text to teleport to selected pig. (Requires OP)
* "/pigremoveall" - removes all player pigs even if they are in unloaded chunks. (Requires OP)

### Extra Features

* PlayerPigs won't die.
* Leads won't break (up to 50 blocks) if connected to PlayerPig.
* Players log in with fire resistance for 20 seconds if their pig is on fire.
    * If the dumbass pig walks into lava or something
* PlayerPigs will be teleported to 0, 100, 0 (relative to their dimension) if they fall into the void.
* Fix for players logging in inside a block.

### Requirements
* Fabric API: https://www.curseforge.com/minecraft/mc-mods/fabric-api/files