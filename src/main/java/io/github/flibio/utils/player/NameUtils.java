/*
 * This file is part of Utils, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 - 2016 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
