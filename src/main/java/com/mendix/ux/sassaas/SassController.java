package com.mendix.ux.sassaas;

import com.mendix.ux.sassaas.specs.api.SassApi;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/sass")
public class SassController implements SassApi {

    @Autowired
    HttpServletResponse response;

    @Override
    @RequestMapping(method = RequestMethod.GET)
    public File compileSass(@RequestParam(value = "variables", required = false) String variables, @RequestParam(value = "entrypoints", required = false) String entrypoints, @RequestParam(value = "output", required = false) String output) throws Exception {
        SCSSProcessorBase processor = null;

        File inputFile = writeInputStreamToFile(getClass().getResourceAsStream("/default-theme.zip"));
        try {
            Map<String, String> mapping = convertFields(variables);

            Map<String, String> entryPoints = convertFields(entrypoints);

            processor = new SCSSProcessorJsass(inputFile.getAbsolutePath(), mapping, entryPoints);

            processor.compileAll();
            if (response != null) {
                OutputStream outputStream = response.getOutputStream();
                if ("css".equals(output)) {
                    serveCss(processor, outputStream, "theme.css");
                } else {
                    serveZip(processor, outputStream, "theme.zip");
                }
                response.flushBuffer();
                IOUtils.closeQuietly(outputStream);
            }
        } finally {
            if (processor != null) processor.cleanup();
            if (inputFile.exists()) inputFile.delete();
        }
        return null;
    }

    private void serveCss(SCSSProcessorBase processor, OutputStream out, String outputFilename) throws ZipException, IOException {
        File exported = processor.exportCss();
        response.setContentType("text/css");
        response.setHeader("Content-Disposition", String.format("attachment; filename=windows-%s", outputFilename));
        out.write(FileUtils.readFileToByteArray(exported));
    }

    private void serveZip(SCSSProcessorBase processor, OutputStream out, String outputFilename) throws ZipException, IOException {
        File exported = processor.exportZip();
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", String.format("attachment; filename=mendix-%s", outputFilename));
        out.write(FileUtils.readFileToByteArray(exported));
    }

    private File writeInputStreamToFile(InputStream inputStream) throws IOException {
        File outfile = File.createTempFile("incoming", ".file");
        outfile.deleteOnExit();
        FileOutputStream outStream = new FileOutputStream(outfile);
        IOUtils.copy(inputStream, outStream);
        IOUtils.closeQuietly(outStream);
        return outfile;
    }

    private Map<String, String> convertFields(String variables) {
        Map<String, String> mapping = new HashMap<String, String>();
        if (variables != null) {
            String[] elements = variables.split("\\|");
            for (String element: elements) {
                String[] entries = element.split("@");
                if (entries.length >= 2) {
                    mapping.put(entries[0], entries[1]);
                }
            }
        }
        return mapping;
    }
}
