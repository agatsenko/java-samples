/**
 * Author: Alexander Gatsenko (alexandr.gatsenko@gmail.com)
 */
package io.agatsenko.samples.typesafeconfig;

import com.typesafe.config.Config;

/**
 * This factory allows merging config files through system properties by the following order:
 * 1. -Dmerge.config.resource=/path/to/file/in/resources
 * 2. -Dmerge.config.file=/path/to/file/in/file/system
 */
public interface MergableConfigFactory {
    Config load();
}
