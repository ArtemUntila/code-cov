package jcc;

import org.apache.tools.ant.DirectoryScanner;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Cov1 {

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        if (args == null || args.length < 1)
            throw new IllegalArgumentException("Incorrect data entered");

        File project = new File(args[0]);

        if (!project.exists())
            throw new IllegalArgumentException("Project doesn't exist");

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{"**\\*.jar"});
        scanner.setBasedir(project);
        scanner.scan();

        String[] scanned = scanner.getIncludedFiles();

        URL[] urls = new URL[scanned.length];
        List<JarFile> jars = new LinkedList<>();

        for (int i = 0; i < scanned.length; i++) {
            String p = project + "\\" + scanned[i];
            System.out.println(p);
            urls[i] = new URL("file:" + p);
            jars.add(new JarFile(p));
        }

        URLClassLoader cl = new URLClassLoader(urls);

        for (JarFile jar : jars) {
            Enumeration<JarEntry> e = jar.entries();
            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                String name = je.getName();
                if (name.endsWith(".class") && name.contains("tests/")) {
                    System.out.println(name);
                    String className = name.substring(0, je.getName().length() - 6).replace('/', '.');
                    System.out.println(className);
                    Class<?> c = cl.loadClass(className);
                    /*Arrays.stream(c.getMethods()).forEach(System.out::println);
                    for (Method m : c.getMethods()) {
                        for (Annotation a : m.getAnnotations()) {
                            if (a.annotationType().equals(Test.class)) {
                                System.out.println(m.getName());
                            }
                        }
                    }*/
                    System.out.println("\nRunning test...\n");
                    Result result = JUnitCore.runClasses(c);
                    result.getFailures().forEach(System.out::println);
                }
            }
        }
    }

}
