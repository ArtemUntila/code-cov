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
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Cov1 {

    public static void main(String[] args) throws Exception {
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
        String projectPath = project.getAbsolutePath();

        for (int i = 0; i < scanned.length; i++) {
            String p = projectPath + "\\" + scanned[i];
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
                    System.out.println(name); //"/tests/TestAdder.class"
                    //"tests.TestAdder"
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


                    final IRuntime runtime = new LoggerRuntime();

                    //Instrumenter instr = new Instrumenter(runtime);
                    //InputStream original = new Cov1().getClass().getResourceAsStream(name);
                    //final byte[] instrumented = instr.instrument(original, className);
                    //original.close();

                    final RuntimeData data = new RuntimeData();
                    runtime.startup(data);

                    Class<?> c = cl.loadClass(className);

                    Runnable r = (Runnable) c.getDeclaredConstructor().newInstance();
                    r.run();

                    final ExecutionDataStore executionData = new ExecutionDataStore();
                    final SessionInfoStore sessionInfos = new SessionInfoStore();
                    data.collect(executionData, sessionInfos, false);
                    runtime.shutdown();

                    final CoverageBuilder coverageBuilder = new CoverageBuilder();
                    final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
                    InputStream original = c.getResourceAsStream(name);
                    analyzer.analyzeClass(original, className);
                    original.close();
                }
            }
        }
    }

    public void gC() {
        System.out.println(getClass());
    }

}
