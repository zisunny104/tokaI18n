package dev.toka.i18n;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The texts of one plugin. The texts are in the files lang/zh-TW.yml and so on, inside the plugin and in the data folder
 * of the plugin; what is in the data folder wins, so the owner of the server can change a text. A missing text is looked
 * for in the language of the plugin and then in en-US, and when it is nowhere the key is returned, so that the gap can be seen.
 * <p>
 * The numbers in a text are written ${0}, ${1} and so on, and they are replaced one time: a replaced value that looks
 * like ${1} is not changed again. Color codes are written with the character § in the file.
 */
public final class Messages {
    private static final Pattern ARGUMENT = Pattern.compile("\\$\\{(\\d{1,2})}");

    private final ClassLoader loader;
    private final Path folder;
    private final Consumer<String> warn;
    private volatile String language;
    private volatile Map<String, Map<String, Object>> tables = Map.of();
    private final Set<String> warned = new HashSet<>();

    /**
     * @param loader   where the texts inside the plugin are read from
     * @param folder   the data folder of the plugin, the files are in its lang folder
     * @param language the language of the plugin, such as zh-TW
     * @param warn     receives one line the first time a text is missing, may be null
     */
    public Messages(ClassLoader loader, Path folder, String language, Consumer<String> warn) {
        this.loader = loader;
        this.folder = folder;
        this.language = Locales.normalize(language, Locales.ZH_TW);
        this.warn = warn == null ? line -> {
        } : warn;
        this.reload();
    }

    public String language() {
        return this.language;
    }

    public synchronized void language(String language) {
        this.language = Locales.normalize(language, this.language);
        this.reload();
    }

    /**
     * Reads the files again. A file that cannot be read is skipped and one line is sent to the warning receiver.
     */
    public synchronized void reload() {
        Map<String, Map<String, Object>> next = new HashMap<>();
        for (String name : new String[]{Locales.EN_US, this.language}) {
            Map<String, Object> table = new HashMap<>();
            this.read(table, () -> this.loader.getResourceAsStream("lang/" + name + ".yml"), name + " (inside the plugin)");
            this.read(table, () -> {
                Path file = this.folder.resolve("lang").resolve(name + ".yml");
                return Files.isRegularFile(file) ? Files.newInputStream(file) : null;
            }, name + " (data folder)");
            next.put(name, table);
        }
        this.tables = next;
        synchronized (this.warned) {
            this.warned.clear();
        }
    }

    private interface Source {
        InputStream open() throws IOException;
    }

    private void read(Map<String, Object> table, Source source, String label) {
        try (InputStream stream = source.open()) {
            if (stream == null) {
                return;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                Object root = new Yaml(new SafeConstructor(new LoaderOptions())).load(reader);
                if (root instanceof Map<?, ?> map) {
                    flatten("", map, table);
                }
            }
        } catch (IOException | RuntimeException error) {
            this.warn.accept("Cannot read the texts " + label + ": " + error.getMessage());
        }
    }

    private static void flatten(String prefix, Map<?, ?> map, Map<String, Object> out) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? String.valueOf(entry.getKey()) : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> child) {
                flatten(key, child, out);
            } else if (value instanceof List<?> list) {
                List<String> lines = new ArrayList<>();
                for (Object item : list) {
                    lines.add(String.valueOf(item));
                }
                out.put(key, lines);
            } else if (value != null) {
                out.put(key, String.valueOf(value));
            }
        }
    }

    private Object find(String key) {
        Map<String, Map<String, Object>> current = this.tables;
        for (String name : new String[]{this.language, Locales.EN_US}) {
            Map<String, Object> table = current.get(name);
            if (table != null && table.containsKey(key)) {
                return table.get(key);
            }
        }
        return null;
    }

    public boolean has(String key) {
        return this.find(key) != null;
    }

    /**
     * @return the text with the arguments in it; a text with several lines is joined with line breaks
     */
    public String text(String key, Object... arguments) {
        Object value = this.find(key);
        if (value == null) {
            this.warnOnce("missing:" + key, "Text is missing: " + key);
            return key;
        }
        String template = value instanceof List<?> list ? String.join("\n", list.stream().map(String::valueOf).toList()) : (String) value;
        return this.fill(key, template, arguments);
    }

    /**
     * @return every line of the text, each with the arguments in it; one line when the text is a single string
     */
    public List<String> lines(String key, Object... arguments) {
        Object value = this.find(key);
        if (value == null) {
            this.warnOnce("missing:" + key, "Text is missing: " + key);
            return List.of(key);
        }
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object line : list) {
                out.add(this.fill(key, String.valueOf(line), arguments));
            }
        } else {
            for (String line : ((String) value).split("\n", -1)) {
                out.add(this.fill(key, line, arguments));
            }
        }
        return out;
    }

    private String fill(String key, String template, Object[] arguments) {
        if (!template.contains("${")) {
            return template;
        }
        Matcher matcher = ARGUMENT.matcher(template);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            String replacement;
            if (index < arguments.length) {
                replacement = String.valueOf(arguments[index]);
            } else {
                replacement = matcher.group();
                this.warnOnce("argument:" + key + ":" + index, "Text " + key + " needs argument " + index);
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private void warnOnce(String id, String line) {
        boolean first;
        synchronized (this.warned) {
            first = this.warned.add(id);
        }
        if (first) {
            this.warn.accept(line);
        }
    }

    /**
     * Puts a file of the plugin in the data folder when it is not there, in the language of the plugin when the plugin
     * has one, so that the comments of a config are in the language of the server. Nothing is overwritten.
     *
     * @param name the name of the file, such as config.yml; it is looked for in lang/{language}/, then in lang/en-US/, then in the plugin itself
     * @return true when the file was written
     */
    public boolean install(String name) throws IOException {
        Path target = this.folder.resolve(name).normalize();
        if (!target.startsWith(this.folder.normalize())) {
            throw new IllegalArgumentException("File name leaves the data folder: " + name);
        }
        if (Files.exists(target)) {
            return false;
        }
        for (String path : new String[]{"lang/" + this.language + "/" + name, "lang/" + Locales.EN_US + "/" + name, name}) {
            try (InputStream stream = this.loader.getResourceAsStream(path)) {
                if (stream != null) {
                    Files.createDirectories(target.getParent());
                    Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
                    return true;
                }
            }
        }
        return false;
    }
}
