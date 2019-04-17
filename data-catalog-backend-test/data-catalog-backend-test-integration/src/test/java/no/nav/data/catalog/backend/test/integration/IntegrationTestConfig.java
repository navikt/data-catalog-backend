package no.nav.data.catalog.backend.test.integration;

import no.nav.data.catalog.backend.app.AppStarter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AppStarter.class})
@ComponentScan(value = "no.nav.data.catalog.backend.test.integration")
public class IntegrationTestConfig {
}
