/**
 * Author: Alexander Gatsenko (alexandr.gatsenko@gmail.com)
 */
package io.agatsenko.samples.typesafeconfig;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.common.io.ByteStreams;
import com.typesafe.config.Config;
import lombok.Builder;
import lombok.Value;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultMergableConfigFactoryTest {
    private static final String FILE_CONFIG_CONF_RES_PATH = "/file-config.conf";

    private static final DefaultMergableConfigFactory configFactory = new DefaultMergableConfigFactory();

    private Properties systemProperties;

    @Test
    public void loadShouldLoadOnlyApplicationConf() {
        final var config = configFactory.load();
        assertThat(config).isNotNull();

        final var rootConfig = buildRootConfig(config);
        assertThat(rootConfig).isNotNull();
        assertThat(rootConfig.getMyDuration()).isEqualTo(Duration.ofHours(12));
        assertThat(rootConfig.getServiceConfig()).isNotNull();
        assertThat(rootConfig.getServiceConfig().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig.getServiceConfig().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig.getServiceConfig().getSize3()).isEqualTo(10_000_000L);
        assertThat(rootConfig.getService2Config()).isNotNull();
        assertThat(rootConfig.getService2Config().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig.getService2Config().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig.getService2Config().getSize3()).isEqualTo(10_000_000L);
        assertThat(rootConfig.getService2Config().getFoo()).isNull();
    }

    @Test
    public void loadShouldMergeSystemPropConfigs() {
        System.getProperties().setProperty("myDuration", "10 seconds");
        final var config = configFactory.load();
        assertThat(config).isNotNull();
        final var rootConfig = buildRootConfig(config);
        assertThat(rootConfig).isNotNull();
        assertThat(rootConfig.getMyDuration()).isEqualTo(Duration.ofSeconds(10));
        assertThat(rootConfig.getServiceConfig()).isNotNull();
        assertThat(rootConfig.getServiceConfig().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig.getServiceConfig().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig.getServiceConfig().getSize3()).isEqualTo(10_000_000L);
        assertThat(rootConfig.getService2Config()).isNotNull();
        assertThat(rootConfig.getService2Config().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig.getService2Config().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig.getService2Config().getSize3()).isEqualTo(10_000_000L);
        assertThat(rootConfig.getService2Config().getFoo()).isNull();

        System.getProperties().setProperty("service2.one.size2", "123");
        final var config2 = configFactory.load();
        assertThat(config2).isNotNull();
        final var rootConfig2 = buildRootConfig(config2);
        assertThat(rootConfig2).isNotNull();
        assertThat(rootConfig2.getMyDuration()).isEqualTo(Duration.ofSeconds(10));
        assertThat(rootConfig2.getServiceConfig()).isNotNull();
        assertThat(rootConfig2.getServiceConfig().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig2.getServiceConfig().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig2.getServiceConfig().getSize3()).isEqualTo(10_000_000L);
        assertThat(rootConfig2.getService2Config()).isNotNull();
        assertThat(rootConfig2.getService2Config().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig2.getService2Config().getSize2()).isEqualTo(123L);
        assertThat(rootConfig2.getService2Config().getSize3()).isEqualTo(10_000_000L);
        assertThat(rootConfig.getService2Config().getFoo()).isNull();
    }

    @Test
    public void loadShouldMergeConfigFromResource() {
        System.getProperties().setProperty("merge.config.resource", "path/to/test/config/application-test.properties");

        final var config = configFactory.load();
        assertThat(config).isNotNull();

        final var rootConfig = buildRootConfig(config);
        assertThat(rootConfig).isNotNull();
        assertThat(rootConfig.getMyDuration()).isEqualTo(Duration.ofHours(12));
        assertThat(rootConfig.getServiceConfig()).isNotNull();
        assertThat(rootConfig.getServiceConfig().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig.getServiceConfig().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig.getServiceConfig().getSize3()).isEqualTo(10_000_000L);
        assertThat(rootConfig.getService2Config()).isNotNull();
        assertThat(rootConfig.getService2Config().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig.getService2Config().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig.getService2Config().getSize3()).isEqualTo(20_000_000_000_000_000L);
        assertThat(rootConfig.getService2Config().getFoo()).isEqualTo("bar");
    }

    @Test
    public void loadShouldMergeConfigFromResourceAndFile() throws IOException {
        final var fileConfigPath = Files.createTempFile("hocon-overridable-config", ".conf");
        try (final var fileConfigInStream =
                     DefaultMergableConfigFactoryTest.class.getResourceAsStream(FILE_CONFIG_CONF_RES_PATH);
             final var fileConfigOutStream = Files.newOutputStream(fileConfigPath)) {
            ByteStreams.copy(fileConfigInStream, fileConfigOutStream);
        }

        System.getProperties().setProperty("merge.config.resource", "path/to/test/config/application-test.properties");
        System.getProperties().setProperty("merge.config.file", fileConfigPath.toString());

        final var config = configFactory.load();
        assertThat(config).isNotNull();

        final var rootConfig = buildRootConfig(config);
        assertThat(rootConfig).isNotNull();
        assertThat(rootConfig.getMyDuration()).isEqualTo(Duration.ofHours(12));
        assertThat(rootConfig.getServiceConfig()).isNotNull();
        assertThat(rootConfig.getServiceConfig().getSize()).isEqualTo(123L);
        assertThat(rootConfig.getServiceConfig().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig.getServiceConfig().getSize3()).isEqualTo(10_000_000L);
        assertThat(rootConfig.getService2Config()).isNotNull();
        assertThat(rootConfig.getService2Config().getSize()).isEqualTo(10_000_000_000L);
        assertThat(rootConfig.getService2Config().getSize2()).isEqualTo(10_000L);
        assertThat(rootConfig.getService2Config().getSize3()).isEqualTo(20_000_000_000_000_000L);
        assertThat(rootConfig.getService2Config().getFoo()).isEqualTo("bar");
    }

    @BeforeEach
    public void saveSystemProperties() {
        systemProperties = new Properties(System.getProperties());
    }

    @AfterEach
    public void restoreSystemProperties() {
        if (systemProperties != null) {
            System.setProperties(systemProperties);
            systemProperties = null;
        }
    }

    private static RootConfig buildRootConfig(Config config) {
        return RootConfig.builder()
                .myDuration(config.getDuration("myDuration"))
                .serviceConfig(RootConfig.ServiceConfig.builder()
                        .size(config.getBytes("service.one.size"))
                        .size2(config.getBytes("service.one.size2"))
                        .size3(config.getBytes("service.one.size3"))
                        .build()
                )
                .service2Config(RootConfig.Service2Config.builder()
                        .size(config.getBytes("service2.one.size"))
                        .size2(config.getBytes("service2.one.size2"))
                        .size3(config.getBytes("service2.one.size3"))
                        .foo(config.getIsNull("service2.one.foo") ?
                                null :
                                config.getString("service2.one.foo")
                        )
                        .build()
                )
                .build();
    }

    @Builder
    @Value
    public static final class RootConfig {
        private final Duration myDuration;
        private final ServiceConfig serviceConfig;
        private final Service2Config service2Config;

        @Builder
        @Value
        public static class ServiceConfig {
            private final Long size;
            private final Long size2;
            private final Long size3;
        }

        @Builder
        @Value
        public static class Service2Config {
            private final Long size;
            private final Long size2;
            private final Long size3;
            private final String foo;
        }
    }
}
