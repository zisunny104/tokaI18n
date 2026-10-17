package dev.toka.i18n;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MessagesTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private ClassLoader inside(String zh, String en) throws IOException {
        Path root = this.temp.newFolder("jar").toPath();
        Files.createDirectories(root.resolve("lang"));
        Files.writeString(root.resolve("lang/zh-TW.yml"), zh, StandardCharsets.UTF_8);
        Files.writeString(root.resolve("lang/en-US.yml"), en, StandardCharsets.UTF_8);
        return new URLClassLoader(new URL[]{root.toUri().toURL()}, null);
    }

    @Test
    public void argumentsAreReplacedOnceAndInAnyOrder() throws IOException {
        Messages messages = new Messages(this.inside("a:\n  b: '${1} 然後 ${0}'\n", "a:\n  b: '${0} ${1}'\n"), this.temp.newFolder("data").toPath(), "zh_TW", null);
        assertEquals("y 然後 x", messages.text("a.b", "x", "y"));
        assertEquals("${0} 然後 ${1}", messages.text("a.b", "${1}", "${0}"));
    }

    @Test
    public void aMissingTextFallsBackToEnglishAndThenToTheKey() throws IOException {
        List<String> warnings = new ArrayList<>();
        Messages messages = new Messages(this.inside("a: 甲\n", "a: A\nonly: Only\n"), this.temp.newFolder("data").toPath(), "zh-TW", warnings::add);
        assertEquals("甲", messages.text("a"));
        assertEquals("Only", messages.text("only"));
        assertEquals("nothing", messages.text("nothing"));
        messages.text("nothing");
        assertEquals(1, warnings.size());
        assertFalse(messages.has("nothing"));
    }

    @Test
    public void theFileInTheDataFolderWinsAndLinesAreKept() throws IOException {
        Path data = this.temp.newFolder("data").toPath();
        Files.createDirectories(data.resolve("lang"));
        Files.writeString(data.resolve("lang/zh-TW.yml"), "a: 改過\nhelp:\n  - 一 ${0}\n  - 二\n", StandardCharsets.UTF_8);
        Messages messages = new Messages(this.inside("a: 甲\nb: 乙\n", ""), data, "zh-TW", null);
        assertEquals("改過", messages.text("a"));
        assertEquals("乙", messages.text("b"));
        assertEquals(List.of("一 7", "二"), messages.lines("help", 7));
        assertEquals("一 7\n二", messages.text("help", 7));
    }

    @Test
    public void aMissingArgumentIsLeftVisible() throws IOException {
        List<String> warnings = new ArrayList<>();
        Messages messages = new Messages(this.inside("a: '${0}/${1}'\n", ""), this.temp.newFolder("data").toPath(), "zh-TW", warnings::add);
        assertEquals("3/${1}", messages.text("a", 3));
        assertTrue(warnings.get(0).contains("argument 1"));
    }

    @Test
    public void installNeverOverwritesAndPrefersTheLanguageFolder() throws IOException {
        Path jar = this.temp.newFolder("jar").toPath();
        Files.createDirectories(jar.resolve("lang/zh-TW"));
        Files.writeString(jar.resolve("lang/zh-TW/config.yml"), "# 註解\n", StandardCharsets.UTF_8);
        Files.writeString(jar.resolve("config.yml"), "# plain\n", StandardCharsets.UTF_8);
        ClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, null);
        Path data = this.temp.newFolder("data").toPath();
        Messages messages = new Messages(loader, data, "zh-TW", null);
        assertTrue(messages.install("config.yml"));
        assertEquals("# 註解\n", Files.readString(data.resolve("config.yml"), StandardCharsets.UTF_8));
        assertFalse(messages.install("config.yml"));
    }

    @Test
    public void languageNamesAreNormalized() {
        assertEquals("zh-TW", Locales.normalize("ZH_tw", "x"));
        assertEquals("en-US", Locales.normalize(" en-us ", "x"));
        assertEquals("x", Locales.normalize("Cht", "x"));
        assertEquals("x", Locales.normalize(null, "x"));
    }
}
