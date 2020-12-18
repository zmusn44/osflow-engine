
package cn.linkey.rule.factory;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class LkJavaFileObject extends SimpleJavaFileObject {
    private CharSequence content;

    public LkJavaFileObject(String className, CharSequence content) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return this.content;
    }
}
