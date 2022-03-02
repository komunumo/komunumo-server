/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.komunumo.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public final class LoginAttemptService {

    public static final int MAX_LOGIN_ATTEMPTS = 3;
    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        super();
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<>() {
                    public @NotNull Integer load(@NotNull final String ip) {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(@NotNull final String ip) {
        attemptsCache.invalidate(ip);
    }

    public void loginFailed(@NotNull final String ip) {
        int attempts;
        try {
            attempts = attemptsCache.get(ip);
        } catch (final ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(ip, attempts);
    }

    public boolean isBlocked(@NotNull final String ip) {
        try {
            return attemptsCache.get(ip) >= MAX_LOGIN_ATTEMPTS;
        } catch (final ExecutionException e) {
            return false;
        }
    }
}
