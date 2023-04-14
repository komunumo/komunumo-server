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

package org.komunumo.ui.view.admin.dashboard;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Role;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.Serial;
import java.time.Year;
import java.util.Comparator;

@Route(value = "admin/dashboard", layout = AdminLayout.class)
@RouteAlias(value = "admin", layout = AdminLayout.class)
@PageTitle("Dashboard")
@CssImport(value = "./themes/komunumo/views/admin/dashboard-view.css")
@RolesAllowed(Role.Type.MEMBER)
public class DashboardView extends Div {

    @Serial
    private static final long serialVersionUID = 3466563649520421310L;

    public DashboardView(@NotNull final DatabaseService databaseService) {
        addClassName("dashboard-view");

        final var years = databaseService.getYearsWithEvents();
        final var minYear = years.stream().min(Comparator.naturalOrder()).orElse(Year.now());
        final var maxYear = years.stream().max(Comparator.naturalOrder()).orElse(Year.now());

        final var actualYear = Year.now();
        final var selectedYear = actualYear.isAfter(maxYear) ? maxYear : actualYear;

        final var title = new H3("Analytics");
        title.addClassName("title");

        final var yearSelector = new IntegerField();
        yearSelector.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        yearSelector.addClassName("year-selector");
        yearSelector.setStep(1);
        yearSelector.setStepButtonsVisible(true);
        yearSelector.setMin(minYear.getValue());
        yearSelector.setMax(maxYear.getValue());
        yearSelector.setValue(selectedYear.getValue());

        final var analyticsContainer = new Div(new AnalyticsBoard(databaseService, selectedYear));
        yearSelector.addValueChangeListener(valueChangeEvent -> {
            analyticsContainer.removeAll();
            analyticsContainer.add(new AnalyticsBoard(databaseService, Year.of(valueChangeEvent.getValue())));
        });

        add(title, new Div(yearSelector), analyticsContainer);
    }

}
