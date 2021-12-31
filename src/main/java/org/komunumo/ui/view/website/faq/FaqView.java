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

package org.komunumo.ui.view.website.faq;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.FaqRecord;
import org.komunumo.data.service.FaqService;
import org.komunumo.ui.view.website.ContentBlock;
import org.komunumo.ui.view.website.WebsiteLayout;

@Route(value = "faq", layout = WebsiteLayout.class)
@PageTitle("FAQ")
@CssImport("./themes/komunumo/views/website/faq-view.css")
@AnonymousAllowed
public class FaqView extends ContentBlock {

    public FaqView(@NotNull final FaqService faqService) {
        super("FAQ");
        addClassName("faq-view");

        final var content = new Div();
        content.add(new H2("Frequently asked questions"));
        faqService.getAllFaqRecords()
                .map(this::toArticle)
                .forEach(content::add);
        setContent(content);
    }

    private Component toArticle(@NotNull final FaqRecord faqRecord) {
        final var article = new Article();
        article.addClassName("faq-entry");
        article.add(new H3(faqRecord.getQuestion()));
        article.add(new Html("<div>%s</div>".formatted(faqRecord.getAnswer())));
        return article;
    }

}
