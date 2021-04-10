package jcc;

import org.apache.tools.ant.DirectoryScanner;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Cov1 {

    public static class MemoryClassLoader extends ClassLoader {

        private final Map<String, byte[]> definitions = new HashMap<>();

        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        public Class<?> loadClass(final String name)
                throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.loadClass(name);
        }

    }

    public void execute(String[] args) throws Exception {
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

        JarFile[] jars = new JarFile[scanned.length];
        String projectPath = project.getAbsolutePath();
        StringBuilder classPath = new StringBuilder(System.getProperty("java.class.path"));
        System.out.println(System.getProperty("java.class.path"));
        for (int i = 0; i < scanned.length; i++) {
            String p = projectPath + "\\" + scanned[i]; // полный путь к jar-файлу
            System.out.println(p);
            jars[i] = new JarFile(p);
            classPath.append(';').append(p);
        }
        System.setProperty("java.class.path", classPath.toString());
        //System.out.println(System.getProperty("java.class.path"));
        //MemoryClassLoader mCL = new MemoryClassLoader();
        //mCL.addDefinition(".tests.TestAdder", null);
        //mCL.addDefinition(".classes.Adder", null);
        //Class<?> c = mCL.loadClass(".tests.TestAdder");
        //Runnable r = (Runnable) c.getDeclaredConstructor().newInstance();
        //r.run();

        final IRuntime runtime = new LoggerRuntime();

        Instrumenter instr = new Instrumenter(runtime);
        InputStream original = this.getClass().getClassLoader().getResourceAsStream("/classes/Adder.class"); // getClass() isn't static
        if (original == null) {
            System.err.println("OF COURSE");
            //final byte[] instrumented = instr.instrument(original, "classes.Adder");
        } else {
            System.out.println("NO WAY");
            original.close();
        }
        System.out.println(System.class.getClassLoader().getResource("."));

        /*ClassLoader cl = ClassLoader.getSystemClassLoader();
        cl.loadClass("Adder");*/
        /*for (JarFile jar : jars) {
            Enumeration<JarEntry> e = jar.entries();
            while (e.hasMoreElements()) {Ф
                JarEntry je = e.nextElement();
                //String name = je.getName();
                System.out.println(je);*/
                /*System.out.println(name + " - " + getCanonicalName(name));
                if (name.endsWith(".class") && name.contains("tests/")) {
                    System.out.println(name); //"/tests/TestAdder.class"
                    String className = name.substring(0, je.getName().length() - 6).replace('/', '.');
                    System.out.println(className);
                    /*Arrays.stream(c.getMethods()).forEach(System.out::println);
                    for (Method m : c.getMethods()) {
                        for (Annotation a : m.getAnnotations()) {
                            if (a.annotationType().equals(Test.class)) {
                                System.out.println(m.getName());
                            }
                        }
                    }*/
                    //System.out.println("\nRunning test...\n");


                    /*final IRuntime runtime = new LoggerRuntime();

                    Instrumenter instr = new Instrumenter(runtime);
                    InputStream original = System.class.getResourceAsStream(className + ".class"); // getClass() isn't static
                    final byte[] instrumented = instr.instrument(original, className);
                    original.close();

                    final RuntimeData data = new RuntimeData();
                    runtime.startup(data);

                    Class<?> c = cl.loadClass(className);
                    System.out.println(ClassLoader.getSystemClassLoader());
                    Runnable r = (Runnable) c.getDeclaredConstructor().newInstance();
                    r.run();

                    final ExecutionDataStore executionData = new ExecutionDataStore();
                    final SessionInfoStore sessionInfos = new SessionInfoStore();
                    data.collect(executionData, sessionInfos, false);
                    runtime.shutdown();

                    final CoverageBuilder coverageBuilder = new CoverageBuilder();
                    final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
                    original = c.getResourceAsStream(name);
                    analyzer.analyzeClass(original, className);
                    original.close();*/
                //}
            //}
        //}
    }

    public static void main(String[] args) throws Exception{
        new Cov1().execute(args);
    }
}
