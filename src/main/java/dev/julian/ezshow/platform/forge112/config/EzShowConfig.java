package dev.julian.ezshow.platform.forge112.config;

import dev.julian.ezshow.platform.forge112.EzShowForgeMod;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

@Config(modid = EzShowForgeMod.MOD_ID, name = EzShowForgeMod.MOD_ID)
public final class EzShowConfig {
    @Config.Name("cooldownSeconds")
    @Config.Comment({
        "Seconds a player must wait between successful /show uses.",
        "Set to 0 to disable the cooldown."
    })
    @Config.RangeInt(min = 0, max = 86400)
    public static int cooldownSeconds = 5;

    @Config.Name("permissions")
    @Config.Comment("Default permission policy. Changes require a game/server restart.")
    public static final PermissionSettings permissions = new PermissionSettings();

    private EzShowConfig() {
    }

    public static final class PermissionSettings {
        @Config.Name("showCommand")
        @Config.Comment("Default for the ezshow.command.show permission node.")
        @Config.RequiresMcRestart
        public PermissionDefault showCommand = PermissionDefault.ALL;

        @Config.Name("bypassCooldown")
        @Config.Comment("Default for the ezshow.cooldown.bypass permission node.")
        @Config.RequiresMcRestart
        public PermissionDefault bypassCooldown = PermissionDefault.OP;
    }

    public enum PermissionDefault {
        ALL(DefaultPermissionLevel.ALL),
        OP(DefaultPermissionLevel.OP),
        NONE(DefaultPermissionLevel.NONE);

        private final DefaultPermissionLevel forgeLevel;

        PermissionDefault(DefaultPermissionLevel forgeLevel) {
            this.forgeLevel = forgeLevel;
        }

        public DefaultPermissionLevel toForgeLevel() {
            return forgeLevel;
        }
    }
}
