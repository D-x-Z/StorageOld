package net.danh.storage.Commands;

import net.danh.dcore.Commands.CMDBase;
import net.danh.storage.Gui.OpenGui;
import net.danh.storage.Manager.Files;
import net.danh.storage.Manager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.danh.dcore.Utils.Chat.colorize;
import static net.danh.dcore.Utils.Player.sendConsoleMessage;
import static net.danh.dcore.Utils.Player.sendPlayerMessage;
import static net.danh.storage.Manager.Data.*;
import static net.danh.storage.Manager.Files.*;
import static net.danh.storage.Manager.Items.*;

public class Storage extends CMDBase {
    public Storage(JavaPlugin core) {
        super(core, "Storage");
    }

    @Override
    public void playerexecute(Player p, String[] args) {
        if (args.length == 0) {
            p.openInventory(OpenGui.Open(p));
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                sendPlayerMessage(p, getlanguagefile().getStringList("User.Help_User"));
                if (p.hasPermission("Storage.admin")) {
                    sendPlayerMessage(p, getlanguagefile().getStringList("Admin.Help_Admin"));
                }
            }
            if (p.hasPermission("Storage.admin")) {
                if (args[0].equalsIgnoreCase("convert")) {
                    if (getRawDataFile().exists()) {
                        try {
                            for (String name : Files.getdatafile().getConfigurationSection("players").getKeys(false)) {
                                PlayerData playerData = new PlayerData(name);
                                playerData.load();
                                if (playerData.getConfig().getKeys(true).size() == 0) {
                                    playerData.getConfig().set("players." + name + ".auto.Smelt", getdatafile().getBoolean("players." + name + ".auto.Smelt"));
                                    playerData.getConfig().set("players." + name + ".auto.Pick", getdatafile().getBoolean("players." + name + ".auto.Pick"));
                                    for (String item : Objects.requireNonNull(getconfigfile().getConfigurationSection("Blocks")).getKeys(false)) {
                                        playerData.getConfig().set("players." + name + ".items." + item + ".max", getdatafile().getInt("players." + name + ".items." + item + ".max"));
                                        playerData.getConfig().set("players." + name + ".items." + item + ".amount", getdatafile().getInt("players." + name + ".items." + item + ".amount"));
                                    }
                                    playerData.save();
                                }
                            }
                            getRawDataFile().delete();
                            sendPlayerMessage(p, getlanguagefile().getString("Convert.Done"));
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("[Storage] The data.yml file is not a data file");
                        }
                    } else {
                        sendPlayerMessage(p, getlanguagefile().getString("Convert.Converted"));
                    }
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    reloadfiles();
                    sendPlayerMessage(p, getlanguagefile().getString("Admin.Reload"));
                }
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("sell")) {
                if (isInt(args[2])) {
                    try {
                        if (Integer.parseInt(args[2]) > 0) {
                            SellItems(p, args[1], Integer.parseInt(args[2]));
                        } else {
                            sendPlayerMessage(p, getlanguagefile().getString("Invaild_Number"));
                        }
                    } catch (Exception e) {
                        sendPlayerMessage(p, getlanguagefile().getString("Number_To_Big"));
                    }
                } else {
                    if (getStorage(Objects.requireNonNull(p), args[1]) > 0) {
                        if (args[2].equalsIgnoreCase("all")) {
                            SellItems(p, args[1], getStorage(Objects.requireNonNull(p), args[1]));
                        } else {
                            sendPlayerMessage(p, getlanguagefile().getString("Invaild_Number"));
                        }
                    } else {
                        sendPlayerMessage(p, getlanguagefile().getString("User.Not_Have_Any_Item"));
                    }
                }
            }
            if (args[0].equalsIgnoreCase("take")) {
                if (isInt(args[2])) {
                    try {
                        if (Integer.parseInt(args[2]) > 0) {
                            if (Integer.parseInt(args[2]) <= getAmountEmpty(Objects.requireNonNull(p), args[1])) {
                                RemoveItems(p, args[1], Integer.parseInt(args[2]));
                            } else {
                                sendPlayerMessage(p, getlanguagefile().getString("User.Not_Enough_Inventory")
                                        .replaceAll("%space%", String.valueOf(getAmountEmpty(Objects.requireNonNull(p), args[1]))));
                            }
                        } else {
                            sendPlayerMessage(p, getlanguagefile().getString("Invaild_Number"));
                        }
                    } catch (Exception e) {
                        sendPlayerMessage(p, getlanguagefile().getString("Number_To_Big"));
                    }
                } else {
                    if (getStorage(Objects.requireNonNull(p), args[1]) > 0) {
                        if (args[2].equalsIgnoreCase("all")) {
                            RemoveItems(p, args[1], Math.min(getStorage(Objects.requireNonNull(p), args[1]), getAmountEmpty(Objects.requireNonNull(p), args[1])));
                        } else {
                            sendPlayerMessage(p, getlanguagefile().getString("Invaild_Number"));
                        }
                    } else {
                        sendPlayerMessage(p, getlanguagefile().getString("User.Not_Have_Any_Item"));
                    }
                }
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (isInt(args[2])) {
                    try {
                        if (Integer.parseInt(args[2]) > 0) {
                            AddItems((p), args[1].toUpperCase(), Integer.parseInt(args[2]));
                        } else {
                            sendPlayerMessage(p, getlanguagefile().getString("Invaild_Number"));
                        }
                    } catch (Exception e) {
                        sendPlayerMessage(p, getlanguagefile().getString("Number_To_Big"));
                    }
                } else {
                    if (getAmountItem((Objects.requireNonNull(p)), args[1]) > 0) {
                        if (args[2].equalsIgnoreCase("all")) {
                            if (getMaxStorage(Objects.requireNonNull(p), args[1]) >= getStorage(Objects.requireNonNull(p), args[1]) + getAmountItem((Objects.requireNonNull(p)), args[1])) {
                                AddItems((p), args[1], getAmountItem((Objects.requireNonNull(p)), args[1]));
                            } else {
                                if (getMaxStorage(Objects.requireNonNull(p), args[1]) - getStorage(Objects.requireNonNull(p), args[1]) > 0) {
                                    AddItems((p), args[1], getMaxStorage(Objects.requireNonNull(p), args[1]) - getStorage(Objects.requireNonNull(p), args[1]));
                                } else {
                                    p.sendMessage(colorize(getlanguagefile().getString("User.Add_Full_Storage")));
                                }
                            }
                        } else {
                            sendPlayerMessage(p, getlanguagefile().getString("Invaild_Number"));
                        }
                    } else {
                        sendPlayerMessage(p, getlanguagefile().getString("User.Not_Have_Any_Item"));
                    }
                }
            }
        }
        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("storage")) {
                Set<String> items = getconfigfile().getConfigurationSection("Blocks.").getKeys(false);
                if (items.contains(args[3].toUpperCase())) {
                    if (Bukkit.getPlayer(args[2]) != null) {
                        if (Integer.parseInt(args[4]) > 0) {
                            if (p.hasPermission("Storage.admin")) {
                                if (args[1].equalsIgnoreCase("set")) {
                                    if (getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) == 0) {
                                        setMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Files.getconfigfile().getInt("Default_Max_Storage"));
                                    }
                                    if (Integer.parseInt(args[4]) <= getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])) {
                                        setStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                        sendPlayerMessage(p, Objects.requireNonNull(getlanguagefile().getString("Admin.Storage_Set")
                                                .replaceAll("%player%", args[2])
                                                .replaceAll("%item%", getName(args[3]))
                                                .replaceAll("%amount%", args[4])));
                                    } else {
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.Storage_Set_Errol")
                                                .replaceAll("%player%", args[2])
                                                .replaceAll("%max%", String.valueOf(getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]))));
                                    }
                                }
                                if (args[1].equalsIgnoreCase("add")) {
                                    if (getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) == 0) {
                                        setMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Files.getconfigfile().getInt("Default_Max_Storage"));
                                    }
                                    if (getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) + Integer.parseInt(args[4]) <= getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])) {
                                        addStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.Storage_Add")
                                                .replaceAll("%player%", args[2])
                                                .replaceAll("%item%", getName(args[3]))
                                                .replaceAll("%amount%", args[4]));
                                    } else {
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.Storage_Add_Errol")
                                                .replaceAll("%amount%", String.valueOf(getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) - getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])))
                                                .replaceAll("%player%", args[2]));
                                    }
                                }
                                if (args[1].equalsIgnoreCase("remove")) {
                                    if (getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) == 0) {
                                        setMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Files.getconfigfile().getInt("Default_Max_Storage"));
                                    }
                                    if (getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) - Integer.parseInt(args[4]) >= 0) {
                                        removeStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.Storage_Remove")
                                                .replaceAll("%player%", args[2])
                                                .replaceAll("%item%", getName(args[3]))
                                                .replaceAll("%amount%", args[4]));
                                    } else {
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.Storage_Remove_Errol")
                                                .replaceAll("%amount%", String.valueOf(getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])))
                                                .replaceAll("%player%", args[2]));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (args[0].equalsIgnoreCase("max_storage")) {
                Set<String> items = getconfigfile().getConfigurationSection("Blocks.").getKeys(false);
                if (items.contains(args[3].toUpperCase())) {
                    if (Bukkit.getPlayer(args[2]) != null) {
                        if (Integer.parseInt(args[4]) > 0) {
                            if (p.hasPermission("Storage.admin")) {
                                if (args[1].equalsIgnoreCase("set")) {
                                    if (Integer.parseInt(args[4]) >= getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) && Integer.parseInt(args[4]) >= getconfigfile().getInt("Default_Max_Storage")) {
                                        setMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.MaxStorage_Set")
                                                .replaceAll("%player%", args[2])
                                                .replaceAll("%item%", getName(args[3]))
                                                .replaceAll("%number%", args[4]));
                                    } else if (Integer.parseInt(args[4]) >= getconfigfile().getInt("Default_Max_Storage")) {
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.MaxStorage_Set_Errol_1")
                                                .replaceAll("%player%", args[2])
                                                .replaceAll("%storage%", String.valueOf(getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]))));
                                    }
                                    if (Integer.parseInt(args[4]) < getconfigfile().getInt("Default_Max_Storage")) {
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.MaxStorage_Set_Errol_1")
                                                .replaceAll("%max_default%", String.valueOf(getconfigfile().getInt("Default_Max_Storage"))));
                                    }
                                }
                                if (args[1].equalsIgnoreCase("add")) {
                                    addMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                    sendPlayerMessage(p, getlanguagefile().getString("Admin.MaxStorage_Add")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%item%", getName(args[3]))
                                            .replaceAll("%number%", args[4]));
                                }
                                if (args[1].equalsIgnoreCase("remove")) {
                                    int remove = getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) - Integer.parseInt(args[4]);
                                    int number = getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) - getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]);
                                    if (remove >= getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])) {
                                        removeMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.MaxStorage_Remove")
                                                .replaceAll("%player%", args[2])
                                                .replaceAll("%item%", getName(args[3]))
                                                .replaceAll("%number%", args[4]));
                                    } else if (number > 0) {
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.MaxStorage_Remove_Errol_1")
                                                .replaceAll("%player%", args[2])
                                                .replaceAll("%number%", String.valueOf(number)));
                                    } else {
                                        sendPlayerMessage(p, getlanguagefile().getString("Admin.MaxStorage_Remove_Errol_2")
                                                .replaceAll("%player%", args[2]));
                                    }
                                }
                            }
                        } else {
                            sendPlayerMessage(p, getlanguagefile().getString("Invaild_Number"));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void consoleexecute(ConsoleCommandSender c, String[] args) {
        if (args.length == 2) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                sendConsoleMessage(c, "Player " + args[1] + " is null");
                return;
            }
            if (args[1].equalsIgnoreCase("open")) {
                p.openInventory(OpenGui.Open(p));
            }
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                sendConsoleMessage(c, getlanguagefile().getStringList("User.Help_User"));
                sendConsoleMessage(c, getlanguagefile().getStringList("User.Help_Admin"));
            }
            if (args[0].equalsIgnoreCase("convert")) {
                if (getRawDataFile().exists()) {
                    try {
                        for (String name : Files.getdatafile().getConfigurationSection("players").getKeys(false)) {
                            PlayerData playerData = new PlayerData(name);
                            playerData.load();
                            if (playerData.getConfig().getKeys(true).size() == 0) {
                                playerData.getConfig().set("players." + name + ".auto.Smelt", getdatafile().getBoolean("players." + name + ".auto.Smelt"));
                                playerData.getConfig().set("players." + name + ".auto.Pick", getdatafile().getBoolean("players." + name + ".auto.Pick"));
                                for (String item : Objects.requireNonNull(getconfigfile().getConfigurationSection("Blocks")).getKeys(false)) {
                                    playerData.getConfig().set("players." + name + ".items." + item + ".max", getdatafile().getInt("players." + name + ".items." + item + ".max"));
                                    playerData.getConfig().set("players." + name + ".items." + item + ".amount", getdatafile().getInt("players." + name + ".items." + item + ".amount"));
                                }
                                playerData.save();
                            }
                        }
                        getRawDataFile().delete();
                        sendConsoleMessage(c, getlanguagefile().getString("Convert.Done"));
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("[Storage] The data.yml file is not a data file");
                    }
                } else {
                    sendConsoleMessage(c, getlanguagefile().getString("Convert.Converted"));
                }
            }
            if (args[0].equalsIgnoreCase("reload")) {
                reloadfiles();
                sendConsoleMessage(c, getlanguagefile().getString("Admin.Reload"));
            }
        }
        if (args.length == 4) {
            Player p = Bukkit.getPlayer(args[3]);
            if (p == null) {
                sendConsoleMessage(c, args[0] + " is null");
                return;
            }
            if (args[0].equalsIgnoreCase("sell")) {
                if (isInt(args[2])) {
                    try {
                        if (Integer.parseInt(args[2]) > 0) {
                            SellItems(p, args[1], Integer.parseInt(args[2]));
                        } else {
                            sendConsoleMessage(c, getlanguagefile().getString("Invaild_Number"));
                        }
                    } catch (Exception e) {
                        sendConsoleMessage(c, getlanguagefile().getString("Number_To_Big"));
                    }
                } else {
                    if (getStorage(Objects.requireNonNull(p), args[1]) > 0) {
                        if (args[2].equalsIgnoreCase("all")) {
                            SellItems(p, args[1], getStorage(Objects.requireNonNull(p), args[1]));
                        } else {
                            sendConsoleMessage(c, getlanguagefile().getString("Invaild_Number"));
                        }
                    } else {
                        sendConsoleMessage(c, getlanguagefile().getString("User.Not_Have_Any_Item"));
                    }
                }
            }
            if (args[0].equalsIgnoreCase("take")) {
                if (isInt(args[2])) {
                    try {
                        if (Integer.parseInt(args[2]) > 0) {
                            if (Integer.parseInt(args[2]) <= getAmountEmpty(Objects.requireNonNull(p), args[1])) {
                                RemoveItems(p, args[1], Integer.parseInt(args[2]));
                            } else {
                                sendConsoleMessage(c, getlanguagefile().getString("User.Not_Enough_Inventory")
                                        .replaceAll("%space%", String.valueOf(getAmountEmpty(Objects.requireNonNull(p), args[1]))));
                            }
                        } else {
                            sendConsoleMessage(c, getlanguagefile().getString("Invaild_Number"));
                        }
                    } catch (Exception e) {
                        sendConsoleMessage(c, getlanguagefile().getString("Number_To_Big"));
                    }
                } else {
                    if (getStorage(Objects.requireNonNull(p), args[1]) > 0) {
                        if (args[2].equalsIgnoreCase("all")) {
                            RemoveItems(p, args[1], Math.min(getStorage(Objects.requireNonNull(p), args[1]), getAmountEmpty(Objects.requireNonNull(p), args[1])));
                        } else {
                            sendConsoleMessage(c, getlanguagefile().getString("Invaild_Number"));
                        }
                    } else {
                        sendConsoleMessage(c, getlanguagefile().getString("User.Not_Have_Any_Item"));
                    }
                }
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (isInt(args[2])) {
                    try {
                        if (Integer.parseInt(args[2]) > 0) {
                            AddItems((p), args[1], Integer.parseInt(args[2]));
                        } else {
                            sendConsoleMessage(c, getlanguagefile().getString("Invaild_Number"));
                        }
                    } catch (Exception e) {
                        sendConsoleMessage(c, getlanguagefile().getString("Number_To_Big"));
                    }
                } else {
                    if (getAmountItem((Objects.requireNonNull(p)), args[1]) > 0) {
                        if (args[2].equalsIgnoreCase("all")) {
                            if (getMaxStorage(Objects.requireNonNull(p), args[1]) >= getStorage(Objects.requireNonNull(p), args[1]) + getAmountItem((Objects.requireNonNull(p)), args[1])) {
                                AddItems((p), args[1], getAmountItem((Objects.requireNonNull(p)), args[1]));
                            } else {
                                if (getMaxStorage(Objects.requireNonNull(p), args[1]) - getStorage(Objects.requireNonNull(p), args[1]) > 0) {
                                    AddItems((p), args[1], getMaxStorage(Objects.requireNonNull(p), args[1]) - getStorage(Objects.requireNonNull(p), args[1]));
                                } else {
                                    sendPlayerMessage(p, getlanguagefile().getString("User.Add_Full_Storage"));
                                }
                            }
                        } else {
                            sendConsoleMessage(c, getlanguagefile().getString("Invaild_Number"));
                        }
                    } else {
                        sendConsoleMessage(c, getlanguagefile().getString("User.Not_Have_Any_Item"));
                    }
                }
            }
        }
        if (args.length == 6) {
            Player p = Bukkit.getPlayer(args[5]);
            if (p == null) {
                sendConsoleMessage(c, args[0] + " is null");
                return;
            }
            if (args[0].equalsIgnoreCase("storage")) {
                Set<String> items = getconfigfile().getConfigurationSection("Blocks.").getKeys(false);
                if (items.contains(args[3].toUpperCase())) {
                    if (Bukkit.getPlayer(args[2]) != null) {
                        if (Integer.parseInt(args[4]) > 0) {
                            if (args[1].equalsIgnoreCase("set")) {
                                if (getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) == 0) {
                                    setMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Files.getconfigfile().getInt("Default_Max_Storage"));
                                }
                                if (Integer.parseInt(args[4]) <= getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])) {
                                    setStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                    sendConsoleMessage(c, Objects.requireNonNull(getlanguagefile().getString("Admin.Storage_Set")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%item%", getName(args[3]))
                                            .replaceAll("%amount%", args[4])));
                                } else {
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.Storage_Set_Errol")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%max%", String.valueOf(getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]))));
                                }
                            }
                            if (args[1].equalsIgnoreCase("add")) {
                                if (getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) == 0) {
                                    setMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Files.getconfigfile().getInt("Default_Max_Storage"));
                                }
                                if (getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) + Integer.parseInt(args[4]) <= getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])) {
                                    addStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.Storage_Add")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%item%", getName(args[3]))
                                            .replaceAll("%amount%", args[4]));
                                } else {
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.Storage_Add_Errol")
                                            .replaceAll("%amount%", String.valueOf(getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) - getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])))
                                            .replaceAll("%player%", args[2]));
                                }
                            }
                            if (args[1].equalsIgnoreCase("remove")) {
                                if (getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) == 0) {
                                    setMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Files.getconfigfile().getInt("Default_Max_Storage"));
                                }
                                if (getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) - Integer.parseInt(args[4]) >= 0) {
                                    removeStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.Storage_Remove")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%item%", getName(args[3]))
                                            .replaceAll("%amount%", args[4]));
                                } else {
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.Storage_Remove_Errol")
                                            .replaceAll("%amount%", String.valueOf(getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])))
                                            .replaceAll("%player%", args[2]));
                                }
                            }
                        }
                    }
                }
            }
            if (args[0].equalsIgnoreCase("max_storage")) {
                Set<String> items = getconfigfile().getConfigurationSection("Blocks.").getKeys(false);
                if (items.contains(args[3].toUpperCase())) {
                    if (Bukkit.getPlayer(args[2]) != null) {
                        if (Integer.parseInt(args[4]) > 0) {
                            if (args[1].equalsIgnoreCase("set")) {
                                if (Integer.parseInt(args[4]) >= getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) && Integer.parseInt(args[4]) >= getconfigfile().getInt("Default_Max_Storage")) {
                                    setMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.MaxStorage_Set")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%item%", getName(args[3]))
                                            .replaceAll("%number%", args[4]));
                                } else if (Integer.parseInt(args[4]) >= getconfigfile().getInt("Default_Max_Storage")) {
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.MaxStorage_Set_Errol_1")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%storage%", String.valueOf(getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]))));
                                }
                                if (Integer.parseInt(args[4]) < getconfigfile().getInt("Default_Max_Storage")) {
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.MaxStorage_Set_Errol_1")
                                            .replaceAll("%max_default%", String.valueOf(getconfigfile().getInt("Default_Max_Storage"))));
                                }
                            }
                            if (args[1].equalsIgnoreCase("add")) {
                                addMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                sendConsoleMessage(c, getlanguagefile().getString("Admin.MaxStorage_Add")
                                        .replaceAll("%player%", args[2])
                                        .replaceAll("%item%", getName(args[3]))
                                        .replaceAll("%number%", args[4]));
                            }
                            if (args[1].equalsIgnoreCase("remove")) {
                                int remove = getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) - Integer.parseInt(args[4]);
                                int number = getMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]) - getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3]);
                                if (remove >= getStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3])) {
                                    removeMaxStorage(Objects.requireNonNull(Bukkit.getPlayer(args[2])), args[3], Integer.parseInt(args[4]));
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.MaxStorage_Remove")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%item%", getName(args[3]))
                                            .replaceAll("%number%", args[4]));
                                } else if (number > 0) {
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.MaxStorage_Remove_Errol_1")
                                            .replaceAll("%player%", args[2])
                                            .replaceAll("%number%", String.valueOf(number)));
                                } else {
                                    sendConsoleMessage(c, getlanguagefile().getString("Admin.MaxStorage_Remove_Errol_2")
                                            .replaceAll("%player%", args[2]));
                                }
                            }
                        }
                    } else {
                        sendConsoleMessage(c, getlanguagefile().getString("Invaild_Number"));
                    }
                }
            }
        }
    }

    @Override
    public List<String> TabComplete(CommandSender commandSender, String[] strings) {
        return null;
    }
}
