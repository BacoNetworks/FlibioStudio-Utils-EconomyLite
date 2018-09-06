/*
 * This file is part of Utils, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */

package io.github.flibio.utils.player;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NameUtils {

    /**
     * Looks up a player's UUID.
     * 
     * @param name Name of the player whom to lookup.
     * @return The UUID found.
     */
    public static Optional<UUID> getUUID(String name) {
        GameProfileManager manager = Sponge.getServer().getGameProfileManager();
        GameProfile profile;
        try {
            profile = manager.get(name).get();
        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
        return Optional.of(profile.getUniqueId());
    }

    /**
     * Looks up a player's name.
     * 
     * @param uuid UUID of the player whom to lookup.
     * @return Name of the corresponding player.
     */
    public static Optional<String> getName(UUID uuid) {
        GameProfileManager manager = Sponge.getServer().getGameProfileManager();
        GameProfile profile;
        try {
            profile = manager.get(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
        return profile.getName();
    }
}
