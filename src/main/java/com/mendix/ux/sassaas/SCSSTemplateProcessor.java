package com.mendix.ux.sassaas;

import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SCSSTemplateProcessor {
    private final File outputDir;
    private final File exportFile;
    private final Map<String, String> mapping;
    private final List<String> entryPoints;

    public SCSSTemplateProcessor(String inputZipFilePath, Map<String, String> mapping, List<String> entryPoints) throws Exception {
        outputDir = createTempDir();
        exportFile = File.createTempFile("export", ".zip");
        exportFile.deleteOnExit();
        ZipFile zip = new ZipFile(inputZipFilePath);
        zip.extractAll(outputDir.getAbsolutePath());
        this.mapping = mapping;
        this.entryPoints = entryPoints;
    }

    public File export() throws Exception {
        applyMapping(mapping);
        for (String entryPoint: entryPoints) {
            compile(entryPoint);
        }

        exportFile.delete(); // ZipFile will create this
        ZipFile zip = new ZipFile(exportFile);
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setIncludeRootFolder(false);
        zip.createZipFileFromFolder(outputDir.getAbsolutePath(), zipParameters, false, -1);
        return exportFile;
    }

    public void cleanup() throws IOException {
        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
        if (exportFile.exists()) {
            exportFile.delete();
        }
    }

    private void applyMapping(Map<String, String> mapping) throws IOException {
        String extensions[] = {"scss"};
        Collection files = FileUtils.listFiles(outputDir, extensions, true);
        for (Object file: files) {
            applyMappingtoFile((File) file, mapping);
        }
    }

    private void applyMappingtoFile(File file, Map<String, String> mapping) throws IOException {
        String content = FileUtils.readFileToString(file);
        StringBuilder sb = new StringBuilder();
        String lines[] = content.split("\n");
        for (String line: lines) {
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                if (line.startsWith(entry.getKey())) {
                    line = String.format("%s: \t%s;", entry.getKey(), entry.getValue());
                }
            }
            sb.append(line);
            sb.append("\n");
        }
        FileUtils.writeStringToFile(file, sb.toString());
    }

    private void compile(String entryPoint) throws Exception {
        String entry = String.format("%s%s%s", outputDir.getAbsoluteFile(), File.separator, entryPoint);
        String output = entry.replaceAll(".scss$", ".css");
        if (output.equals(entry)) {
            throw new InvalidParameterException(String.format("EntryPoint must be a relative path to a .scss file: %s ", entry));
        }
        URI inputURI = new File(entry).toURI();
        URI outputURI = new File(output).toURI();
        Compiler compiler = new Compiler();
        Options options = new Options();
        options.setPrecision();
        compiler.compileFile(inputURI, outputURI, options);
    }

    public static File createTempDir() throws IOException {
        File temp = File.createTempFile("template", "");
        temp.delete();
        temp.mkdir();
        return temp;
    }
}
