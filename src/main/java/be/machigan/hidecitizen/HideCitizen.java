package be.machigan.hidecitizen;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HideCitizen extends JavaPlugin {
    private static HideCitizen instance;
    public static final Map<Integer, UUID> ENTITY_ID_UUID_TRANSLATION = new HashMap<>();
    private static ProtocolManager protocolManager;
    public static final String USE_PERMISSION = "hidecitizen.hide";
    private static LuckPerms luckPerms;

    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.removePacketListeners(this);
        protocolManager.addPacketListener(new PacketCitizen());
        Bukkit.getPluginManager().registerEvents(new OnNPCSpawnEvent(), this);
        this.setCommand();
        this.startFillFromNPCRegistryScheduler();
        luckPerms = Optional
                .ofNullable(getServer().getServicesManager().getRegistration(LuckPerms.class))
                .orElseThrow(() -> new IllegalStateException("LuckPerm"))
                .getProvider();
    }

    private void setCommand() {
        PluginCommand command = this.getCommand("hide-citizen");
        if (command == null) return;
        command.setExecutor(new HideCitizenCommand());;
        command.setTabCompleter(new HideCitizenTabCompleter());
    }

    /**
     * The registry of NPC is not filled synchronously, so we fetch it until it's filled or to many times has passed
     * (no NPC, it can happen)
     */
    private void startFillFromNPCRegistryScheduler() {
        AtomicInteger passedTimes = new AtomicInteger();
        final int[] taskID = new int[] { -1 };
        taskID[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            passedTimes.addAndGet(20);
            CitizensAPI.getNPCRegistry()
                    .forEach(npc -> {
                        Entity npcEntity = npc.getEntity();
                        if (npcEntity != null)
                            ENTITY_ID_UUID_TRANSLATION.put(npcEntity.getEntityId(), npc.getEntity().getUniqueId());
                    });
            if (!ENTITY_ID_UUID_TRANSLATION.isEmpty() || passedTimes.get() > 1200)
                Bukkit.getScheduler().cancelTask(taskID[0]);
        },  20L, 20L);
    }

    public static HideCitizen getInstance() {
        return instance;
    }

    public static void addCitizenTranslationIfNotExists(NPC npc) {
        Entity entity = npc.getEntity();
        ENTITY_ID_UUID_TRANSLATION.putIfAbsent(entity.getEntityId(), entity.getUniqueId());
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public static LuckPerms getLuckPerms() {
        return luckPerms;
    }
}
