package cn.linkey.rule.factory;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class LkJavaClassObject extends SimpleJavaFileObject {
    protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    public LkJavaClassObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    public byte[] getBytes() {
        return this.bos.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return this.bos;
    }
}
