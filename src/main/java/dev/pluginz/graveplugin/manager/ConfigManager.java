/*
 * This file is part of BT's GravePlugin, licensed under the MIT License.
 *
 *  Copyright (c) BT Pluginz <github@tubyoub.de>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package dev.pluginz.graveplugin.manager;

import dev.pluginz.graveplugin.GravePlugin;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ConfigManager {
    private YamlDocument config;
    private int graveTimeout;
    private boolean checkVersion;
    private final GravePlugin plugin;

    public ConfigManager(GravePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        try {
            config = YamlDocument.create(new File(plugin.getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,

                    UpdaterSettings.builder().setVersioning(new BasicVersioning("fileversion"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build());

            graveTimeout = config.getInt("graveTimeout", 60);
            checkVersion = config.getBoolean("checkVersion", true);
        } catch (IOException e) {
            //plugin.getLogger().severe("Could not load configuration: " + e.getMessage());
            plugin.getLogger().severe("Error while loading the configuration");
        }
    }

    public void saveConfig() {
        try {
            config.save();
        } catch (IOException e) {
            plugin.getLogger().severe("Error while saving the configuration");
        }
    }
    public void reloadConfig() {
        loadConfig();
    }
    public int getGraveTimeout(){
        return graveTimeout;
    }
    public void setGraveTimeout(int graveTimeout) {
        this.graveTimeout = graveTimeout;
    }

    public boolean isCheckVersion() {
        return checkVersion;
    }
}