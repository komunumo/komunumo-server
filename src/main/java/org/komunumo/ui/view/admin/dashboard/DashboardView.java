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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Crosshair;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import java.time.Year;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.EventService;
import org.komunumo.ui.view.admin.AdminView;

@Route(value = "admin/dashboard", layout = AdminView.class)
@RouteAlias(value = "", layout = AdminView.class)
@PageTitle("Dashboard")
public class DashboardView extends Div {

    private final Chart monthlyVisitors = new Chart();
    private final H2 usersH2 = new H2();
    private final H2 eventsH2 = new H2();
    private final H2 conversionH2 = new H2();

    private final EventService eventService;

    public DashboardView(@NotNull final EventService eventService) {
        this.eventService = eventService;

        addClassName("dashboard-view");

        final var board = new Board();
        board.addRow(
                createBadge("Registrations", usersH2, "primary-text", "Registrations this year", "badge"),
                createBadge("Events", eventsH2, "success-text", "Events this year", "badge success"),
                createBadge("No-shows", conversionH2, "error-text", "No-show-rate this year", "badge error")
        );

        monthlyVisitors.getConfiguration().setTitle("Monthly visitors per location");
        monthlyVisitors.getConfiguration().getChart().setType(ChartType.COLUMN);
        final var monthlyVisitorsWrapper = new WrapperCard("wrapper", new Component[]{monthlyVisitors}, "card");
        board.add(monthlyVisitorsWrapper);

        add(board);
        populateCharts();
    }

    private WrapperCard createBadge(@NotNull final String title, @NotNull final H2 h2, @NotNull final String h2ClassName,
                                    @NotNull final String description, @NotNull final String badgeTheme) {
        final var titleSpan = new Span(title);
        titleSpan.getElement().setAttribute("theme", badgeTheme);

        h2.addClassName(h2ClassName);

        final var descriptionSpan = new Span(description);
        descriptionSpan.addClassName("secondary-text");

        return new WrapperCard("wrapper", new Component[]{titleSpan, h2, descriptionSpan}, "card", "space-m");
    }

    private void populateCharts() {
        // Set some data when this view is displayed.

        // Top row widgets
        usersH2.setText("1'174");
        eventsH2.setText(Integer.toString(eventService.countByYear(Year.now())));
        conversionH2.setText("24%");

        // First chart
        var configuration = monthlyVisitors.getConfiguration();
        configuration.addSeries(
                new ListSeries("Basel", 48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2));
        configuration.addSeries(
                new ListSeries("Bern", 48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2));
        configuration.addSeries(
                new ListSeries("Luzern", 83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3));
        configuration.addSeries(
                new ListSeries("St. Gallen", 42.4, 33.2, 34.5, 39.7, 52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1));
        configuration.addSeries(
                new ListSeries("Zürich", 49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4));

        var x = new XAxis();
        x.setCrosshair(new Crosshair());
        x.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        configuration.addxAxis(x);

        var y = new YAxis();
        y.setMin(0);
        y.setTitle("");
        configuration.addyAxis(y);

    }
}
