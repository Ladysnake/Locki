------------------------------------------------------
Version 0.9.1
------------------------------------------------------
[Now published on Modrinth](https://modrinth.com/mod/locki)!

**Fixes**
- Fixed missing texture appearing on locked slots

------------------------------------------------------
Version 0.9.0
------------------------------------------------------
Updated to 1.19.3

------------------------------------------------------
Version 0.8.0
------------------------------------------------------
Updated to 1.19

**Changes**
- Now a [Quilt](https://quiltmc.org) mod !

------------------------------------------------------
Version 0.7.0
------------------------------------------------------
**Additions**
Now uses interface injection to expose methods in PlayerInventory

------------------------------------------------------
Version 0.6.2
------------------------------------------------------
**Mod Compatibility**
- Fixed BackSlot incompatibility for real this time

------------------------------------------------------
Version 0.6.1
------------------------------------------------------
**Mod Compatibility**
- Fixed crash at launch with BackSlot

------------------------------------------------------
Version 0.6.0
------------------------------------------------------
Updated to 1.18

------------------------------------------------------
Version 0.5.1
------------------------------------------------------
**Mod Compatibility**
- Fixed crash at launch with BackSlot

**Fixes**
- Ported death persistence change from 0.2.2

------------------------------------------------------
Version 0.5.0
------------------------------------------------------
**Mod Compatibility**
- Added support for BackSlot's back and belt slots

------------------------------------------------------
Version 0.4.1
------------------------------------------------------
**Fixes**
- Port recipe book fix from 0.2.1

------------------------------------------------------
Version 0.4.0
------------------------------------------------------
**Additions**
- Made lock persistence configurable

------------------------------------------------------
Version 0.3.1
------------------------------------------------------
**Fixes**
- Fixed a crash from illegally updating a final field

------------------------------------------------------
Version 0.3.0
------------------------------------------------------
Updated to 1.17

------------------------------------------------------
Version 0.2.2
------------------------------------------------------
**Changes**
- Made inventory locking persist after death

------------------------------------------------------
Version 0.2.1
------------------------------------------------------
**Fixes**
- Fixed a crash when crafting something while the crafting book is locked

------------------------------------------------------
Version 0.2.0
------------------------------------------------------
**Additions**
- Added an inventory node dedicated to the hotbar
- Added an inventory node dedicated to the recipe book
- Added a locking change callback

**Changes**
Inventory locking no longer takes effect in creative mode

**Fixes**
- Fixed all crafting result slots being locked instead of only the player's
- Fixed some glitches allowing players to select slots or items in the hotbar when they shouldn't
- Fixed random locking data being overwritten when loading a save

------------------------------------------------------
Version 0.1.1
------------------------------------------------------
**Fixes**
- Locki's command argument types are now properly registered

------------------------------------------------------
Version 0.1.0
------------------------------------------------------
*First release of Locki, split from Requiem 1.7.6*