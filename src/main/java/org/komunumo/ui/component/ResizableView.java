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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import org.jetbrains.annotations.Nullable;

public class ResizableView extends Div {

    private Registration listener;

    /**
     * @see Div#onAttach(AttachEvent)
     */
    @Override
    protected void onAttach(@Nullable final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Add browser window listener to observe width change
        getUI().ifPresent(ui -> listener = ui.getPage().addBrowserWindowResizeListener(event -> {
            final var width = event.getWidth();
            onResize(width);
        }));
        // Adjust Grid according to initial width of the screen
        getUI().ifPresent(ui -> ui.getPage().retrieveExtendedClientDetails(receiver -> {
            final var width = receiver.getBodyClientWidth();
            onResize(width);
        }));
    }

    /**
     * @see Div#onDetach(DetachEvent)
     */
    @Override
    protected void onDetach(@Nullable final DetachEvent detachEvent) {
        // Listener needs to be eventually removed in order to avoid resource leak
        listener.remove();
        super.onDetach(detachEvent);
    }

    /**
     * Override this method to react on resizing events for this view.
     *
     * @param width the width of the view
     */
    @SuppressWarnings("java:S1186") // methods should not be empty
    protected void onResize(@SuppressWarnings("unused") final int width) { }

}
