
package cn.linkey.rule.factory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;

public class LkClassLoader extends URLClassLoader {
    public LkClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public Class findClassByClassName(String className) throws ClassNotFoundException {
        return this.findClass(className);
    }

    public Class loadClass(String fullName, LkJavaClassObject jco) {
        try {
            byte[] classData = jco.getBytes();
            return this.defineClass(fullName, classData, 0, classData.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Class loadClassByName(String fullName) {
        byte[] classData = this.getBytes(fullName);
        return this.defineClass(fullName, classData, 0, classData.length);
    }

    public byte[] getBytes(String classPath) {
        classPath = classPath.replace(".", "/") + ".class";
        classPath = FactoryRuleEngine.getCompilePath() + classPath;
        File file = new File(classPath);
        long len = file.length();
        byte[] raw = new byte[(int)len];

        try {
            FileInputStream fin = new FileInputStream(file);
            int r = fin.read(raw);
            if ((long)r != len) {
                System.out.println(classPath + "read class file error! " + classPath);
            }

            return raw;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(classPath + " can't read class file ! " + classPath);
            return null;
        }
    }
}
