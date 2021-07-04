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
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.ui.view.admin.AdminLayout;
import org.komunumo.util.FormatterUtil;

@Route(value = "admin/dashboard", layout = AdminLayout.class)
@RouteAlias(value = "admin", layout = AdminLayout.class)
@PageTitle("Dashboard")
public class DashboardView extends Div {

    private final Chart monthlyVisitors = new Chart();
    private final H2 usersH2 = new H2();
    private final H2 eventsH2 = new H2();
    private final H2 noShowsH2 = new H2();

    private final EventService eventService;
    private final EventMemberService eventMemberService;

    public DashboardView(@NotNull final EventService eventService,
                         @NotNull final EventMemberService eventMemberService) {
        this.eventService = eventService;
        this.eventMemberService = eventMemberService;

        addClassName("dashboard-view");

        final var board = new Board();
        board.addRow(
                createBadge("Registrations", usersH2, "primary-text", "Registrations this year", "badge"),
                createBadge("Events", eventsH2, "success-text", "Events this year", "badge success"),
                createBadge("No-shows", noShowsH2, "error-text", "No-show-rate this year", "badge error")
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
        usersH2.setText(FormatterUtil.formatNumber(eventMemberService.countByYear(Year.now())));
        eventsH2.setText(FormatterUtil.formatNumber(eventService.countByYear(Year.now())));
        noShowsH2.setText(FormatterUtil.formatNumber(eventMemberService.calculateNoShowRateByYear(Year.now())) + "%");

        // First chart
        var configuration = monthlyVisitors.getConfiguration();
        eventMemberService.calculateMonthlyVisitorsByYear(Year.now()).stream()
                .map(data -> new ListSeries(data.getLocation(),
                        data.getJanuary(), data.getFebruary(), data.getMarch(),
                        data.getApril(), data.getMay(), data.getJune(),
                        data.getJuly(), data.getAugust(), data.getSeptember(),
                        data.getOctober(), data.getNovember(), data.getDecember()))
                .forEach(configuration::addSeries);

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
