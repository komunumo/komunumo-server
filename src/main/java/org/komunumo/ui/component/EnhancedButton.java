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

package org.komunumo.ui.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public final class EnhancedButton extends Button {

    @Serial
    private static final long serialVersionUID = 4972834260936037127L;

    public EnhancedButton(@NotNull final String text, @NotNull final ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, clickListener);
    }

    public EnhancedButton(@NotNull final Component icon, @NotNull final ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(icon, clickListener);
    }

    public void setTitle(@NotNull final String title) {
        getElement().setAttribute("title", title);
    }

}
