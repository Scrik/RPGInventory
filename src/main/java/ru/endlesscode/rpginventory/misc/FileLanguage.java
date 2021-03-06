/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 Osip Fatkullin
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.misc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileLanguage {
    @NotNull
    private final Plugin plugin;
    private final String locale;

    private FileConfiguration lang;
    private File langFile;

    public FileLanguage(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.locale = Config.getConfig().getString("language");
        this.langFile = new File(this.plugin.getDataFolder(), "lang/" + this.locale + ".lang");
        this.saveDefault();
        this.load();
    }

    private void saveDefault() {
        for (String loc : this.getSupportedLocales()) {
            String localeFile = "lang/" + loc + ".lang";
            if (!new File(this.plugin.getDataFolder(), localeFile).exists()) {
                this.plugin.saveResource(localeFile, true);
            }
        }
    }

    private List<String> getSupportedLocales() {
        List<String> supportedLocales = new ArrayList<>();

        CodeSource src = this.getClass().getProtectionDomain().getCodeSource();
        if (src == null) {
            RPGInventory.getPluginLogger().severe("Error while loading language list.");
            return supportedLocales;
        }

        URL jar = src.getLocation();

        try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
            while (true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null) {
                    break;
                }

                String name = e.getName();
                if (name.matches("lang/\\w+.lang")) {
                    supportedLocales.add(name.replaceAll("lang/|.lang", ""));
                }
            }
        } catch (IOException e) {
            RPGInventory.getPluginLogger().log(Level.SEVERE, "Error while loading language list.", e);
        }

        return supportedLocales;
    }

    private void load() {
        if (!this.langFile.exists()) {
            this.langFile = new File(this.plugin.getDataFolder() + "/lang", "en.lang");
        }

        this.lang = YamlConfiguration.loadConfiguration(this.langFile);

        try (Reader defaultLangStream = new InputStreamReader(
                this.plugin.getResource("lang/" + (this.getSupportedLocales().contains(locale) ? locale : "en") + ".lang"), "UTF8")) {
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(defaultLangStream);
            this.lang.setDefaults(defaultLang);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCaption(String name, Object... args) {
        String caption = this.lang.getString(name);
        if (caption == null) {
            this.plugin.getLogger().warning("Missing caption: " + name);
            caption = "&c[missing caption]";
        }

        if (args.length > 0) {
            caption = String.format(caption, args);
        }

        return StringUtils.coloredLine(caption);
    }
}
