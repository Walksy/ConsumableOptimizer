<h1>Consumable Optimizer!</h1>

This mod enforces more client-sided processing when handling consumable items (any food item).

---
**Information**

By default, Minecraft requires the user to wait for a server response before consuming an item. While this delay is hardly noticeable on low ping, it becomes much more noticable on high ping and causes players to be stuck in an eating animation until the server eventually responds.
This optimizer ensures all of that processing is done client-side rather than server-side, allowing items to be comsumed with no delay and on the exact tick it should be finished. The server still applies the item's effects (such as absorption or regeneration from a golden apple) while the client handles the actual eating process.

High ping can also occationaly causes desyncs when consuming an item, forcing the server to restart the eating process for the player. This further slows the overall process of consuming an item down even more and makes it feel inconsistent. This optimizer fixes this, by cancelling these recalls from the server when unneeded.

**Demonstration Video**

https://youtu.be/1eVq6nrUfqs

---

## Server Opt-Out

On servers using popular anti-cheats, such as Grim or Vulkan, it's unlikely for Consumable Optimizer to flag. However, if your server has reports of Consumable Optimizer causing flags or you don't want your player base using the mod, you can opt out using a very simple plugin. Some example code can be found below:

```java
public class ConsumableOptimizerDisable extends JavaPlugin implements PluginMessageListener {

    private static final String S2C_CHANNEL = "consumable_optimizer:disable_payload";
    private static final String C2S_CHANNEL = "consumable_optimizer:handshake_payload";

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, S2C_CHANNEL);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, C2S_CHANNEL, this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals(C2S_CHANNEL)) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) {
                    player.sendPluginMessage(this, S2C_CHANNEL, new byte[0]);
                    getLogger().info("Disabled consumable optimizer for " + player.getName());
                }
            }, 20L);
        }
    }
}
```

---
**Credits**
- Mod: Walksy
- Icon: [SakuraFX](https://www.youtube.com/@everythingsakura)

