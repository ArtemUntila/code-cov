package jcc;

import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.Arrays;

public class Cov1 {

    public static void main(String[] args) {
        if (args == null || args.length < 1)
            throw new IllegalArgumentException("Incorrect data entered");

        File project = new File(args[0]);

        if (!project.exists() || !project.isDirectory())
            throw new IllegalArgumentException("Project doesn't exist");

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{"**\\*.class"});
        scanner.setBasedir(project);
        scanner.scan();

        String[] classes = scanner.getIncludedFiles();
        Arrays.stream(classes).forEach(System.out::println);
    }

}
