package cl.charlys.cmiconfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

final class YamlScalarEditor {
    private static final DateTimeFormatter BACKUP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private YamlScalarEditor() {}

    static synchronized boolean setScalar(Path file, String dottedPath, String yamlValue) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        String[] wanted = dottedPath.split("\\.");
        List<String> stack = new ArrayList<>();
        int target = -1;

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String trimmed = raw.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) continue;

            int colon = trimmed.indexOf(':');
            if (colon <= 0) continue;
            String key = trimmed.substring(0, colon).trim();
            int indent = leadingSpaces(raw);
            int level = indent / 2;

            while (stack.size() > level) stack.remove(stack.size() - 1);
            if (stack.size() == level) stack.add(key);
            else continue;

            if (stack.size() == wanted.length && matches(stack, wanted)) {
                String after = trimmed.substring(colon + 1).trim();
                if (!after.isEmpty()) {
                    target = i;
                    break;
                }
            }
        }

        if (target < 0) return false;
        backup(file);
        String old = lines.get(target);
        int indent = leadingSpaces(old);
        String key = old.trim().substring(0, old.trim().indexOf(':')).trim();
        lines.set(target, " ".repeat(indent) + key + ": " + yamlValue);

        Path temp = file.resolveSibling(file.getFileName() + ".cmiconfig.tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
    }

    static Optional<String> readScalar(Path file, String dottedPath) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        String[] wanted = dottedPath.split("\\.");
        List<String> stack = new ArrayList<>();
        for (String raw : lines) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) continue;
            int colon = trimmed.indexOf(':');
            if (colon <= 0) continue;
            String key = trimmed.substring(0, colon).trim();
            int level = leadingSpaces(raw) / 2;
            while (stack.size() > level) stack.remove(stack.size() - 1);
            if (stack.size() == level) stack.add(key); else continue;
            if (stack.size() == wanted.length && matches(stack, wanted)) {
                String value = trimmed.substring(colon + 1).trim();
                if (!value.isEmpty()) return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    private static boolean matches(List<String> stack, String[] wanted) {
        if (stack.size() != wanted.length) return false;
        for (int i = 0; i < wanted.length; i++) if (!stack.get(i).equals(wanted[i])) return false;
        return true;
    }

    private static int leadingSpaces(String value) {
        int i = 0;
        while (i < value.length() && value.charAt(i) == ' ') i++;
        return i;
    }

    private static void backup(Path file) throws IOException {
        Path dir = file.getParent().resolve("CMIConfigDialogs-backups");
        Files.createDirectories(dir);
        String stamp = LocalDateTime.now().format(BACKUP_FORMAT);
        Path copy = dir.resolve(file.getFileName() + "." + stamp + ".bak");
        if (!Files.exists(copy)) Files.copy(file, copy);
    }
}
