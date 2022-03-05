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

package org.komunumo.ui.view.website;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.NotFoundException;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.enums.PageParent;
import org.komunumo.data.entity.Page;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.util.FormatterUtil;

@CssImport("./themes/komunumo/views/website/content-block.css")
public class ContentBlock extends HorizontalLayout {

    @Serial
    private static final long serialVersionUID = -7498317913387890319L;
    private final HtmlContainer titleColumn;

    private HtmlContainer subMenuContainer = null;
    private HtmlContainer contentColumn;

    public ContentBlock(@NotNull final String title) {
        this(title, null, new Div());
    }

    public ContentBlock(@NotNull final String title, @Nullable final Component subMenu, @NotNull final Component content) {
        setClassName("content-block");

        final var h2 = new H2(title);
        titleColumn = new Div(h2);
        titleColumn.addClassName("title-column");

        if (subMenu != null) {
            subMenuContainer = new Div(subMenu);
            subMenuContainer.addClassName("sub-menu");
            titleColumn.add(subMenuContainer);
        }

        contentColumn = new Div(content);
        contentColumn.addClassName("content-column");

        add(titleColumn, contentColumn);
    }

    /**
     * Set a sub menu (second level) to this page.
     * @param subMenu the sub menu
     */
    public void setSubMenu(@Nullable final Component subMenu) {
        if (subMenuContainer == null && subMenu == null) {
            return;
        }

        if (subMenuContainer != null) {
            titleColumn.remove(subMenuContainer);
        }

        if (subMenu != null) {
            subMenuContainer = new Div(subMenu);
            subMenuContainer.addClassName("sub-menu");
            titleColumn.add(subMenuContainer);
        }
    }

    /**
     * Set the content of this page.
     * @param content the content
     */
    public void setContent(@NotNull final Component... content) {
        final var newContentColumn = new Div(content);
        newContentColumn.addClassName("content-column");
        replace(contentColumn, newContentColumn);
        contentColumn = newContentColumn;
    }

    /**
     * Load a page from the database.
     * @param databaseService the service to access the database
     * @param url the URL of the page to load
     * @return the loaded page
     */
    public Page loadPage(@NotNull final DatabaseService databaseService, @NotNull final String url) {
        final var urlParts = url.split("/", 2);
        if (urlParts.length < 2 || urlParts[1].isBlank()) {
            throw new NotFoundException();
        }
        final var parent = PageParent.valueOf(FormatterUtil.camelCase(urlParts[0]));
        final var page = databaseService.getPage(parent, urlParts[1]).orElseThrow(NotFoundException::new);
        setContent(
                new H2(page.getTitle()),
                new Html("<div>%s</div>".formatted(page.getContent()))
        );
        return page;
    }

}
