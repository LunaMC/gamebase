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

import io.lunamc.gamebase.GameManager;
import io.lunamc.platform.plugin.PluginAdapter;
import io.lunamc.platform.plugin.PluginContext;
import io.lunamc.platform.plugin.annotation.LunaPlugin;
import io.lunamc.platform.plugin.annotation.LunaPluginDependency;
import io.lunamc.platform.service.ServiceRegistry;

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
        ServiceRegistry serviceRegistry = context.getServiceRegistry();
        serviceRegistry.setService(GameManager.class, new DefaultGameManager());
    }
}
