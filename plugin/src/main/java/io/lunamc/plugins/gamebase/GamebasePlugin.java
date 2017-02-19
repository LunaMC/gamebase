package io.lunamc.plugins.gamebase;

import io.lunamc.common.host.StaticVirtualHost;
import io.lunamc.common.host.VirtualHostManager;
import io.lunamc.common.text.builder.ComponentBuilderFactory;
import io.lunamc.platform.plugin.PluginAdapter;
import io.lunamc.platform.plugin.PluginContext;
import io.lunamc.platform.plugin.annotation.LunaPlugin;
import io.lunamc.platform.plugin.annotation.LunaPluginDependency;
import io.lunamc.platform.service.ServiceRegistry;
import io.lunamc.plugins.gamebase.status.ExampleStatusProvider;
import io.lunamc.plugins.netty.handler.PlayHandlerFactory;

@LunaPlugin(
        id = "luna-gamebase",
        version = "0.0.1",
        pluginDependencies = {
                @LunaPluginDependency(id = "luna-common", versionExpression = "0.*"),
                @LunaPluginDependency(id = "luna-netty", versionExpression = "0.*")
        }
)
public class GamebasePlugin extends PluginAdapter {

    @Override
    public void initialize(PluginContext context) {
        DefaultGame game = new DefaultGame(MinecraftVersion.VERSION_1_10_2);
        game.initialize();

        ServiceRegistry serviceRegistry = context.getServiceRegistry();
        serviceRegistry.setService(PlayHandlerFactory.class, new DefaultPlayHandlerFactory(game.getBlockRegistry()));
    }

    @Override
    public void start(PluginContext context) {
        ServiceRegistry serviceRegistry = context.getServiceRegistry();
        VirtualHostManager virtualHostManager = serviceRegistry.getService(VirtualHostManager.class).requireInstance();

        virtualHostManager.setFallbackHost(new StaticVirtualHost(
                null,
                new ExampleStatusProvider(serviceRegistry.getService(ComponentBuilderFactory.class)),
                false,
                null
        ));
    }
}
