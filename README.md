# Locki
An inventory control library

## Using Locki
### Through Fabric Permissions API

```java
if (Permissions.check(player, "locki.inventory", true)) {
    // nothing is locking the inventory
}
```

### Through Locki's API

```java
public static final InventoryLock MY_LOCK = Locki.registerLock("mymod", "test_feature");


```

## Setup

For setup instructions please see the [fabric wiki page](https://fabricmc.net/wiki/tutorial:setup) that relates to the IDE that you are using.

