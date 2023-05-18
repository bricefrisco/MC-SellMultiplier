package com.sellmultiplier.managers;

import com.sellmultiplier.utils.GeneralUtils;
import com.sellmultiplier.utils.Multiplier;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiplierManager {

    // An instance of the ConfigManager, which is responsible for handling the configuration of the plugin
    private final ConfigManager configManager;

    // An instance of the PermissionsManager, which is responsible for handling player permissions
    private final PermissionsManager permissionsManager;

    // Constructor for the MultiplierManager that takes ConfigManager and PermissionsManager as parameters
    public MultiplierManager(ConfigManager configManager, PermissionsManager permissionsManager) {
        this.configManager = configManager;
        this.permissionsManager = permissionsManager;
    }

    // Method to get the multiplier of a player
    public Multiplier getMultiplier(Player player) {
        // By default, the key is "default"
        String key = "default";
        // The value is fetched from the config manager by the key
        BigDecimal value = configManager.getMultiplierFromConfig("default");

        // If player is null or multiplier permissions are null, return a new multiplier with key and value
        if (player == null || permissionsManager.getMultiplierPerms() == null) {
            return new Multiplier(key, value);
        }

        // For each multiplier permission in the list of multiplier permissions
        for (String multiplierPerm : permissionsManager.getMultiplierPerms()) {
            // If the player has permission for the multiplier
            if (player.hasPermission("sell.multiplier." + multiplierPerm)) {
                // Get the value for the permission from the config manager
                BigDecimal permValue = configManager.getMultiplierFromConfig(multiplierPerm);
                // If the permission value is greater than the current value
                if (permValue.compareTo(value) > 0) {
                    // Set the key to the permission and the value to the permission value
                    key = multiplierPerm;
                    value = permValue;
                }
            }
        }

        // Return a new multiplier with the final key and value
        return new Multiplier(key, value);
    }

    public Multiplier getStackedMultiplier(Player player) {
        int count = 0;
        Pattern pattern = Pattern.compile("^sell\\.multiplier\\..*");
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            String permission = perm.getPermission();
            // Check if permission matches pattern
            Matcher matcher = pattern.matcher(permission);
            if (matcher.find()) {
                GeneralUtils.log(permission);
                count++;
            }
        }
        // Fetch the base multiplier from the config and adjust it
        double baseMultiplier = configManager.getStackingBaseValue() - 1;
        return new Multiplier(Integer.toString(count), (BigDecimal.valueOf(1 + (count * baseMultiplier))));
    }
}

