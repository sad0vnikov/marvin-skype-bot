package net.sadovnikov.marvinbot.core.main;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.fortsoft.pf4j.*;

import java.util.ArrayList;
import java.util.List;

public class PluginLoader {

    private PluginManager pluginManager;
    private Logger logger;

    @Inject
    public PluginLoader(Injector injector) {

        logger = LogManager.getLogger("core-logger");

        /**
         * Overriding DefaultExtensionFactory so that we could inject modules in extensions
         */
        PluginManager pluginManager = new DefaultPluginManager() {
            @Override
            protected ExtensionFactory createExtensionFactory() {
                return new DefaultExtensionFactory() {
                    @Override
                    public Object create(Class<?> extensionClass) {
                        logger.debug("Create instance for extension '{}'", extensionClass.getName());
                        return injector.getInstance(extensionClass);
                    }
                };
            }
        };

        this.pluginManager = pluginManager;
    }

    public void loadPlugins() {
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        List<String> pluginNames = new ArrayList<>();
        for (PluginWrapper plugin: pluginManager.getStartedPlugins()) {
            pluginNames.add(plugin.getPlugin().getClass().getCanonicalName());
        }

        logger.info("Run PluginManager in {} mode", pluginManager.getRuntimeMode().toString());
        if (pluginNames.size() > 0) {
            logger.info("Loaded {} plugins: {}", pluginNames.size(), String.join(", ", pluginNames));
        } else {
            logger.info("Didn't find any plugins");
        }
    }

    public PluginManager getPluginManager() {
        return pluginManager;

    }

}