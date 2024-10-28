package be.machigan.hidecitizen;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HideCitizenTabCompleter implements TabCompleter {
    private static final List<String> ACTIONS = List.of("toggle", "show", "hide");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission(HideCitizen.USE_PERMISSION) || args.length <= 1)
            return List.of();
        if (args.length == 2)
            return StringUtil.copyPartialMatches(args[1], ACTIONS, new ArrayList<>());
        if (args.length == 3)
            return null;
        return List.of();
    }
}
