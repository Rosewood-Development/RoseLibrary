package dev.rosewood.rosegarden.config;

import java.io.File;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

public class CommentedFileConfiguration extends CommentedConfigurationSection {

    private int comments;
    private CommentedFileConfigurationHelper helper;
    private File file;

    public CommentedFileConfiguration(Reader configStream, File configFile, int comments) {
        super(YamlConfiguration.loadConfiguration(configStream));
        this.comments = comments;
        this.helper = new CommentedFileConfigurationHelper();
        this.file = configFile;
    }

    public static CommentedFileConfiguration loadConfiguration(File file) {
        return new CommentedFileConfigurationHelper().getNewConfig(file);
    }

    public void set(String path, Object value, String... comments) {
        this.addPathedComments(path, comments);
        this.set(path, value);
    }

    public void addComments(String... comments) {
        for (String comment : comments) {
            this.set("_COMMENT_" + this.comments, " " + comment);
            this.comments++;
        }
    }

    public void addPathedComments(String path, String... comments) {
        if (!this.contains(path)) {
            int subpathIndex = path.lastIndexOf('.');
            String subpath = subpathIndex == -1 ? "" : path.substring(0, subpathIndex) + '.';

            for (String comment : comments) {
                this.set(subpath + "_COMMENT_" + this.comments, " " + comment);
                this.comments++;
            }
        }
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(this.helper.getConfigContent(this.file));
    }

    public void save() {
        this.save(false);
    }

    public void save(boolean compactLines) {
        String config = this.getConfigAsString();
        this.helper.saveConfig(config, this.file, compactLines);
    }

    public void save(File file) {
        this.save(file, false);
    }

    public void save(File file, boolean compactLines) {
        String config = this.getConfigAsString();
        this.helper.saveConfig(config, file, compactLines);
    }

    private String getConfigAsString() {
        if (!(this.config instanceof YamlConfiguration))
            throw new UnsupportedOperationException("Cannot get config string of non-YamlConfiguration");

        YamlConfiguration yamlConfiguration = (YamlConfiguration) this.config;

        // Edit the configuration to how we want it
        try {
            Field field_yamlOptions = YamlConfiguration.class.getDeclaredField("yamlOptions");
            field_yamlOptions.setAccessible(true);
            DumperOptions yamlOptions = (DumperOptions) field_yamlOptions.get(yamlConfiguration);
            yamlOptions.setWidth(Integer.MAX_VALUE);

            if (Stream.of(DumperOptions.class.getDeclaredMethods()).anyMatch(x -> x.getName().equals("setIndicatorIndent")))
                yamlOptions.setIndicatorIndent(2);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return yamlConfiguration.saveToString();
    }

}
