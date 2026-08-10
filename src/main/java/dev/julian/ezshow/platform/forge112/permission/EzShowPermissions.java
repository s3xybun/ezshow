package dev.julian.ezshow.platform.forge112.permission;

import dev.julian.ezshow.platform.forge112.config.EzShowConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.server.permission.PermissionAPI;

public final class EzShowPermissions {
    public static final String SHOW_COMMAND = "ezshow.command.show";
    public static final String BYPASS_COOLDOWN = "ezshow.cooldown.bypass";

    private EzShowPermissions() {
    }

    public static void register() {
        PermissionAPI.registerNode(
            SHOW_COMMAND,
            EzShowConfig.permissions.showCommand.toForgeLevel(),
            "Allows a player to share their held item with /show."
        );
        PermissionAPI.registerNode(
            BYPASS_COOLDOWN,
            EzShowConfig.permissions.bypassCooldown.toForgeLevel(),
            "Allows a player to bypass the /show cooldown."
        );
    }

    public static boolean canShow(EntityPlayerMP player) {
        return PermissionAPI.hasPermission(player, SHOW_COMMAND);
    }

    public static boolean bypassesCooldown(EntityPlayerMP player) {
        return PermissionAPI.hasPermission(player, BYPASS_COOLDOWN);
    }
}
