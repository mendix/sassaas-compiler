package com.mendix.ux.sassaas;

import com.vaadin.sass.SassCompiler;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Map;

@Deprecated
public class SCSSProcessorVaadin extends SCSSProcessorBase {

    public SCSSProcessorVaadin(String inputZipFilePath, Map<String, String> mapping, Map<String, String> entryPoints) throws Exception {
        super(inputZipFilePath, mapping, entryPoints);
    }

    @Override
    protected void compile(Map.Entry<String, String> entryPoint) throws Exception {
        File entry = new File(String.format("%s%s%s", outputDir.getAbsoluteFile(), File.separator, entryPoint.getKey()));
        File output = new File(String.format("%s%s%s", outputDir.getAbsoluteFile(), File.separator, entryPoint.getValue()));
        if (!entry.exists()) {
            throw new InvalidParameterException(String.format("Does not exists: %s", entry.getAbsolutePath()));
        }
        File parent = output.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        String args[] = {entry.getAbsolutePath(), output.getAbsolutePath()};
        SassCompiler.main(args);
    }
}
