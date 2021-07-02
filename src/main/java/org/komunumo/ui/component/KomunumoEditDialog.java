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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.Lumo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.UpdatableRecord;
import org.komunumo.ApplicationContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

@CssImport("./themes/komunumo/views/admin/komunumo-dialog.css")
public abstract class KomunumoEditDialog<R extends UpdatableRecord<?>> extends Dialog {

    private static final String DOCK = "dock";
    private static final String FULLSCREEN = "fullscreen";

    private boolean isDocked = false;
    private boolean isFullScreen = false;

    private final Button min;
    private final Button max;

    private final VerticalLayout content;
    private final Footer footer;

    protected final Binder<R> binder;
    protected final FormLayout formLayout;

    private Callback afterSave;
    private boolean initialized;

    public KomunumoEditDialog(@NotNull final String title) {
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

        min = new Button(VaadinIcon.DOWNLOAD_ALT.create());
        min.addClickListener(event -> minimise());
        min.setVisible(false); // maybe we activate this feature at a later time

        max = new Button(VaadinIcon.EXPAND_SQUARE.create());
        max.addClickListener(event -> maximise());
        max.setVisible(false); // maybe we activate this feature at a later time

        final var close = new Button(VaadinIcon.CLOSE_SMALL.create());
        close.addClickListener(event -> close());

        final var header = new Header(dialogTitle, min, max, close);
        header.getElement().getThemeList().add(Lumo.DARK);
        add(header);

        // Content

        formLayout = new FormLayout();

        binder = new Binder<>();


        // Content
        content = new VerticalLayout(formLayout);
        content.addClassName("dialog-content");
        content.setAlignItems(FlexComponent.Alignment.STRETCH);
        add(content);

        // Footer
        final var save = new Button("Save");
        save.setEnabled(false);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> {
            if (binder.isValid()) {
                final var dsl = ApplicationContextHolder.getBean(DSLContext.class);
                final var transactionTemplate = ApplicationContextHolder.getBean(TransactionTemplate.class);
                transactionTemplate.executeWithoutResult((transactionStatus) -> {
                    dsl.attach(binder.getBean());
                    binder.getBean().store();

                    if (afterSave != null) {
                        afterSave.execute();
                    }
                });
                close();
            } else {
                Notification.show("Pay attention to the instructions in the form!");
            }
        });
        binder.addStatusChangeListener(event -> save.setEnabled(binder.isValid()));

        final var cancel = new Button("Cancel");
        cancel.addClickListener(click -> close());

        footer = new Footer(save, cancel);
        add(footer);

        // Button theming
        for (final var button : new Button[] { min, max, close }) {
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
            min.setIcon(VaadinIcon.UPLOAD_ALT.create());
            getElement().getThemeList().add(DOCK);
            setWidth("320px");
        }
        isDocked = !isDocked;
        isFullScreen = false;
        content.setVisible(!isDocked);
        footer.setVisible(!isDocked);
    }

    private void initialSize() {
        min.setIcon(VaadinIcon.DOWNLOAD_ALT.create());
        getElement().getThemeList().remove(DOCK);
        max.setIcon(VaadinIcon.EXPAND_SQUARE.create());
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
            max.setIcon(VaadinIcon.COMPRESS_SQUARE.create());
            getElement().getThemeList().add(FULLSCREEN);
            setSizeFull();
            content.setVisible(true);
            footer.setVisible(true);
        }
        isFullScreen = !isFullScreen;
        isDocked = false;
    }

    public abstract void createForm();

    @Override
    @Deprecated()
    public void open() {
        throw new UnsupportedOperationException("use \"open(UpdateableRecord, Callback)\" instead");
    }

    @SuppressWarnings("unchecked")
    public void open(@NotNull final UpdatableRecord<?> record, @Nullable final Callback afterSave) {
        binder.setBean((R) record);
        this.afterSave = afterSave;

        if (!initialized) {
            createForm();
            initialized = true;
        }

        super.open();
    }

    @FunctionalInterface
    public interface Callback {
        void execute();
    }
}
