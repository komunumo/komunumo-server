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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.Lumo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CssImport("./themes/komunumo/views/admin/komunumo-dialog.css")
public class EnhancedDialog extends Dialog {

    private static final String DOCK = "dock";
    private static final String FULLSCREEN = "fullscreen";

    private boolean isDocked = false;
    private boolean isFullScreen = false;

    private final Button minimise;
    private final Button maximise;

    private final VerticalLayout content;
    private final Footer footer;

    public EnhancedDialog(@NotNull final String title) {
        setCloseOnOutsideClick(false);
        setDraggable(false);
        setModal(true);
        setResizable(false);

        // Dialog theming
        getElement().getThemeList().add("komunumo-dialog");
        setWidth("600px");

        // Accessibility
        getElement().setAttribute("aria-labelledby", "dialog-title");

        // Header
        final var dialogTitle = new H2(title);
        dialogTitle.addClassName("dialog-title");

        minimise = new Button(VaadinIcon.DOWNLOAD_ALT.create());
        minimise.addClickListener(event -> minimise());
        minimise.setVisible(false); // maybe we activate this feature at a later time

        maximise = new Button(VaadinIcon.EXPAND_SQUARE.create());
        maximise.addClickListener(event -> maximise());
        maximise.setVisible(false); // maybe we activate this feature at a later time

        final var close = new Button(VaadinIcon.CLOSE_SMALL.create());
        close.addClickListener(event -> close());

        final var header = new Header(dialogTitle, minimise, maximise, close);
        header.getElement().getThemeList().add(Lumo.DARK);
        super.add(header);

        // Content
        content = new VerticalLayout();
        content.addClassName("dialog-content");
        content.setAlignItems(FlexComponent.Alignment.STRETCH);
        super.add(content);

        // Footer
        footer = new Footer();
        super.add(footer);

        // Button theming
        for (final var button : new Button[] {minimise, maximise, close }) {
            button.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY);
        }
    }

    private void minimise() {
        if (isDocked) {
            initialSize();
        } else {
            if (isFullScreen) {
                initialSize();
            }
            minimise.setIcon(VaadinIcon.UPLOAD_ALT.create());
            getElement().getThemeList().add(DOCK);
            setWidth("320px");
        }
        isDocked = !isDocked;
        isFullScreen = false;
        content.setVisible(!isDocked);
        footer.setVisible(!isDocked);
    }

    private void initialSize() {
        minimise.setIcon(VaadinIcon.DOWNLOAD_ALT.create());
        getElement().getThemeList().remove(DOCK);
        maximise.setIcon(VaadinIcon.EXPAND_SQUARE.create());
        getElement().getThemeList().remove(FULLSCREEN);
        setHeight("auto");
        setWidth("600px");
    }

    private void maximise() {
        if (isFullScreen) {
            initialSize();
        } else {
            if (isDocked) {
                initialSize();
            }
            maximise.setIcon(VaadinIcon.COMPRESS_SQUARE.create());
            getElement().getThemeList().add(FULLSCREEN);
            setSizeFull();
            content.setVisible(true);
            footer.setVisible(true);
        }
        isFullScreen = !isFullScreen;
        isDocked = false;
    }

    /**
     * @deprecated Use {@link #addToContent(Component...)} or {@link #addToFooter(Component...)} instead!
     */
    @Override
    @Deprecated(since = "1.0")
    public void add(@Nullable final Component... components) {
        throw new UnsupportedOperationException("Use \"addToContent(Component...)\" or \"addToFooter(Component...)\" instead");
    }

    /**
     * Add components to the content of the dialog.
     * @param components the components to add
     */
    public void addToContent(@NotNull final Component... components) {
        content.add(components);
    }

    /**
     * Add components to the footer of the dialog.
     * @param components the components to add
     */
    public void addToFooter(@NotNull final Component... components) {
        footer.add(components);
    }

}
