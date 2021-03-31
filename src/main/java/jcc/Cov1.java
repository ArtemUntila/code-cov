package jcc;

import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.IOException;
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
        URL[] urls = { new URL("jar:file:" + p + "!/")};
        System.out.println(urls[0].getPath());
        URLClassLoader cl = new URLClassLoader(urls);
        while (e.hasMoreElements()) {
           JarEntry je = e.nextElement();
           String name = je.getName();
           if (name.endsWith(".class") && name.contains("tests")) {
               System.out.println(name);
               String className = je.getName().substring(0, je.getName().length() - 6);
               className = className.replace('/', '.');
               System.out.println(className);
               Class<?> c = cl.loadClass(className);
               Arrays.stream(c.getMethods()).forEach(System.out::println);
           }
        }


        /*URL url = new URL("file:D:/UltimateIDEA/code-cov/target/classes/jcc");
        URL[] urls = new URL[]{url};
        System.out.println(
                "Protocol: " + url.getProtocol());
        System.out.println(
                "Filename: " + url.getFile());
        System.out.println(
                "Reference: " + url.getRef());
        System.out.println(
                "External Form: " + url.toExternalForm());
        //System.out.println(project.toString());
        URLClassLoader ucl = new URLClassLoader(urls);
        Class<?> cl = ucl.loadClass("Cov1");
        System.out.println(cl);*/

        /*File f = new File(project, classes[0]);
        String aPath = f.getAbsolutePath();
        System.out.println(aPath);
        System.out.println(f.getName());
        String pathToPackageBase = aPath.substring(0, aPath.length() - f.getName().length());
        System.out.println("pathToPackageBase = " + pathToPackageBase);
        Class<?> clss = new URLClassLoader(
                new URL[]{new File(pathToPackageBase).toURI().toURL()}
        ).loadClass("Adder");
        System.out.println(clss.toString());*/
    }

}
