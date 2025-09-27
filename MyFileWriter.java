import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyFileWriter {
    public static void main(String[] args) {
        String data = "Hello, World!";
        String fileName1 = "example.txt";
        String fileName2 = "example2.txt";
        String fileName3 = "example3.txt";
        String fileName4 = "example4.txt";
        String fileName5 = "example5.txt";

        makeHiddenFile();
        makeFileInHiddenFolder();
        printFileSize(fileName5);

        // 1. Using FileWriter
        try (FileWriter writer = new FileWriter(fileName1)) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. Using BufferedWriter
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName2))) {
            bufferedWriter.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. Using FileOutputStream
        try (FileOutputStream outputStream = new FileOutputStream(fileName3)) {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 4. Using BufferedOutputStream
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName4))) {
            bufferedOutputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 5. Using Files (java.nio.file)
        try {
            Files.write(Paths.get(fileName5), data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        printFileSize(fileName1);
    }

    public static void makeHiddenFile() {
        String fileName = ".password.txt";
        String password = "mypasswordis1234";
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName))) {
            bufferedWriter.write(password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makeFileInHiddenFolder() {
        String fileName = "confidential.txt";
        String password = "mypasswordis1234";
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(".classified/" + fileName))) {
            bufferedWriter.write(password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Calculate and print the file size using the File class
    private static void printFileSize(String... fileNames) {
        long totalSize = 0;
        for (String fileName : fileNames) {
            File file = new File(fileName);
            if (file.exists()) {
                totalSize += file.length();
            }
        }
        System.out.println("Total size of all files: " + totalSize + " bytes");
    }

    private static String toString(String fileName) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            while (line != null) {
                content.append(line);
                content.append("\n");
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static String hashFile(String filePath) throws IOException, NoSuchAlgorithmException {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must be a non-empty string");
        }

        Path p = Paths.get(filePath);
        if (!Files.exists(p)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        if (!Files.isRegularFile(p)) {
            throw new IOException("Not a regular file: " + filePath);
        }
        if (!Files.isReadable(p)) {
            throw new IOException("File is not readable: " + filePath);
        }


        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(Path.of(filePath));
            DigestInputStream din = new DigestInputStream(in, sha256)) {

            byte[] buffer = new byte[8192];
            while (din.read(buffer) != -1) {

            }
        }
        byte[] digest = sha256.digest();

        StringBuilder hexString = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}