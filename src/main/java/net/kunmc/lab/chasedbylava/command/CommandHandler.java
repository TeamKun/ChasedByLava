package net.kunmc.lab.chasedbylava.command;

import net.kunmc.lab.chasedbylava.ChasedByLava;
import net.kunmc.lab.chasedbylava.task.GameTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements TabExecutor {
    private final Map<UUID, GameTask> uuidBukkitTaskMap = new HashMap<>();
    private Mode mode = Mode.Stop;
    private Material normalModeMaterial;
    private boolean isEnabled;

    public CommandHandler() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), ChasedByLava.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return false;
        }

        switch (args[0]) {
            case "start":
                if (isEnabled) {
                    sender.sendMessage(ChatColor.RED + "ChasedByLavaはすでに実行されています.");
                    break;
                }

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "usage:\n" +
                            "/chasedbylava start normal <lava|water>\n" +
                            "/chasedbylava start shuffle");
                    break;
                }

                switch (args[1]) {
                    case "normal": {
                        if (args.length < 3) {
                            sender.sendMessage(ChatColor.RED + "usage: /chasedbylava start normal <lava|water>");
                            break;
                        }

                        if (args[2].equalsIgnoreCase("lava")) {
                            normalModeMaterial = Material.LAVA;
                        } else if (args[2].equalsIgnoreCase("water")) {
                            normalModeMaterial = Material.WATER;
                        } else {
                            sender.sendMessage(ChatColor.RED + args[2] + "は不正な値です.lavaもしくはwaterを指定してください.");
                            break;
                        }

                        Bukkit.getOnlinePlayers().forEach(p -> {
                            GameTask task = new GameTask(p.getUniqueId(), normalModeMaterial);
                            uuidBukkitTaskMap.put(p.getUniqueId(), task);
                            task.runTaskTimerAsynchronously(ChasedByLava.getInstance(), 0, 1);
                        });
                        isEnabled = true;
                        mode = Mode.Normal;

                        sender.sendMessage(ChatColor.GREEN + "ChasedByLavaがNormalモードで有効化されました.");
                        break;
                    }
                    case "shuffle": {
                        Bukkit.getOnlinePlayers().forEach(p -> {
                            Material material = new Random().nextInt(2) == 0 ? Material.WATER : Material.LAVA;
                            GameTask task = new GameTask(p.getUniqueId(), material);
                            uuidBukkitTaskMap.put(p.getUniqueId(), task);
                            task.runTaskTimerAsynchronously(ChasedByLava.getInstance(), 0, 1);
                        });
                        isEnabled = true;
                        mode = Mode.Shuffle;

                        sender.sendMessage(ChatColor.GREEN + "ChasedByLavaがShuffleモードで有効化されました.");
                        break;
                    }
                    default:
                        sender.sendMessage(ChatColor.RED + "不明なコマンドです.");
                }
                break;
            case "stop":
                if (!isEnabled) {
                    sender.sendMessage(ChatColor.RED + "ChasedByLavaは実行されていません.");
                    break;
                }

                uuidBukkitTaskMap.values().forEach(BukkitRunnable::cancel);
                uuidBukkitTaskMap.clear();
                isEnabled = false;
                mode = Mode.Stop;

                sender.sendMessage(ChatColor.GREEN + "ChasedByLavaを停止しました.");
                break;
            case "change":
                if (!isEnabled) {
                    sender.sendMessage(ChatColor.RED + "ChasedByLavaは実行されていません.");
                    break;
                }


                List<Player> playerList = selectPlayers(sender, args[1]);
                if (playerList.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりませんでした.");
                    break;
                }

                Material material;
                if (args[2].equalsIgnoreCase("lava")) {
                    material = Material.LAVA;
                } else if (args[2].equalsIgnoreCase("water")) {
                    material = Material.WATER;
                } else {
                    sender.sendMessage(ChatColor.RED + args[2] + "は不正な値です.lavaもしくはwaterを指定してください.");
                    break;
                }

                playerList.forEach(p -> {
                    GameTask task = uuidBukkitTaskMap.get(p.getUniqueId());
                    if (task == null) {
                        return;
                    }

                    task.changeMaterial(material);
                });

                sender.sendMessage(ChatColor.GREEN + (playerList.size() + "人のプレイヤーの追ってくる流体を" + material + "に変更しました."));
                break;
            default:
                sender.sendMessage(ChatColor.RED + "不明なコマンドです.");
        }

        return true;
    }

    private List<Player> selectPlayers(CommandSender sender, String selector) {
        try {
            return Bukkit.selectEntities(sender, selector).stream()
                    .filter(x -> x instanceof Player)
                    .map(x -> ((Player) x))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("start", "stop", "change").filter(x -> x.startsWith(args[0])).collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0]) {
                case "start":
                    return Stream.of("normal", "shuffle").filter(x -> x.startsWith(args[1])).collect(Collectors.toList());
                case "change":
                    return Stream.concat(Bukkit.getOnlinePlayers().stream().map(Player::getName), Stream.of("@a", "@r", "@p"))
                            .filter(x -> x.startsWith(args[1]))
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            if (args[0].equals("change") || args[1].equals("normal")) {
                return Stream.of("lava", "water").filter(x -> x.startsWith(args[2])).collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private class PlayerJoinListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            UUID uuid = e.getPlayer().getUniqueId();
            switch (mode) {
                case Normal:
                    if (!uuidBukkitTaskMap.containsKey(uuid)) {
                        GameTask task = new GameTask(uuid, normalModeMaterial);
                        uuidBukkitTaskMap.put(uuid, task);
                        task.runTaskTimerAsynchronously(ChasedByLava.getInstance(), 0, 1);
                    }
                case Shuffle:
                    if (!uuidBukkitTaskMap.containsKey(uuid)) {
                        Material material = new Random().nextInt(2) == 0 ? Material.WATER : Material.LAVA;
                        GameTask task = new GameTask(uuid, material);
                        uuidBukkitTaskMap.put(uuid, task);
                        task.runTaskTimerAsynchronously(ChasedByLava.getInstance(), 0, 1);
                    }
                case Stop:
            }
        }
    }

    private enum Mode {
        Normal,
        Shuffle,
        Stop
    }
}
