package ru.job4j;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileSearcher {
    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Missing same arguments");
        }

        String directory = null;
        String namePattern = null;
        String searchType = null;
        String outputFile = null;

        for (String arg : args) {
            if (arg.startsWith("-d=")) {
                directory = arg.substring(3);
            }
            if (arg.startsWith("-n=")) {
                namePattern = arg.substring(3);
            }
            if (arg.startsWith("-t=")) {
                searchType = arg.substring(3);
            }
            if (arg.startsWith("-o=")) {
                outputFile = arg.substring(3);
            }
        }
        if (directory == null || namePattern == null || searchType == null || outputFile == null) {
            throw new IllegalArgumentException(
                    "Arguments much be FileSearcher: -d=<directory> -n=<name|mask|regex> -t=<name|mask|regex> -o=<output_file>");
        }
        try {
            List<String> list = searchFile(directory, namePattern, searchType);
            writeResultsToFile(list, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<String> searchFile(String directory, String namePattern, String searchType) throws IOException {
        ArrayList<String> result = new ArrayList<>();
        Path startPath = Paths.get(directory);
        Files.walkFileTree(startPath,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fileName = file.getFileName().toString();
                        if (check(fileName, namePattern, searchType)) {
                            result.add(file.toString());
                        }
                        return super.visitFile(file, attrs);
                    }
                });

        return result;
    }

    private static boolean check(String fileName, String namePattern, String searchType) {
        switch (searchType) {
            case "name" -> {
                return fileName.equals(namePattern);
            }
            case "mask" -> {
                String regex = namePattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
                return fileName.matches(regex);
            }
            case "regex" -> {
                return Pattern.compile(namePattern).matcher(fileName).matches();
            }
            default -> throw new IllegalArgumentException("Invalid searchType");
        }
    }

    private static void writeResultsToFile(List<String> results, String outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            for (String line : results) {
                writer.println(line);
            }
        }
    }
}

