package network.multicore.vc.utils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Messages {
    private static Messages instance;
    private final YamlDocument yaml;
    private final String console;

    private Messages(File file) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("messages.yml")) {
            yaml = YamlDocument.create(
                    file,
                    Objects.requireNonNull(is),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder()
                            .setAutoUpdate(true)
                            .setCreateFileIfAbsent(true)
                            .build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );

            yaml.update();
            yaml.save();

            console = yaml.getString("console");
        }
    }

    public static void init(File file) throws IOException {
        instance = new Messages(file);
    }

    public static Messages get() {
        if (instance == null) throw new IllegalStateException("Messages not initialized");
        return instance;
    }

    public String get(String route) {
        return yaml.getString(route);
    }

    public List<String> getStringList(String route) {
        return yaml.getStringList(route);
    }

    public String getAndReplace(String route, String[] targets, Object[] replacements) {
        String message = yaml.getString(route);

        for (int i = 0; i < targets.length; i++) {
            if (replacements[i] instanceof CommandSource src) {
                if (src instanceof Player player) replacements[i] = player.getUsername();
                else replacements[i] = console;
            }

            message = message.replace(String.format("{%s}", targets[i]), replacements[i].toString());
        }

        return message;
    }

    public String getAndReplace(String route, String target, Object replacement) {
        return getAndReplace(route, new String[]{target}, new Object[]{replacement});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2) {
        return getAndReplace(route, new String[]{target1, target2}, new Object[]{replacement1, replacement2});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3) {
        return getAndReplace(route, new String[]{target1, target2, target3}, new Object[]{replacement1, replacement2, replacement3});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4) {
        return getAndReplace(route, new String[]{target1, target2, target3, target4}, new Object[]{replacement1, replacement2, replacement3, replacement4});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5) {
        return getAndReplace(route, new String[]{target1, target2, target3, target4, target5}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5, String target6, Object replacement6) {
        return getAndReplace(route, new String[]{target1, target2, target3, target4, target5, target6}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5, replacement6});
    }

    public String formatDate(Date date) {
        SimpleDateFormat format;
        try {
            format = new SimpleDateFormat(yaml.getString("time.date-format"));
        } catch (Throwable t) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        return format.format(date);
    }
}
