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

import com.vaadin.componentfactory.EnhancedDatePicker;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.komunumo.util.ComponentUtil.createDatePicker;

public class EnhancedDateTimePicker extends Div {

    private final EnhancedDatePicker datePicker;
    private final TimePicker timePicker;

    private LocalDateTime minValue = null;

    public EnhancedDateTimePicker(@NotNull final String dateLabel, @NotNull final String timeLabel) {
        datePicker = createDatePicker(dateLabel, null);
        timePicker = new TimePicker(timeLabel);
        timePicker.addValueChangeListener(this::validateTime);

        final var layout = new HorizontalLayout(datePicker, timePicker);
        layout.setPadding(false);
        add(layout);
    }

    public void setValue(@Nullable LocalDateTime value) {
        datePicker.setValue(value != null ? value.toLocalDate() : null);
        timePicker.setValue(value != null ? value.toLocalTime() : null);
    }

    public LocalDateTime getValue() {
        final var date = datePicker.getValue();
        final var time = timePicker.getValue();
        return date != null && time != null ? LocalDateTime.of(date, time) : null;
    }

    public boolean isEmpty() {
        return getValue() == null;
    }

    public void setMin(@Nullable final LocalDateTime minValue) {
        this.minValue = minValue;
        datePicker.setMin(minValue != null ? minValue.toLocalDate() : null);
    }

    private void validateTime(@NotNull final ComponentValueChangeEvent<TimePicker, LocalTime> event) {
        final var date = datePicker.getValue();
        final var time = event.getValue();
        if (minValue != null && date != null && time != null) {
            timePicker.setInvalid(LocalDateTime.of(date, time).isBefore(minValue));
        } else {
            timePicker.setInvalid(false);
        }
    }

}
