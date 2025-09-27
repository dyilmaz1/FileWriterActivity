import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileWriterTester {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("hashfile-tests-");
        System.out.println("Temp dir: " + tmp);

        testEmptyFile(tmp);
        testUnicodeAndEmojis(tmp);
        testLargeFileStreaming(tmp);
        testNonExistent(tmp);

        System.out.println();
        System.out.printf("RESULT: %d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }

    private static void testEmptyFile(Path tmp) throws Exception {
        String name = "Empty file SHA-256";
        Path f = tmp.resolve("empty.txt");
        Files.createFile(f);
        String actual = MyFileWriter.hashFile(f.toString());
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        if (expected.equals(actual)) {
            passed++; System.out.println("PASS: " + name);
        } else {
            failed++; System.out.println("FAIL: " + name + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void testUnicodeAndEmojis(Path tmp) throws Exception {
        String name = "Unicode/emoji content";
        String text = "こんにちは🌟\nCafé 🚀\n汉字\n";
        Path f = tmp.resolve("unicode.txt");
        Files.write(f, text.getBytes(StandardCharsets.UTF_8));
        String actual = MyFileWriter.hashFile(f.toString());
        String expected = sha256Hex(text.getBytes(StandardCharsets.UTF_8));
        if (expected.equals(actual)) {
            passed++; System.out.println("PASS: " + name);
        } else {
            failed++; System.out.println("FAIL: " + name + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void testLargeFileStreaming(Path tmp) throws Exception {
        String name = "Large file (~10MiB) streamed hash";
        Path f = tmp.resolve("big.bin");
        byte[] chunk = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8); // 32 B
        int targetBytes = 10 * 1024 * 1024; // 10 MiB
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(f))) {
            int written = 0;
            while (written < targetBytes) {
                int toWrite = Math.min(chunk.length, targetBytes - written);
                bos.write(chunk, 0, toWrite);
                written += toWrite;
            }
        }
        String actual = MyFileWriter.hashFile(f.toString());
        String expected = sha256HexOfFile(f);
        if (expected.equals(actual)) {
            passed++; System.out.println("PASS: " + name);
        } else {
            failed++; System.out.println("FAIL: " + name + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void testNonExistent(Path tmp) {
        String name = "Non-existent file throws FileNotFoundException";
        Path missing = tmp.resolve("does-not-exist.txt");
        try {
            MyFileWriter.hashFile(missing.toString());
            failed++;
            System.out.println("FAIL: " + name + " (no exception thrown)");
        } catch (java.io.FileNotFoundException e) {
            passed++;
            System.out.println("PASS: " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("FAIL: " + name + " (wrong exception: " + t.getClass().getSimpleName() + ")");
        }
    }
    
    private static String sha256Hex(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(bytes);
        StringBuilder sb = new StringBuilder(dig.length * 2);
        for (byte b : dig) {
            String h = Integer.toHexString(b & 0xff);
            if (h.length() == 1) sb.append('0');
            sb.append(h);
        }
        return sb.toString();
    }

    private static String sha256HexOfFile(Path p) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(p); DigestInputStream din = new DigestInputStream(in, md)) {
            byte[] buf = new byte[8192];
            while (din.read(buf) != -1) {}
        }
        return sha256Hex(md.digest());
    }
}
