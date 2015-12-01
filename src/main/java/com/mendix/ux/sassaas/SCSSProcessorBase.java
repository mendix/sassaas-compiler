package com.mendix.ux.sassaas;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SCSSProcessorBase {
    protected final File outputDir;
    protected final File exportFile;
    protected final Map<String, String> mapping;
    protected final Map<String, String> entryPoints;

    public SCSSProcessorBase(String inputZipFilePath, Map<String, String> mapping, Map<String, String> entryPoints) throws Exception {
        outputDir = createTempDir();
        exportFile = File.createTempFile("export", ".zip");
        exportFile.deleteOnExit();
        ZipFile zip = new ZipFile(inputZipFilePath);
        zip.extractAll(outputDir.getAbsolutePath());
        this.mapping = mapping;
        this.entryPoints = entryPoints;
    }

    public void compileAll() throws Exception {
        applyMapping(mapping);
        for (Map.Entry<String, String> entry: entryPoints.entrySet()) {
            compile(entry);
        }
    }

    public File exportZip() throws ZipException, IOException {
        exportFile.delete(); // ZipFile will create this
        zipFolder(outputDir, exportFile.getAbsolutePath());
        return exportFile;
    }

    public File exportCss() throws ZipException, IOException {
        String target = null;
        String content;
        for (Map.Entry<String, String> entry : entryPoints.entrySet()) {
            target = String.format("%s%s%s", outputDir.getAbsoluteFile(), File.separator, entry.getValue());
            content = FileUtils.readFileToString(new File(target));
            FileUtils.write(exportFile, content, true);
        }
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

    protected void applyMapping(Map<String, String> mapping) throws IOException {
        String extensions[] = {"scss"};
        Collection files = FileUtils.listFiles(outputDir, extensions, true);
        for (Object file: files) {
            applyMappingtoFile((File) file, mapping);
        }
    }

    protected void applyMappingtoFile(File file, Map<String, String> mapping) throws IOException {
        String content = FileUtils.readFileToString(file);
        StringBuilder sb = new StringBuilder();
        String lines[] = content.split("\n");
        for (String line: lines) {
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                if (line.startsWith(entry.getKey() + ":")) {
                    line = String.format("%s: \t%s;", entry.getKey(), entry.getValue());
                }
            }
            sb.append(line);
            sb.append("\n");
        }
        FileUtils.writeStringToFile(file, sb.toString());
    }

    protected void compile(Map.Entry<String, String> entryPoint) throws Exception {
        throw new NotImplementedException("No idea how to compile");
    }

    public static File createTempDir() throws IOException {
        File temp = File.createTempFile("template", "");
        temp.delete();
        temp.mkdir();
        return temp;
    }

    protected void zipFolder(File inputDirectory, String outputName) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputName));
        try {
            Collection<File> files = FileUtils.listFiles(inputDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            int truncate = inputDirectory.getPath().length() + 1; // remove extra character being slash /
            for (File file : files){
                ZipEntry entry = new ZipEntry(file.getPath().substring(truncate));
                entry.setSize(file.length());
                entry.setTime(file.lastModified());
                out.putNextEntry(entry);
                FileInputStream in = new FileInputStream(file);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    IOUtils.closeQuietly(in);
                }
                out.closeEntry();
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
