/**
 * Author: Alexander Gatsenko (alexandr.gatsenko@gmail.com)
 */
package io.agatsenko.samples.typesafeconfig;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DefaultMergableConfigFactory implements MergableConfigFactory {
    private static final String RESOURCE_MERGE_CONFIG_PROP_NAME = "merge.config.resource";
    private static final String FILE_MERGE_CONFIG_PROP_NAME = "merge.config.file";

    @Override
    public synchronized Config load() {
        ConfigFactory.invalidateCaches();
        final var defaultConfig = ConfigFactory.load();
        final var mergeResouConfig = mergeResourceConfig(defaultConfig);
        final var mergeFileConfig = mergeFileConfig(mergeResouConfig);
        return mergeFileConfig;
    }

    private static Config merge(Config defaultConfig, Config mergeConfig) {
        return mergeConfig.withFallback(defaultConfig);
    }

    private static Config mergeResourceConfig(Config defaultConfig) {
        final var resourcePath = String.valueOf(System.getProperties().get(RESOURCE_MERGE_CONFIG_PROP_NAME));
        return Strings.isNullOrEmpty(resourcePath) ?
                defaultConfig :
                merge(defaultConfig, ConfigFactory.parseResources(resourcePath));
    }

    private static Config mergeFileConfig(Config defaultConfig) {
        final var filePathStr = String.valueOf(System.getProperties().get(FILE_MERGE_CONFIG_PROP_NAME));
        if (Strings.isNullOrEmpty(filePathStr)) {
            return defaultConfig;
        }
        else {
            final var filePath = Path.of(filePathStr);
            return Files.exists(filePath) && !Files.isDirectory(filePath) ?
                    merge(defaultConfig, ConfigFactory.parseFile(filePath.toFile())) :
                    defaultConfig;
        }
    }
}
