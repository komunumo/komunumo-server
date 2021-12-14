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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.NewsEntity;
import org.komunumo.data.service.NewsService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.WebsiteLayout;

import java.util.List;

@Route(value = "news", layout = WebsiteLayout.class)
@PageTitle("News")
@CssImport("./themes/komunumo/views/website/news-view.css")
@AnonymousAllowed
public class NewsView extends ContentBlock {

    public NewsView(@NotNull final NewsService newsService) {
        super("News");
        addClassName("news-view");

        final var visibleNews = newsService.getVisibleNews();
        if (visibleNews.isEmpty()) {
            setContent(new Paragraph("No news available."));
        } else {
            setContent(createNewsList(visibleNews));
        }
    }

    private Component createNewsList(@NotNull final List<NewsEntity> visibleNews) {
        final var newsList = new Div();
        newsList.addClassName("news-list");
        visibleNews.stream()
                .map(this::toNewsItem)
                .forEach(newsList::add);
        return newsList;
    }

    private Component toNewsItem(@NotNull final NewsEntity newsEntity) {
        final var newsItem = new Article();
        newsItem.addClassName("news-item");

        final var anchor = new Anchor();
        anchor.getElement().setAttribute("name", "news-item-%d".formatted(newsEntity.id()));
        newsItem.add(anchor);

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

}
