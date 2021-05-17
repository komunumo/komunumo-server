package org.komunumo;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "komunumo")
@PWA(name = "Komunumo", shortName = "Komunumo", offlineResources = {"images/logo.png"})
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
