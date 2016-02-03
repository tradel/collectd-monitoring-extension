/**
 * @author Todd Radel <tradel@appdynamics.com>
 */

package com.appdynamics.extensions.collectd.util;

import java.io.File;

import com.appdynamics.extensions.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.apache.commons.lang3.StringUtils;

public class ConfigUtils {

    public static String resolvePath(String filename) {
        if(StringUtils.isBlank(filename)){
            return "";
        }

        //for absolute paths
        if(new File(filename).exists()){
            return filename;
        }

        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = String.format("%s%s%s", jarPath, File.separator, filename);
        return configFileName;
    }

}
