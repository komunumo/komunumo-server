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

package org.komunumo.ui.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;

import java.io.Serial;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.enums.MemberTheme;
import org.komunumo.security.AuthenticatedUser;
import org.komunumo.ui.view.admin.dashboard.DashboardView;
import org.komunumo.ui.view.admin.events.EventsView;
import org.komunumo.ui.view.admin.faq.FaqView;
import org.komunumo.ui.view.admin.feedback.FeedbackView;
import org.komunumo.ui.view.admin.imports.ImportsView;
import org.komunumo.ui.view.admin.keywords.KeywordsView;
import org.komunumo.ui.view.admin.members.MembersView;
import org.komunumo.ui.view.admin.news.NewsView;
import org.komunumo.ui.view.admin.pages.PagesView;
import org.komunumo.ui.view.admin.settings.SettingsView;
import org.komunumo.ui.view.admin.speakers.SpeakersView;
import org.komunumo.ui.view.admin.sponsors.SponsorsView;
import org.komunumo.ui.view.login.ChangePasswordView;
import org.komunumo.util.GravatarUtil;

import java.util.ArrayList;
import java.util.Optional;

@CssImport(value = "./themes/komunumo/views/admin/admin-layout.css")
public final class AdminLayout extends AppLayout {

    @Serial
    private static final long serialVersionUID = -8057080937435753774L;
    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;
    private final Tabs menu;
    private H1 viewTitle;

    public AdminLayout(@NotNull final AuthenticatedUser authenticatedUser,
                       @NotNull final AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        addToNavbar(new CookieConsent());
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        addToDrawer(createDrawerContent());

        authenticatedUser.get().ifPresent(member -> UI.getCurrent().getElement().setAttribute("theme", member.getTheme().getLiteral()));
    }

    private Component createHeaderContent() {
        final var layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        viewTitle = new H1();
        layout.add(viewTitle);
        layout.add(createAvatarMenu());
        return layout;
    }

    private MenuBar createAvatarMenu() {
        final var menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        final var menuItem = menuBar.addItem(createAvatar());
        final var subMenu = menuItem.getSubMenu();
        final var darkThemeItem = subMenu.addItem("Dark Theme");
        final var lightThemeItem = subMenu.addItem("Light Theme");
        subMenu.add(new Hr());
        subMenu.addItem("Website", e -> UI.getCurrent().getPage().open("/", AnchorTarget.TOP.getValue()));
        subMenu.add(new Hr());
        subMenu.addItem("Logout", e -> authenticatedUser.logout());

        darkThemeItem.setCheckable(true);
        lightThemeItem.setCheckable(true);
        authenticatedUser.get().ifPresent(member -> {
            switch (member.getTheme()) {
                case dark -> darkThemeItem.setChecked(true);
                case light -> lightThemeItem.setChecked(true);
                default -> throw new IllegalStateException("Unexpected value: " + member.getTheme());
            }
        });

        darkThemeItem.addClickListener(clickEvent -> {
            authenticatedUser.get().ifPresent(member -> {
                member.setTheme(MemberTheme.dark);
                member.store();
            });
            UI.getCurrent().getElement().setAttribute("theme", "dark");
            lightThemeItem.setChecked(false);
        });

        lightThemeItem.addClickListener(clickEvent -> {
            authenticatedUser.get().ifPresent(member -> {
                member.setTheme(MemberTheme.light);
                member.store();
            });
            UI.getCurrent().getElement().setAttribute("theme", "light");
            darkThemeItem.setChecked(false);
        });

        return menuBar;
    }

    private Avatar createAvatar() {
        final var member = authenticatedUser.get().orElse(null);
        if (member != null) {
            final var avatar = new Avatar(String.format("%s %s", member.getFirstName(), member.getLastName()));
            avatar.setImage(GravatarUtil.getGravatarAddress(member.getEmail().toLowerCase(Locale.getDefault())));
            avatar.getStyle().set("cursor", "pointer");
            return avatar;
        } else {
            return new Avatar();
        }
    }

    private Component createDrawerContent() {
        final var layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        final var logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.add(new H2("Komunumo"));
        layout.add(logoLayout, menu);
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        final var member = authenticatedUser.get().orElse(null);
        if (member != null && member.getPasswordChange()) {
            return new Tab[] {
                    createTab(new AdminMenuItem("Change Password", ChangePasswordView.class, false))
            };
        }

        final var views  = new ArrayList<AdminMenuItem>();
        views.add(new AdminMenuItem("Dashboard", DashboardView.class, false));
        views.add(new AdminMenuItem("Events", EventsView.class, true));
        views.add(new AdminMenuItem("FAQ", FaqView.class, false));
        views.add(new AdminMenuItem("Feedback", FeedbackView.class, false));
        views.add(new AdminMenuItem("Keywords", KeywordsView.class, false));
        views.add(new AdminMenuItem("Members", MembersView.class, false));
        views.add(new AdminMenuItem("News", NewsView.class, false));
        views.add(new AdminMenuItem("Pages", PagesView.class, false));
        views.add(new AdminMenuItem("Speakers", SpeakersView.class, false));
        views.add(new AdminMenuItem("Sponsors", SponsorsView.class, false));
        views.add(new AdminMenuItem("Settings", SettingsView.class, true));
        views.add(new AdminMenuItem("Imports", ImportsView.class, false));

        final var tabs = new ArrayList<Tab>();
        views.forEach(adminMenuItem -> {
            if (accessChecker.hasAccess(adminMenuItem.navigationTarget())) {
                tabs.add(createTab(adminMenuItem));
            }
        });

        return tabs.toArray(new Tab[0]);
    }

    private static Tab createTab(@NotNull final AdminMenuItem adminMenuItem) {
        final var tab = new Tab();
        tab.add(new RouterLink(adminMenuItem.title(), adminMenuItem.navigationTarget()));
        ComponentUtil.setData(tab, Class.class, adminMenuItem.navigationTarget());
        if (adminMenuItem.newSection()) {
            tab.addClassName("new-section");
        }
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
        viewTitle.setText(getCurrentPageTitle());
    }

    private Optional<Tab> getTabForComponent(@NotNull final Component component) {
        return menu.getChildren().filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        final var title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
