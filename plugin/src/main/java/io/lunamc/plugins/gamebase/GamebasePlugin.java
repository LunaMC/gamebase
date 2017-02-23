/*
 *  Copyright 2017 LunaMC.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.lunamc.plugins.gamebase;

import com.google.common.reflect.Reflection;
import io.lunamc.common.host.VirtualHost;
import io.lunamc.common.host.VirtualHostManager;
import io.lunamc.common.text.builder.ComponentBuilderFactory;
import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.GameManager;
import io.lunamc.gamebase.block.BlockRegistry;
import io.lunamc.platform.plugin.PluginAdapter;
import io.lunamc.platform.plugin.PluginContext;
import io.lunamc.platform.plugin.annotation.LunaPlugin;
import io.lunamc.platform.plugin.annotation.LunaPluginDependency;
import io.lunamc.platform.service.ServiceRegistration;
import io.lunamc.platform.service.ServiceRegistry;
import io.lunamc.plugins.gamebase.status.SimpleStatusProvider;
import io.lunamc.plugins.gamebase.utils.MinecraftUtils;
import io.lunamc.plugins.gamebase.utils.VersionMetaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@LunaPlugin(
        id = "luna-gamebase",
        version = "0.0.1",
        pluginDependencies = {
                @LunaPluginDependency(id = "luna-common", versionExpression = "0.*"),
                @LunaPluginDependency(id = "luna-netty", versionExpression = "0.*")
        }
)
public class GamebasePlugin extends PluginAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GamebasePlugin.class);

    @Override
    public void initialize(PluginContext context) {
        VersionMetaUtils.initialize();

        ServiceRegistry serviceRegistry = context.getServiceRegistry();
        serviceRegistry.setService(GameManager.class, new DefaultGameManager());
    }

    @Override
    public void start(PluginContext context) {
        File configFile = new File(context.getDescription().getDataDirectory(), "games.xml");
        if (configFile.isFile()) {
            GamesConfiguration config = loadGamesConfiguration(configFile);
            if (config != null) {
                ServiceRegistry serviceRegistry = context.getServiceRegistry();
                GameManager gameManager = serviceRegistry.getService(GameManager.class).requireInstance();
                VirtualHostManager virtualHostManager = serviceRegistry.getService(VirtualHostManager.class).requireInstance();
                ServiceRegistration<ComponentBuilderFactory> componentBuilderFactory = serviceRegistry.getService(ComponentBuilderFactory.class);
                config.getGames().forEach(game -> {
                    try {
                        register(gameManager, virtualHostManager, componentBuilderFactory, game);
                    } catch (Throwable throwable) {
                        LOGGER.error("Error while registering game", throwable);
                    }
                });
            }
        } else {
            LOGGER.warn("Configuration file {} not found", configFile.getName());
        }
    }

    private GamesConfiguration loadGamesConfiguration(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            JAXBContext jaxbContext = JAXBContext.newInstance(GamesConfiguration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (GamesConfiguration) unmarshaller.unmarshal(in);
        } catch (JAXBException | ClassCastException e) {
            LOGGER.error("An exception occurred while unmarshalling {}", file.getName(), e);
        } catch (IOException e) {
            LOGGER.error("An exception occurred while loading {}", file.getName(), e);
        }
        return null;
    }

    private static void register(GameManager gameManager, VirtualHostManager virtualHostManager, ServiceRegistration<ComponentBuilderFactory> componentBuilderFactory, GamesConfiguration.Game source) {
        Game game = new DefaultGame();

        // Register virtual hosts
        Collection<VirtualHost> handledVirtualHosts = game.getHandledVirtualHosts();
        for (String virtualHostName : source.getVirtualHosts()) {
            Optional<VirtualHost> virtualHost = virtualHostManager.getVirtualHostByName(virtualHostName);
            if (virtualHost.isPresent())
                handledVirtualHosts.add(virtualHost.get());
            else
                LOGGER.warn("Unknown virtual host: {}", virtualHostName);
        }

        // Register blocks
        BlockRegistry blockRegistry = game.getBlockRegistry();
        for (GamesConfiguration.Block block : source.getBlocks())
            blockRegistry.register(block.getName(), MinecraftUtils.parsePaletteId(block.getPaletteId()));

        GamesConfiguration.StatusProvider statusProvider = source.getStatusProvider();
        if (statusProvider != null)
            game.setStatusProvider(new SimpleStatusProvider(componentBuilderFactory, game, statusProvider.getMotd(), statusProvider.getMaxPlayers()));

        gameManager.addGame(game);
    }
}
