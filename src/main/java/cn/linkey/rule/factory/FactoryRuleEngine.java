
package cn.linkey.rule.factory;

import org.apache.commons.lang3.StringUtils;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class FactoryRuleEngine {
    private static ClassLoader parentClassLoader = FactoryRuleEngine.class.getClassLoader();
    private static String classpath = null;
    private static String compilePath = "";

    static {
        getCompilePath();
        buildClassPath();
    }

    public FactoryRuleEngine() {

    }

    /**
     * 获取项目的编译路径
     * @return 编译类路径
     */
    public static String getCompilePath() {
        if (StringUtils.isNotBlank(compilePath)) {
            return compilePath;
        } else {
            URL url = FactoryRuleEngine.class.getResource("");
                if (url == null) {
                    compilePath = FactoryRuleEngine.class.getResource("/").getPath();
                } else {
                    compilePath = FactoryRuleEngine.class.getResource("").getPath();
                }
                int spos = compilePath.indexOf("/classes/");
                if (spos != -1) {
                    compilePath = compilePath.substring(1, spos + 9);

                compilePath = "/" + compilePath;
            }

            return compilePath;
        }
    }

    /**
     * 获取项目的构建路径
     */
    private static void buildClassPath() {
        classpath = null;
        StringBuilder sb = new StringBuilder();
        if (getAppServerid().equals("TOMCAT")) {
            URL[] arrayOfURL;
            int j = (arrayOfURL = ((URLClassLoader)parentClassLoader).getURLs()).length;
            for(int i = 0; i < j; ++i) {
                URL url = arrayOfURL[i];
                String p = url.getFile();
                sb.append(p).append(File.pathSeparator);
            }
        } else {
            String libPath = compilePath.replace("/classes/", "/lib/");
            sb.append(compilePath).append(File.pathSeparator);
            String configLibJarList = "";
            if (StringUtils.isBlank(configLibJarList)) {
                sb.append(listJarFile(libPath));
            } else {
                sb.append(configLibJarList);
            }
        }

        classpath = sb.toString();
    }

    /**
     * 遍历项目jar包
     * @param path
     * @return
     */
    private static StringBuilder listJarFile(String path) {
        StringBuilder sb = new StringBuilder();
        File file = new File(path);
        if (file.isDirectory()) {
            File[] dirFile = file.listFiles();
            File[] arrayOfFile = dirFile;
            int dirFileLength = dirFile.length;

            for(int i = 0; i < dirFileLength; ++i) {
                File f = arrayOfFile[i];
                if (!f.isDirectory() && f.getName().endsWith(".jar")) {
                    sb.append(f.getAbsolutePath()).append(File.pathSeparator);
                }
            }
        }
        return sb;
    }

    /**
     * 获取服务器类型
     * @return 服务器类型  TOMCAT、JBOSS、OTHER
     */
    public static String getAppServerid() {
        String serverid = "";
        if (StringUtils.isNotBlank(serverid)) {
            return serverid;
        } else {
            String classLoader = FactoryRuleEngine.class.getClassLoader().toString().toLowerCase();
            if (classLoader.indexOf("moduleclassloader") != -1) {
                return "JBOSS";
            } else {
                return classLoader.indexOf("org.apache.catalina.loader") != -1 ? "TOMCAT" : "OTHER";
            }
        }
    }

    /**
     * 获取java对象
     * @param fullClassName Java类全称
     * @param javaCode      Java源码
     * @param creatClass    是否生成类文件true表示是，false表示否
     * @return 返回Java对象
     */
    public static Object javaCodeToObject(String fullClassName, String javaCode, boolean creatClass) {
        Object instance = null;
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector();
        LkClassFileManager fileManager = new LkClassFileManager(compiler.getStandardFileManager(diagnostics, (Locale)null, (Charset)null), creatClass);
        List<JavaFileObject> jfiles = new ArrayList();
        jfiles.add(new LkJavaFileObject(fullClassName, javaCode));
        List<String> options = new ArrayList();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-d");
        options.add(compilePath);
        options.add("-classpath");
        options.add(classpath);
        CompilationTask task = compiler.getTask((Writer)null, fileManager, diagnostics, options, (Iterable)null, jfiles);
        boolean success = task.call();
        if (success) {
            LkClassLoader lkClassLoader = new LkClassLoader(parentClassLoader);
            Class clazz;
            if (!creatClass) {
                LkJavaClassObject jco = fileManager.getJavaClassObject();
                clazz = lkClassLoader.loadClass(fullClassName, jco);
            } else {
                clazz = lkClassLoader.loadClassByName(fullClassName);
            }

            try {
                instance = clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String error = "";
            Diagnostic diagnostic;
            for(Iterator iterator = diagnostics.getDiagnostics().iterator(); iterator.hasNext(); error = error + compilePrint(diagnostic)) {
                diagnostic = (Diagnostic)iterator.next();
            }
           /* for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                error = error + compilePrint(diagnostic);
            }*/
        }
        return instance;
    }

    /**
     * 编译java代码
     * @param fullClassName  Java类全称
     * @param javaCode     Java源码
     * @param creatClass 是否生成类文件true表示是，false表示否
     * @return 返回操作结果
     */
    public static String compileJavaCode(String fullClassName, String javaCode, boolean creatClass) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector();
        StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, (Locale)null, (Charset)null);
        LkClassFileManager fileManager = new LkClassFileManager(fm, creatClass);
        List<JavaFileObject> jfiles = new ArrayList();
        jfiles.add(new LkJavaFileObject(fullClassName, javaCode));
        List<String> options = new ArrayList();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-d");
        options.add(compilePath);
        options.add("-classpath");
        options.add(classpath);
        CompilationTask task = compiler.getTask((Writer)null, fileManager, diagnostics, options, (Iterable)null, jfiles);
        boolean success = task.call();
        if (success) {
            LkClassLoader lkClassLoader = new LkClassLoader(parentClassLoader);
            if (!creatClass) {
                LkJavaClassObject jco = fileManager.getJavaClassObject();
                lkClassLoader.loadClass(fullClassName, jco);
            } else {
                lkClassLoader.loadClassByName(fullClassName);
            }

            return "1";
        } else {
            String error = "";

            Diagnostic diagnostic;
            for(Iterator iterator = diagnostics.getDiagnostics().iterator(); iterator.hasNext(); error = error + compilePrint(diagnostic)) {
                diagnostic = (Diagnostic)iterator.next();
            }

            /*for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                error = error + compilePrint(diagnostic);
            }*/

            return encode(error);
        }
    }

    /**
     * 开发者模式
     * @param fullClassPath 绝对路径
     * @return 加载的对象
     */
    public static Object loadClassForDevModel(String fullClassPath) {
        LkClassLoader lkClassLoader = new LkClassLoader(parentClassLoader);
        Class clazz = lkClassLoader.loadClassByName(fullClassPath);
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String compilePrint(Diagnostic diagnostic) {
        System.out.println("Code:" + diagnostic.getCode());
        System.out.println("Kind:" + diagnostic.getKind());
        System.out.println("Position:" + diagnostic.getPosition());
        System.out.println("Start Position:" + diagnostic.getStartPosition());
        System.out.println("End Position:" + diagnostic.getEndPosition());
        System.out.println("Source:" + diagnostic.getSource());
        System.out.println("Message:" + diagnostic.getMessage((Locale)null));
        System.out.println("LineNumber:" + diagnostic.getLineNumber());
        System.out.println("ColumnNumber:" + diagnostic.getColumnNumber());
        StringBuffer res = new StringBuffer();
        res.append("代码:[" + diagnostic.getCode() + "]<br>");
        res.append("类型:[" + diagnostic.getKind() + "]<br>");
        res.append("消息:[" + diagnostic.getMessage((Locale)null) + "]<br>");
        res.append("<font style='color:red' ><b>错误行:[" + diagnostic.getLineNumber() + "]</b></font><br>");
        res.append("错误列:[" + diagnostic.getColumnNumber() + "]<br>");
        return res.toString();
    }


    public static String encode(String str) {
        try {
            String code = URLEncoder.encode(str, "utf-8");
            code = code.replace("+", "%20");
            return code;
        } catch (Exception e) {
            System.out.println("字符串编码失败(" + str + ")");
            return str;
        }
    }
}
