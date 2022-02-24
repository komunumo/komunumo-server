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

package org.komunumo.ui.view.website.news;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.NewsEntity;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.SubMenu;
import org.komunumo.ui.view.website.SubMenuItem;
import org.komunumo.ui.view.website.WebsiteLayout;

import java.util.ArrayList;
import java.util.List;

import static org.komunumo.util.FormatterUtil.formatDate;

@Route(value = "news", layout = WebsiteLayout.class)
@RouteAlias(value = "news/:id", layout = WebsiteLayout.class)
@CssImport("./themes/komunumo/views/website/news-view.css")
@AnonymousAllowed
public final class NewsView extends ContentBlock implements BeforeEnterObserver, HasDynamicTitle {

    private final DatabaseService databaseService;

    private NewsEntity newsEntity = null;

    public NewsView(@NotNull final DatabaseService databaseService) {
        super("News");
        this.databaseService = databaseService;
        addClassName("news-view");
    }

    @Override
    public void beforeEnter(@NotNull final BeforeEnterEvent beforeEnterEvent) {
        newsEntity = null;
        final var newsEntities = new ArrayList<NewsEntity>();

        final var params = beforeEnterEvent.getRouteParameters();
        final var idParam = params.get("id");
        if (idParam.isPresent()) {
            final var id = Long.parseLong(idParam.get());
            databaseService.getNewsWhenVisible(id).ifPresent(newsEntities::add);
            if (newsEntities.isEmpty()) {
                beforeEnterEvent.forwardTo(NewsView.class);
            } else {
                newsEntity = newsEntities.get(0);
            }
            setSubMenu(new SubMenu(new SubMenuItem("/news", "News overview")));
        } else {
            setSubMenu(null);
        }

        if (newsEntities.isEmpty()) {
            newsEntities.addAll(databaseService.getAllVisibleNews());
        }

        if (newsEntities.isEmpty()) {
            setContent(new Paragraph("No news available."));
        } else {
            setContent(createNewsList(newsEntities));
        }
    }

    private Component createNewsList(@NotNull final List<NewsEntity> newsEntities) {
        final var newsList = new Div();
        newsList.addClassName("news-list");
        newsEntities.stream()
                .map(NewsView::toNewsItem)
                .forEach(newsList::add);
        return newsList;
    }

    private static Component toNewsItem(@NotNull final NewsEntity newsEntity) {
        final var newsItem = new Article();
        newsItem.addClassName("news-item");

        final var createdDate = new Paragraph(formatDate(newsEntity.created().toLocalDate()));
        createdDate.addClassName("created-date");
        newsItem.add(createdDate);

        newsItem.add(new H2(newsEntity.title()));
        if (!newsEntity.subtitle().isBlank()) {
            newsItem.add(new H3(newsEntity.subtitle()));
        }

        final var teaser = new Paragraph(new Html("<div>%s</div>".formatted(newsEntity.teaser())));
        teaser.addClassName("teaser");
        newsItem.add(teaser);

        final var message = new Paragraph(new Html("<div>%s</div>".formatted(newsEntity.message())));
        message.addClassName("message");
        newsItem.add(message);

        return newsItem;
    }

    @Override
    public String getPageTitle() {
        return "%s: %s".formatted(databaseService.configuration().getWebsiteName(), newsEntity != null ? newsEntity.title() : "News");
    }

}
