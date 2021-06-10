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

package org.komunumo.components.speaker;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.SpeakerService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SpeakerSearchField extends Div {

    private final ComboBox<SpeakerRecord> searchField = new ComboBox<>("Speaker");
    private final UnorderedList speakerList = new UnorderedList();

    private final List<SpeakerRecord> speakerRecordList = new ArrayList<>();

    public SpeakerSearchField(final SpeakerService speakerService) {
        searchField.setSizeFull();
        searchField.setClearButtonVisible(true);
        searchField.setItemLabelGenerator(speakerRecord -> String.format("%s %s",
                speakerRecord.getFirstName(), speakerRecord.getLastName()));
        searchField.setItems(speakerService.list(0, Integer.MAX_VALUE)
                .collect(Collectors.toList())); // TODO lazy loading
        searchField.addValueChangeListener(event -> {
            final var speakerRecord = event.getValue();
            if (speakerRecord != null && !speakerRecordList.contains(speakerRecord)) {
                speakerRecordList.add(speakerRecord);
                updateSpeakerList();
                searchField.clear();
            }
        });

        add(
                searchField,
                speakerList
        );
    }

    private void updateSpeakerList() {
        speakerList.removeAll();
        speakerRecordList.stream()
                .sorted(Comparator.comparing(SpeakerRecord::getFirstName).thenComparing(SpeakerRecord::getLastName))
                .map(this::createListItem)
                .forEach(speakerList::add);
    }

    private ListItem createListItem(final SpeakerRecord speakerRecord) {
        final var label = new Label(String.format("%s %s", speakerRecord.getFirstName(), speakerRecord.getLastName()));
        final var icon = new Icon(VaadinIcon.CLOSE_SMALL);
        icon.setColor("#ff0000");
        icon.getStyle().set("cursor", "pointer");
        icon.addClickListener(event -> {
            speakerRecordList.remove(speakerRecord);
            updateSpeakerList();
        });

        return new ListItem(label, icon);
    }

}
