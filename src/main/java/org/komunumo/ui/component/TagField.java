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
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyPressEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@CssImport("./themes/komunumo/views/admin/tag-field.css")
public class TagField extends TextField {

    private Set<String> items;

    public TagField(@NotNull final String label) {
        this(label, Set.of());
    }

    public TagField(@NotNull final String label, @NotNull final Set<String> items) {
        super(label);
        addClassName("komunumo-tag-field");
        setItems(items);
        addKeyPressListener(this::keyPressListener);
    }

    public void setItems(@NotNull final Set<String> items) {
        this.items = new HashSet<>(items);
        updateItemView();
    }

    public Set<String> getItems() {
        return Collections.unmodifiableSet(items);
    }

    private void updateItemView() {
        final var itemView = new Span();
        items.stream().sorted().forEach(item -> itemView.add(createItemView(item)));
        setPrefixComponent(itemView);
    }

    private Component createItemView(@NotNull final String item) {
        final var tag = new Span();
        tag.addClassName("tag");

        final var tagLabel = new Span(new Text(item));
        tagLabel.addClassName("tag-label");

        final var tagRemoveButton = new Span();
        tagRemoveButton.addClassName("tag-remove-button");
        tagRemoveButton.setTitle("remove");
        tagRemoveButton.addClickListener(clickListener -> {
            items.remove(item);
            updateItemView();
            focus();
        });

        tag.add(tagLabel, tagRemoveButton);
        return tag;
    }

    private void keyPressListener(@NotNull final KeyPressEvent keyPressEvent) {
        if (keyPressEvent.getKey().equals(Key.ENTER)) {
            final var value = getValue();
            if (!value.isBlank()) {
                items.add(value.trim());
                setValue("");
                updateItemView();
            }
        }
    }

}
