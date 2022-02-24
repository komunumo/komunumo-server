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
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.Binder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.UpdatableRecord;
import org.komunumo.ApplicationContextHolder;
import org.komunumo.Callback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class EditDialog<R extends UpdatableRecord<?>> extends EnhancedDialog {

    private final Binder<R> binder;
    private final FormLayout formLayout;
    private final Button save;

    private Callback afterSave;
    private boolean initialized;

    protected EditDialog(@NotNull final String title) {
        super(title);

        // Content
        formLayout = new FormLayout();
        binder = new Binder<>();
        addToContent(formLayout);

        // Footer
        save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> {
            if (binder.isValid()) {
                final var dsl = ApplicationContextHolder.getBean(DSLContext.class);
                final var transactionTemplate = ApplicationContextHolder.getBean(TransactionTemplate.class);
                transactionTemplate.executeWithoutResult(transactionStatus -> {
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

        final var cancel = new Button("Cancel");
        cancel.addClickListener(click -> close());

        addToFooter(save, cancel);
    }

    @SuppressWarnings("checkstyle:HiddenField")
    public abstract void createForm(@NotNull FormLayout formLayout, @NotNull Binder<R> binder);

    /**
     * @deprecated Use {@link #open(UpdatableRecord)} instead!
     */
    @Override
    @Deprecated(since = "1.0")
    public void open() {
        throw new UnsupportedOperationException("use \"open(UpdateableRecord)\" instead");
    }

    /**
     * Open (show) the dialog.
     * @param record the record object to be displayed
     */
    public void open(@NotNull final R record) {
        open(record, null, null);
    }

    /**
     * Open (show) the dialog.
     * @param record the record object to be displayed
     * @param afterSave a callback to be called after save action
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public void open(@NotNull final R record, @Nullable final Callback afterSave) {
        open(record, null, afterSave);
    }

    /**
     * Open (show) the dialog.
     * @param record the record object to be displayed
     * @param afterOpen a callback to be called after dialog was opened/shown
     * @param afterSave a callback to be called after save action
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public void open(@NotNull final R record, @Nullable final Callback afterOpen, @Nullable final Callback afterSave) {
        binder.setBean(record);
        this.afterSave = afterSave;

        if (!initialized) {
            createForm(formLayout, binder);
            binder.addStatusChangeListener(event -> save.setEnabled(binder.isValid()));
            binder.validate();
            initialized = true;
        }

        save.setEnabled(false);
        focusFirstFormField();

        super.open();

        if (afterOpen != null) {
            afterOpen.execute();
        }
    }

    /**
     * Close the dialog. Unsaved modifications will be reset.
     */
    @Override
    public void close() {
        final var record = binder.getBean();
        if (record != null) {
            record.reset();
        }
        super.close();
    }

    private void focusFirstFormField() {
        //noinspection rawtypes
        formLayout.getChildren()
                .filter(Component::isVisible)
                .filter(HasValue.class::isInstance)
                .filter(c -> !((HasValue) c).isReadOnly())
                .filter(HasEnabled.class::isInstance)
                .filter(c -> ((HasEnabled) c).isEnabled())
                .filter(Focusable.class::isInstance)
                .findFirst()
                .ifPresent(c -> ((Focusable) c).focus());
    }

}
