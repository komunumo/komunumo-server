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
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.StatisticService;
import org.komunumo.util.FormatterUtil;

import java.time.Year;

@CssImport(value = "./themes/komunumo/views/admin/analytics-board.css")
public class AnalyticsBoard extends Div {

    private final Chart monthlyVisitors = new Chart();
    private final H2 numberOfRegistrations = new H2();
    private final H2 numberOfEvents = new H2();
    private final H2 noShowRate = new H2();

    private final StatisticService statisticService;

    public AnalyticsBoard(@NotNull final StatisticService statisticService, @NotNull final Year year) {
        this.statisticService = statisticService;
        addClassName("analytics-board");

        final var board = new Board();
        board.addRow(
                createBadge("Registrations", numberOfRegistrations, "primary-text", "Registrations this year", "badge"),
                createBadge("Events", numberOfEvents, "success-text", "Events this year", "badge success"),
                createBadge("No-shows", noShowRate, "error-text", "No-show-rate this year", "badge error")
        );

        monthlyVisitors.getConfiguration().setTitle("Monthly visitors per location");
        monthlyVisitors.getConfiguration().getChart().setType(ChartType.COLUMN);
        final var monthlyVisitorsWrapper = new WrapperCard("wrapper", new Component[]{monthlyVisitors}, "card");
        board.add(monthlyVisitorsWrapper);

        add(board);
        populateCharts(year);
    }

    private WrapperCard createBadge(@NotNull final String title, @NotNull final H2 h2, @NotNull final String h2ClassName,
                                    @NotNull final String description, @NotNull final String badgeTheme) {
        final var titleSpan = new Span(title);
        titleSpan.addClassName("badge-title");
        titleSpan.getElement().setAttribute("theme", badgeTheme);

        h2.addClassName(h2ClassName);

        final var descriptionSpan = new Span(description);
        descriptionSpan.addClassName("secondary-text");

        return new WrapperCard("wrapper", new Component[]{titleSpan, h2, descriptionSpan}, "card", "space-m");
    }

    private void populateCharts(@NotNull final Year year) {
        // Top row widgets
        final var registrations = statisticService.countAttendeesByYear(year, StatisticService.NoShows.INCLUDE);
        final var events = statisticService.countEventsByYear(year);
        final var noShows = statisticService.countAttendeesByYear(year, StatisticService.NoShows.ONLY);
        numberOfRegistrations.setText(FormatterUtil.formatNumber(registrations));
        numberOfEvents.setText(FormatterUtil.formatNumber(events));
        noShowRate.setText(FormatterUtil.formatNumber(registrations == 0 ? 0 : noShows * 100L / registrations) + "%");

        final var locationColorMap = statisticService.getLocationColorMap();

        // First chart
        final var configuration = monthlyVisitors.getConfiguration();
        statisticService.calculateMonthlyVisitorsByYear(year).stream()
                .map(data -> {
                    final var series = new ListSeries(data.getLocation(),
                            data.getJanuary(), data.getFebruary(), data.getMarch(),
                            data.getApril(), data.getMay(), data.getJune(),
                            data.getJuly(), data.getAugust(), data.getSeptember(),
                            data.getOctober(), data.getNovember(), data.getDecember());
                    final var colorCode = locationColorMap.get(data.getLocation());
                    if (colorCode != null) {
                        final var options = new PlotOptionsColumn();
                        options.setColor(new SolidColor(colorCode));
                        series.setPlotOptions(options);
                    }
                    return series;
                }).forEach(configuration::addSeries);

        final var x = new XAxis();
        x.setCrosshair(new Crosshair());
        x.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        configuration.addxAxis(x);

        final var y = new YAxis();
        y.setMin(0);
        y.setTitle("");
        configuration.addyAxis(y);

        final var tooltip = new Tooltip();
        tooltip.setShared(true);
        configuration.setTooltip(tooltip);
    }

}