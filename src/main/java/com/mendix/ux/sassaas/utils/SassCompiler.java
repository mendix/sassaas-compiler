package com.mendix.ux.sassaas.utils;

import com.mendix.ux.sassaas.specs.model.KeyValue;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SassCompiler {

    private final Logger logger = LoggerFactory.getLogger(SassCompiler.class);
    private File workingDirectory;
    private List<KeyValue> targets;

    public SassCompiler(File inputZip, File workingDirectory, File variables, List<KeyValue> targets) throws Exception {
        this.workingDirectory = workingDirectory;
        if (targets == null) {
            this.targets = new LinkedList<KeyValue>();
            KeyValue lib = new KeyValue();
            lib.setKey("/styles/sass/lib/lib.scss");
            lib.setValue("/styles/css/lib/lib.css");
            this.targets.add(lib);
            KeyValue custom = new KeyValue();
            custom.setKey("/styles/sass/custom/custom.scss");
            custom.setValue("/styles/css/custom/custom.css");
            this.targets.add(custom);
        } else {
            this.targets = targets;
        }
        extractBase(inputZip);
        applyVariables(variables);
        for (KeyValue item: this.targets)
            compile(item);
    }

    private void compile(KeyValue target) throws Exception {
        File entry = new File(String.format("%s%s%s", this.workingDirectory.getAbsoluteFile(), File.separator, target.getKey()));
        File output = new File(String.format("%s%s%s", this.workingDirectory.getAbsoluteFile(), File.separator, target.getValue()));
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

    private void applyVariables(File variables) throws IOException {
        File customVariableFile = getCustomVariableFile();
        JSONTokener jsonTokener = new JSONTokener(new FileInputStream(variables));
        JSONObject jsonObject = new JSONObject(jsonTokener);
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (String key: jsonObject.keySet())
            map.put(key, jsonObject.getString(key));
        applyVariablesInFile(customVariableFile, map);
    }

    private void applyVariablesInFile(File file, Map<String, String> variables) throws IOException {
        String content = FileUtils.readFileToString(file);
        StringBuilder sb = new StringBuilder();
        String lines[] = content.split("\n");
        for (String line: lines) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                if (line.startsWith("$" + entry.getKey() + ":")) {
                    line = String.format("$%s: \t%s;", entry.getKey(), entry.getValue());
                }
            }
            sb.append(line);
            sb.append("\n");
        }
        FileUtils.writeStringToFile(file, sb.toString());
    }

    private void extractBase(File inputBase) throws IOException, ZipException {
        if (this.workingDirectory.isDirectory()) {
            logger.warn("Not unpacking base theme because working directory already exists: " + workingDirectory);
            return;
        }
        ZipFile zip = new ZipFile(inputBase.getAbsoluteFile());
        zip.extractAll(this.workingDirectory.getAbsolutePath());
    }

    private File getCustomVariableFile() {
        String extensions[] = {"scss"};
        Collection files = FileUtils.listFiles(this.workingDirectory, extensions, true);

        File current;
        for (Object file: files) {
            current = (File) file;
            if (current.getName().equals("_custom-variables.scss"))
                return current;
        }
        logger.warn("Failed to find _custom-variables.scss");
        return null;
    }

    public void exportCss(String outputPath) throws IOException {
        String target;
        String content;
        File outputFile = new File(outputPath);

        for (KeyValue item : this.targets) {
            target = String.format("%s%s%s", this.workingDirectory.getAbsoluteFile(), File.separator, item.getValue());
            content = FileUtils.readFileToString(new File(target));
            FileUtils.write(outputFile, content, true);
        }
    }

    public void exportZip(String outputPath) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputPath));
        try {
            Collection<File> files = FileUtils.listFiles(this.workingDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            int truncate = this.workingDirectory.getPath().length() + 1; // remove extra character being slash /
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
