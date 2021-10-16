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
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
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
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.AuthService;
import org.komunumo.ui.view.admin.dashboard.DashboardView;
import org.komunumo.ui.view.admin.events.EventsView;
import org.komunumo.ui.view.admin.bigmarker.BigMarkerView;
import org.komunumo.ui.view.admin.keywords.KeywordsView;
import org.komunumo.ui.view.admin.members.MembersView;
import org.komunumo.ui.view.admin.speakers.SpeakersView;
import org.komunumo.ui.view.admin.sponsors.SponsorsView;
import org.komunumo.ui.view.logout.LogoutView;
import org.komunumo.util.GravatarUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

public class AdminLayout extends AppLayout {

    private final AuthService authService;
    private final Tabs menu;
    private H1 viewTitle;

    public AdminLayout(@NotNull final AuthService authService) {
        this.authService = authService;
        addToNavbar(new CookieConsent());
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));
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
        subMenu.addItem("Logout", e -> UI.getCurrent().navigate(LogoutView.class));

        return menuBar;
    }

    private Avatar createAvatar() {
        final var member = authService.getCurrentUser();
        if (member != null) {
            final var avatar = new Avatar(String.format("%s %s", member.getFirstName(), member.getLastName()));
            avatar.setImage(GravatarUtil.getGravatarAddress(member.getEmail().toLowerCase()));
            return avatar;
        } else {
            return new Avatar();
        }
    }

    private Component createDrawerContent(@NotNull final Tabs menu) {
        final var layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        final var logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.add(new Image("images/logo.png", "Komunumo logo"));
        logoLayout.add(new H1("Komunumo"));
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
        final var tabs = new ArrayList<Tab>();

        final var views  = new LinkedHashMap<String, Class<? extends Component>>();
        views.put("Dashboard", DashboardView.class);
        views.put("Events", EventsView.class);
        views.put("Bigmarker", BigMarkerView.class);
        views.put("Keywords", KeywordsView.class);
        views.put("Members", MembersView.class);
        views.put("Speakers", SpeakersView.class);
        views.put("Sponsors", SponsorsView.class);

        views.forEach((title, klass) -> {
            if (authService.isAccessGranted(klass)) {
                tabs.add(createTab(title, klass));
            }
        });

        return tabs.toArray(new Tab[0]);
    }

    private static Tab createTab(@NotNull final String text,
                                 @NotNull final Class<? extends Component> navigationTarget) {
        final var tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
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
