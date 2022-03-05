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

import java.io.Serial;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@CssImport("./themes/komunumo/views/admin/tag-field.css")
public final class TagField extends TextField {

    @Serial
    private static final long serialVersionUID = -3370499523503628664L;
    private Set<String> tags;

    public TagField(@NotNull final String label) {
        super(label);
        addClassName("komunumo-tag-field");
        addKeyPressListener(this::keyPressListener);
        tags = new HashSet<>();
    }

    @Override
    public void setValue(@NotNull final String value) {
        this.tags = value.isBlank() ? new HashSet<>() : Arrays.stream(value.split(","))
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());
        updateItemView();
    }

    @Override
    public String getValue() {
        return tags.isEmpty() ? "" : tags.stream()
                .sorted()
                .collect(Collectors.joining(","));
    }

    private void updateItemView() {
        final var itemView = new Span();
        tags.stream().sorted().forEach(item -> itemView.add(createItemView(item)));
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
            tags.remove(item);
            updateItemView();
            triggerChangeEvent();
            focus();
        });

        tag.add(tagLabel, tagRemoveButton);
        return tag;
    }

    private void triggerChangeEvent() {
        final var originalValue = super.getValue();
        super.setValue(originalValue.concat(" "));
        super.setValue(originalValue);
    }

    private void keyPressListener(@NotNull final KeyPressEvent keyPressEvent) {
        if (keyPressEvent.getKey().equals(Key.ENTER)) {
            final var newValue = super.getValue();
            if (!newValue.isBlank()) {
                Arrays.stream(newValue.split(","))
                        .filter(value -> !value.isBlank())
                        .forEach(value -> tags.add(value.trim()));
                super.setValue("");
                updateItemView();
            }
        }
    }

}
