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

package io.lunamc.plugins.gamebase.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VersionMetaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionMetaUtils.class);
    private static final List<MinecraftVersion> VERSIONS;

    static {
        LOGGER.debug("Initializing minecraft version metadata...");
        JaxbMinecraftVersions versions = null;
        try (InputStream in = VersionMetaUtils.class.getResourceAsStream("/versions.xml")) {
            if (in != null) {
                JAXBContext jaxbContext = JAXBContext.newInstance(JaxbMinecraftVersions.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                versions = (JaxbMinecraftVersions) unmarshaller.unmarshal(in);
            } else {
                LOGGER.debug("Input stream null");
            }
        } catch (JAXBException | ClassCastException e) {
            LOGGER.error("Error while unmarshalling version metadata", e);
            versions = null;
        } catch (IOException e) {
            LOGGER.error("Error while reading version metadata", e);
            versions = null;
        }
        if (versions != null) {
            List<JaxbMinecraftVersion> minecraftVersions = versions.getMinecraftVersions();
            if (minecraftVersions != null) {
                VERSIONS = Collections.unmodifiableList(minecraftVersions);
            } else {
                LOGGER.debug("minecraftVersions is null");
                VERSIONS = null;
            }
        } else {
            VERSIONS = null;
        }
        if (VERSIONS != null)
            LOGGER.debug("Finally initialized minecraft version metadata");
        else
            LOGGER.warn("Minecraft version metadata not available");
    }

    private VersionMetaUtils() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a utility class and should not be constructed");
    }

    public static boolean initialize() {
        try {
            getVersions();
        } catch (Throwable ignore) {
            return false;
        }
        return true;
    }

    public static List<MinecraftVersion> getVersions() {
        check();
        return VERSIONS;
    }

    public static Optional<MinecraftVersion> getVersionByProtocolVersion(int protocolVersion, VersionOrder order) {
        Objects.requireNonNull(order, "order must not be null");
        check();
        switch (order) {
            case LEAST_RECENT:
                for (int i = VERSIONS.size() - 1; i >= 0; i--) {
                    MinecraftVersion version = VERSIONS.get(i);
                    if (version.getProtocolVersion() == protocolVersion)
                        return Optional.of(version);
                }
                break;
            case MOST_RECENT:
                for (MinecraftVersion version : VERSIONS) {
                    if (version.getProtocolVersion() == protocolVersion)
                        return Optional.of(version);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown version order: " + order);
        }
        return Optional.empty();
    }

    private static void check() {
        if (VERSIONS == null)
            throw new UnsupportedOperationException("Versions are not available");
        if (VERSIONS.size() < 1)
            throw new UnsupportedOperationException("No version metadata available");
    }

    public enum VersionOrder {

        MOST_RECENT, LEAST_RECENT
    }

    public enum VersionType {
        SNAPSHOT, RELEASE, PRE_RELEASE, JOKE
    }

    public interface MinecraftVersion {

        String getVersionName();

        VersionType getVersionType();

        int getProtocolVersion();
    }

    @XmlRootElement(name = "minecraftVersions")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class JaxbMinecraftVersions {

        @XmlElement(name = "minecraftVersion")
        private List<JaxbMinecraftVersion> minecraftVersions;

        public List<JaxbMinecraftVersion> getMinecraftVersions() {
            return minecraftVersions;
        }

        public void setMinecraftVersions(List<JaxbMinecraftVersion> minecraftVersions) {
            this.minecraftVersions = minecraftVersions;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static class JaxbMinecraftVersion implements MinecraftVersion {

        @XmlAttribute(name = "versionName")
        private String versionName;

        @XmlAttribute(name = "versionType")
        private VersionType versionType;

        @XmlAttribute(name = "protocolVersion")
        private int protocolVersion;

        @Override
        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        @Override
        public VersionType getVersionType() {
            return versionType;
        }

        public void setVersionType(VersionType versionType) {
            this.versionType = versionType;
        }

        @Override
        public int getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
        }
    }
}
