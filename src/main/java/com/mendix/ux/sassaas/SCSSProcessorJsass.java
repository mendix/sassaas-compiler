package com.mendix.ux.sassaas;

import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Map;

public class SCSSProcessorJsass extends SCSSProcessorBase {

    public SCSSProcessorJsass(String inputZipFilePath, Map<String, String> mapping, Map<String, String> entryPoints) throws Exception {
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
        URI inputURI = entry.toURI();
        URI outputURI = output.toURI();
        Compiler compiler = new Compiler();
        Options options = new Options();
        Output result = compiler.compileFile(inputURI, outputURI, options);
        FileUtils.writeStringToFile(output, result.getCss());
    }
}
