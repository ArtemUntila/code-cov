package jcc;

import org.apache.tools.ant.DirectoryScanner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
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

        String[] classes = scanner.getIncludedFiles();
        Arrays.stream(classes).forEach(System.out::println);

        String p = project + "\\" + classes[0];
        JarFile jarFile = new JarFile(p);
        Enumeration<JarEntry> e = jarFile.entries();
        System.out.println(p);

        URL[] urls = {new URL("file:" + p)};
        URLClassLoader cl = new URLClassLoader(urls);

        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            String name = je.getName();
            //System.out.println(name);
            if (name.endsWith(".class") && name.contains("tests/")) {
                System.out.println(name);
                //System.out.println(name);
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                //System.out.println(className);
                Class<?> c = cl.loadClass(className);
                System.out.println("\nClass methods:\n");
                for (Method m : c.getMethods()) {
                    for (Annotation a : m.getAnnotations()) {
                        if (a.annotationType().equals(Test.class)) {
                            System.out.println(m.getName());
                        }
                    }
                }
            }
        }
    }

}
