package no.nav.data.catalog.backend.app.common.jpa;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.vault.config.databases.VaultDatabaseProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.lease.LeaseEndpoints;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.support.VaultResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static no.nav.data.catalog.backend.app.common.utils.MdcUtils.wrapAsync;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "spring.cloud.vault.enabled", matchIfMissing = true)
public class VaultHikariConfig implements InitializingBean {

    private static final String VAULT_ROTATER = "VaultRotater";

    private final SecretLeaseContainer container;
    private final VaultOperations vaultOperations;
    private final HikariDataSource ds;
    private final VaultDatabaseProperties props;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    public VaultHikariConfig(SecretLeaseContainer container, VaultOperations vaultOperations,
            HikariDataSource ds, VaultDatabaseProperties props) {
        this.container = container;
        this.vaultOperations = vaultOperations;
        this.ds = ds;
        this.props = props;
        scheduler.setThreadNamePrefix(VAULT_ROTATER);
        scheduler.initialize();
    }

    @Override
    public void afterPropertiesSet() {
        container.setLeaseEndpoints(LeaseEndpoints.SysLeases);
        scheduleNextRotation(Duration.ofMinutes(5).toSeconds(), 0);
    }

    private void rotate() {
        int tries = 0;
        while (tries++ < 10) {
            try {
                String path = getPath();
                log.info("Roterer brukernavn/passord for: {}", path);
                VaultResponse vaultResponse = vaultOperations.read(path);
                Assert.notNull(vaultResponse, "No response from vault");
                log.info("Ny lease for {} {}", path, vaultResponse.getLeaseId());
                updateCredentials(vaultResponse);
                int minutesBeforeExpire = 30;
                scheduleNextRotation(vaultResponse.getLeaseDuration(), minutesBeforeExpire);
                return;
            } catch (Exception e) {
                log.error("error rotating db credentials", e);
                try {
                    Thread.sleep(1000L * (tries * tries));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    log.warn("sleepinterruped", ex);
                }
            }
        }
        log.error("Failed getting database credentials");
    }

    private void updateCredentials(VaultResponse vaultResponse) {
        Map<String, Object> data = vaultResponse.getData();
        Assert.notNull(data, "No data in vaultResponse");
        val username = data.get("username").toString();
        val password = data.get("password").toString();
        ds.setUsername(username);
        ds.setPassword(password);
        ds.getHikariPoolMXBean().softEvictConnections();
    }

    private void scheduleNextRotation(long leaseDuration, int minutesBeforeExpire) {
        Instant startTime = Instant.now().plusSeconds(leaseDuration - minutesBeforeExpire * 60);
        log.info("Ny lease duration: {}, next: {}", leaseDuration, startTime);
        scheduler.schedule(wrapAsync(this::rotate, VAULT_ROTATER), startTime);
    }

    private String getPath() {
        return String.format("%s/creds/%s", props.getBackend(), props.getRole());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [container=" + container + ", ds=" + ds + ", props=" + props + "]";
    }
}
