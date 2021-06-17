package net.kunmc.lab.chasedbylava.task;

import net.kunmc.lab.chasedbylava.ChasedByLava;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

public class GameTask extends BukkitRunnable {
    private final UUID uuid;
    private Material material;
    private final ArrayBlockingQueue<Block> queue = new ArrayBlockingQueue<>(2, true);

    public GameTask(UUID uuid, Material material) {
        this.uuid = uuid;
        this.material = material;
    }

    @Override
    public void run() {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }

        if (p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }

        Block block = p.getLocation().add(0, 1, 0).getBlock();

        if (queue.contains(block)) {
            return;
        }

        if (queue.size() < 2) {
            queue.add(block);
        }

        Location lastLoc = queue.peek().getLocation();
        if (block.getLocation().distance(lastLoc) >= 2) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    lastLoc.getBlock().setType(material);
                }
            }.runTask(ChasedByLava.getInstance());

            queue.poll();
            queue.add(block);
        }
    }

    public Material getMaterial() {
        return this.material;
    }

    public void changeMaterial(Material material) {
        this.material = material;
    }
}
