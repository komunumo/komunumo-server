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

package org.komunumo.ui.view.login;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.MemberRecord;

@Route(value = "blocked")
@PageTitle("Account blocked")
public class BlockedView extends Div implements BeforeEnterObserver {

    private final Text message;

    public BlockedView() {
        this.message = new Text("");

        add(
                new H2("Your account is blocked"),
                new Text("Reason: "),
                message
        );
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent event) {
        final var member = VaadinSession.getCurrent().getAttribute(MemberRecord.class);
        if (member.getBlocked()) {
            final var reason = member.getBlockedReason();
            message.setText(reason.isBlank() ? "unknown" : reason);
        } else {
            event.forwardTo(LoginView.class);
        }
    }
}
