package org.komunumo.data.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.komunumo.data.entity.Member;
import org.komunumo.views.dashboard.DashboardView;
import org.komunumo.views.events.EventsView;
import org.komunumo.views.logout.LogoutView;
import org.komunumo.views.main.MainView;
import org.komunumo.views.members.MembersView;
import org.komunumo.views.sponsors.SponsorsView;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public record AuthorizedRoute(String route, String name, Class<? extends Component> view) {}

    public class AuthException extends Exception {}

    private final MemberRepository memberRepository;

    public AuthService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void authenticate(final String email, final String password) throws AuthException {
        final var member = memberRepository.getByEmail(email);
        if (member != null && member.checkPassword(password)) {
            VaadinSession.getCurrent().setAttribute(Member.class, member);
            createRoutes(member);
        } else {
            throw new AuthException();
        }
    }

    private void createRoutes(final Member member) {
        getAuthorizedRoutes(member)
                .forEach(route -> RouteConfiguration.forSessionScope().setRoute(route.route, route.view, MainView.class));
    }

    public List<AuthorizedRoute> getAuthorizedRoutes(final Member member) {
        final var routes = new ArrayList<AuthorizedRoute>();

        if (member != null) {
            routes.add(new AuthorizedRoute("dashboard", "Dashboard", DashboardView.class));
            if (member.isAdmin()) {
                routes.add(new AuthorizedRoute("events/:eventID?/:action?(edit)", "Events", EventsView.class));
                routes.add(new AuthorizedRoute("members/:memberID?/:action?(edit)", "Members", MembersView.class));
                routes.add(new AuthorizedRoute("sponsors/:sponsorID?/:action?(edit)", "Sponsors", SponsorsView.class));
            }
            routes.add(new AuthorizedRoute("logout", "Logout", LogoutView.class));
        }

        return routes;
    }

    public void register(final String firstName, final String lastName, final String email,
                         final String address, final String zipCode, final String city,
                         final String state, final String country) {
        final var member = new Member();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setAddress(address);
        member.setZipCode(zipCode);
        member.setCity(city);
        member.setState(state);
        member.setCountry(country);
        member.setMemberSince(LocalDate.now());
        member.setAdmin(false);
        memberRepository.save(member);
    }

}
