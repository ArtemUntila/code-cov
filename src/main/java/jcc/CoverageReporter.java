package jcc;

import org.apache.tools.ant.DirectoryScanner;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Принимаемый проект скомпилирован,
 * а также содержит классы тестов в каких-либо .jar-файлах
 * Варианты добавления тестовых классов в .jar-файлы (если директория с тестами помечена, как тестовая):
 * https://maven.apache.org/plugins/maven-jar-plugin/examples/create-test-jar.html
 * Тесты принимаемого проекта реализуют интерфейс Runnable.
 *
 * В дальнейшем, при интеграции прототипа в KEX, зависимоть junit будет убрана.
 */

public class CoverageReporter {

    private final URLClassLoader baseClassLoader;

    private final MemoryClassLoader instrAndTestsClassLoader;

    private final List<JarFile> jarFiles;

    public CoverageReporter(String[] args) throws IOException {

        if (args == null || args.length < 1) throw new IllegalArgumentException("Incorrect data entered");

        String projectDirPath = args[0];
        File projectDir = new File(projectDirPath);

        if (!projectDir.exists()) throw new IllegalArgumentException("Project doesn't exist");

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{"**\\*.jar"});
        scanner.setBasedir(projectDir);
        scanner.scan();
        String[] scannedJarPaths = scanner.getIncludedFiles();
        int length = scannedJarPaths.length;

        URL[] scannedJarURLs = new URL[length];
        List<JarFile> scannedJarFiles = new ArrayList<>(length);
        for (int i = 0; i < scannedJarPaths.length; i++) {
            String absolutePath = projectDirPath + "\\" + scannedJarPaths[i];
            scannedJarURLs[i] = new URL("file:" + absolutePath);
            scannedJarFiles.add(new JarFile(absolutePath));
        }

        this.baseClassLoader = new URLClassLoader(scannedJarURLs);
        this.instrAndTestsClassLoader = new MemoryClassLoader(baseClassLoader);
        this.jarFiles = scannedJarFiles;

    }

    public void execute() throws Exception {

        final IRuntime runtime = new LoggerRuntime();

        List<String> classes = new ArrayList<>();

        List<String> tests = new ArrayList<>();

        InputStream original;

        for (JarFile jarFile : jarFiles) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                String name = jarEntry.getName();
                if (name.endsWith(".class")) {
                    String fullyQualifiedName = getFullyQualifiedName(name);
                    System.out.println(name + " - " + fullyQualifiedName);
                    original = baseClassLoader.getResourceAsStream(name);
                    if (name.contains("tests/")) {
                        instrAndTestsClassLoader.addDefinition(fullyQualifiedName, original.readAllBytes());
                        tests.add(name);
                    } else {
                        final Instrumenter instr = new Instrumenter(runtime);
                        final byte[] instrumented = instr.instrument(original, fullyQualifiedName);
                        original.close();
                        instrAndTestsClassLoader.addDefinition(fullyQualifiedName, instrumented);
                        classes.add(name);
                    }
                }
            }
        }

        final RuntimeData data = new RuntimeData();
        runtime.startup(data);

        System.out.println("Running tests...\n");
        for (String testName : tests) {
            final Class<?> testClass = instrAndTestsClassLoader.loadClass(getFullyQualifiedName(testName));
            final Runnable targetInstance = (Runnable) testClass.getDeclaredConstructor().newInstance();
            targetInstance.run();
        }

        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

        for (String className : classes) {
            original = baseClassLoader.getResourceAsStream(className);
            analyzer.analyzeClass(original, getFullyQualifiedName(className));
            original.close();
        }

        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            System.out.printf("%nCoverage of class %s:%n", cc.getName());

            printCounter("instructions", cc.getInstructionCounter());
            printCounter("branches", cc.getBranchCounter());
            printCounter("lines", cc.getLineCounter());
            printCounter("methods", cc.getMethodCounter());
            printCounter("complexity", cc.getComplexityCounter());
        }

    }

    private String getFullyQualifiedName(String name) {
        return name.substring(0, name.length() - 6).replace('/', '.');
    }

    private void printCounter(final String unit, final ICounter counter) {
        final Integer covered = counter.getCoveredCount();
        final Integer total = counter.getTotalCount();
        System.out.printf("%s of %s %s covered%n", covered, total, unit);
    }

    public static void main(String[] args) throws Exception{
        new CoverageReporter(args).execute();
    }

    public static class MemoryClassLoader extends ClassLoader {

        private final URLClassLoader parent;

        public MemoryClassLoader(URLClassLoader parent) {
            this.parent = parent;
        }

        private final Map<String, byte[]> definitions = new HashMap<>();

        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return parent.loadClass(name);
        }

    }
}
