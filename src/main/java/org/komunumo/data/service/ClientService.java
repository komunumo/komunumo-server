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

package org.komunumo.data.service;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.komunumo.data.db.tables.records.ClientRecord;
import org.komunumo.data.entity.Client;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.komunumo.data.db.tables.Client.CLIENT;

@Service
public class ClientService {

    private final DSLContext dsl;

    public ClientService(@NotNull final DSLContext dsl) {
        this.dsl = dsl;
    }

    public Client newClient() {
        return dsl.newRecord().into(Client.class);
    }

    public Optional<Client> getClient(@NotNull final Long id) {
        return dsl.selectFrom(CLIENT)
                .where(CLIENT.ID.eq(id))
                .fetchOptionalInto(Client.class);
    }

    public void store(@NotNull final ClientRecord clientRecord) {
        clientRecord.store();
    }
}
