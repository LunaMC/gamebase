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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "games", namespace = "http://lunamc.io/game/1.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class GamesConfiguration {

    @XmlElement(name = "game", namespace = "http://lunamc.io/game/1.0")
    private List<Game> games;

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Game {

        @XmlElementWrapper(name = "virtualHosts", namespace = "http://lunamc.io/game/1.0")
        @XmlElement(name = "virtualHost", namespace = "http://lunamc.io/game/1.0")
        private List<String> virtualHosts;

        @XmlElementWrapper(name = "blocks", namespace = "http://lunamc.io/game/1.0")
        @XmlElement(name = "block", namespace = "http://lunamc.io/game/1.0")
        private List<Block> blocks;

        public List<String> getVirtualHosts() {
            return virtualHosts;
        }

        public void setVirtualHosts(List<String> virtualHosts) {
            this.virtualHosts = virtualHosts;
        }

        public List<Block> getBlocks() {
            return blocks;
        }

        public void setBlocks(List<Block> blocks) {
            this.blocks = blocks;
        }
    }

    public static class Block {

        @XmlAttribute(name = "name")
        private String name;

        @XmlAttribute(name = "paletteId")
        private int paletteId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPaletteId() {
            return paletteId;
        }

        public void setPaletteId(int paletteId) {
            this.paletteId = paletteId;
        }
    }
}
