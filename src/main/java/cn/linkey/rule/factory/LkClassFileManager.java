
package cn.linkey.rule.factory;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;

public class LkClassFileManager extends ForwardingJavaFileManager {
    private LkJavaClassObject jclassObject;
    private boolean creatClass;

    public LkJavaClassObject getJavaClassObject() {
        return this.jclassObject;
    }

    public LkClassFileManager(StandardJavaFileManager standardManager, boolean creatClass) {
        super(standardManager);
        this.creatClass = creatClass;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        this.jclassObject = new LkJavaClassObject(className, kind);
        return (JavaFileObject)(this.creatClass ? super.fileManager.getJavaFileForOutput(location, className, kind, sibling) : this.jclassObject);
    }
}
