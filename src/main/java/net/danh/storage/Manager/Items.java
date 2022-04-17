package net.danh.storage.Manager;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import preponderous.ponder.minecraft.bukkit.nms.NMSAssistant;

import java.util.Objects;

import static net.danh.storage.Manager.Data.*;
import static net.danh.storage.Manager.Files.*;
import static net.danh.storage.Storage.economy;

public class Items {

    private static int price;
    private static String block;

    public static void RemoveItems(Player p, String name, Integer amount) {
        if (Objects.requireNonNull(getconfigfile().getConfigurationSection("Blocks.")).getKeys(false).contains(name)) {
            if (getStorage(p, name) >= amount) {
                for (String getBlockType : Objects.requireNonNull(getconfigfile().getConfigurationSection("Blocks.")).getKeys(false)) {
                    if (name.equalsIgnoreCase(getBlockType)) {
                        block = getconfigfile().getString("Blocks." + name + ".Name");
                        break;
                    }
                }
                removeStorage(p, name, amount);
                if (Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TYPE")).equalsIgnoreCase("ACTION_BAR")
                        || Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TYPE")).equalsIgnoreCase("CHAT")) {
                    if (getconfigfile().getBoolean("Message.TAKE.STATUS")) {
                        p.spigot().sendMessage(ChatMessageType.valueOf(getconfigfile().getString("Message.TAKE.TYPE")),
                                new TranslatableComponent(colorize(Objects.requireNonNull(getlanguagefile().getString("Take_Item"))
                                        .replaceAll("%item%", block.replaceAll("_", " "))
                                        .replaceAll("%amount%", String.valueOf(amount))
                                        .replaceAll("%storage%", String.format("%,d", getStorage(p, name)))
                                        .replaceAll("%max%", String.format("%,d", getMaxStorage(p, name))))));
                    }
                } else {
                    if (Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TYPE")).equalsIgnoreCase("TITLE")) {
                        p.sendTitle(colorize(Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TITLE.TITLE"))
                                .replaceAll("%item%", block.replaceAll("_", " ")
                                        .replaceAll("-", " "))
                                .replaceAll("%amount%", String.valueOf(amount))
                                .replaceAll("%storage%", String.format("%,d", getStorage(p, name))))
                                .replaceAll("%max%", String.format("%,d", getMaxStorage(p, name))), colorize(Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TITLE.SUBTITLE"))
                                .replaceAll("%item%", block.replaceAll("_", " ")
                                        .replaceAll("-", " "))
                                .replaceAll("%amount%", String.valueOf(amount))
                                .replaceAll("%storage%", String.format("%,d", getStorage(p, name)))
                                .replaceAll("%max%", String.format("%,d", getMaxStorage(p, name)))), getconfigfile().getInt("Message.TAKE.TITLE.FADEIN"), getconfigfile().getInt("Message.TAKE.TITLE.STAY"), getconfigfile().getInt("Message.TAKE.TITLE.FADEOUT"));
                    }
                }
                if (autoSmelt(p)) {
                    if (Objects.requireNonNull(getconfigfile().getConfigurationSection("Blocks." + name)).getKeys(false).contains("Convert")) {
                        name = getconfigfile().getString("Blocks." + name + ".Convert");
                    }
                }
                NMSAssistant nmsAssistant = new NMSAssistant();
                if (nmsAssistant.isVersionLessThan(13)) {
                    String[] data = Objects.requireNonNull(name).split(";");
                    String material = data[0];
                    if (data.length == 1) {
                        ItemStack items = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(material))), amount);
                        p.getInventory().addItem(items);
                    } else {
                        short damaged = Short.parseShort(data[1]);
                        ItemStack items = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(material))), amount, damaged);
                        p.getInventory().addItem(items);
                    }
                } else {
                    ItemStack items = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(name))), amount);
                    p.getInventory().addItem(items);
                }
            } else {
                p.sendMessage(colorize(getlanguagefile().getString("Not_Enough")));
            }
        } else {
            p.sendMessage(colorize(getlanguagefile().getString("Not_Correct_Item")));
        }
    }

    public static String getName(@NotNull String name) {
        return getconfigfile().getString("Blocks." + name.toUpperCase() + ".Name");
    }

    public static void SellItems(Player p, String name, Integer amount) {
        if (getStorage(p, name) >= amount) {
            for (String getBlockType : Objects.requireNonNull(getconfigfile().getConfigurationSection("Blocks.")).getKeys(false)) {
                if (name.equalsIgnoreCase(getBlockType)) {
                    price = getconfigfile().getInt("Blocks." + name + ".Price");
                    block = getconfigfile().getString("Blocks." + name + ".Name");
                    break;
                }
            }
            removeStorage(p, name, amount);
            int money = price * amount;
            EconomyResponse r = economy.depositPlayer(p, money);
            if (r.transactionSuccess()) {
                if (Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TYPE")).equalsIgnoreCase("ACTION_BAR")
                        || Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TYPE")).equalsIgnoreCase("CHAT")) {
                    p.spigot().sendMessage(ChatMessageType.valueOf(getconfigfile().getString("Message.TAKE")),
                            new TranslatableComponent(colorize(Objects.requireNonNull(getlanguagefile().getString("Sell"))
                                    .replaceAll("%money%", String.valueOf(money))
                                    .replaceAll("%item%", block.replaceAll("_", " "))
                                    .replaceAll("%storage%", String.format("%,d", getStorage(p, name)))
                                    .replaceAll("%max%", String.format("%,d", getMaxStorage(p, name))))));
                } else {
                    if (Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TYPE")).equalsIgnoreCase("TITLE")) {
                        p.sendTitle(colorize(Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TITLE.TITLE"))
                                .replaceAll("%item%", block.replaceAll("_", " ")
                                        .replaceAll("-", " "))
                                .replaceAll("%amount%", String.valueOf(amount))
                                .replaceAll("%storage%", String.format("%,d", getStorage(p, name))))
                                .replaceAll("%max%", String.format("%,d", getMaxStorage(p, name))), colorize(Objects.requireNonNull(getconfigfile().getString("Message.TAKE.TITLE.SUBTITLE"))
                                .replaceAll("%item%", block.replaceAll("_", " ")
                                        .replaceAll("-", " "))
                                .replaceAll("%amount%", String.valueOf(amount))
                                .replaceAll("%storage%", String.format("%,d", getStorage(p, name)))
                                .replaceAll("%max%", String.format("%,d", getMaxStorage(p, name)))), getconfigfile().getInt("Message.TAKE.TITLE.FADEIN"), getconfigfile().getInt("Message.TAKE.TITLE.STAY"), getconfigfile().getInt("Message.TAKE.TITLE.FADEOUT"));
                    }
                }
            } else {
                p.sendMessage(colorize("&cError!"));
            }
        } else {
            p.sendMessage(colorize(getlanguagefile().getString("Not_Enough")));
        }
    }

    public static int getPrice(String m) {
        return getconfigfile().getInt("Blocks." + m + ".Price");
    }
}
