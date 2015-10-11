package com.mendix.ux.sassaas;

import com.mendix.ux.sassaas.specs.api.SassApi;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/sass")
public class SassController implements SassApi {

    @Autowired
    HttpServletResponse response;

    @Override
    @RequestMapping(method = RequestMethod.POST)
    public File compileSass(@RequestPart("template") MultipartFile fileDetail, @RequestParam(value = "variables", required = false) String variables, @RequestParam(value = "entrypoints", required = false) String entrypoints) throws Exception {
        File inputFile = writeInputStreamToFile(fileDetail.getInputStream());
        Map<String, String> mapping = convertFields(variables);
        List<String> entryPoints = convertEntryPoints(entrypoints);
        SCSSTemplateProcessor processor = new SCSSTemplateProcessor(inputFile.getAbsolutePath(), mapping, entryPoints);
        File exported = processor.export();
        if (response != null) {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", String.format("attachment; filename=compiled-%s", fileDetail.getOriginalFilename()));
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(FileUtils.readFileToByteArray(exported));
            IOUtils.closeQuietly(outputStream);
        }
        processor.cleanup();
        inputFile.delete();
        if (response != null) {
            response.flushBuffer();
        }
        return null;
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
            JSONArray jsonArray = new JSONArray(variables);
            JSONObject jsonObject;
            String key = null, value;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                for (String k : jsonObject.keySet()) {
                    key = k;
                }
                value = jsonObject.getString(key);
                mapping.put(key, value);
            }
        }
        return mapping;
    }

    private List<String> convertEntryPoints(String entryPoints) {
        List<String> entries = new ArrayList<String>();
        if (entryPoints != null) {
            JSONArray jsonArray = new JSONArray(entryPoints);
            for (int i = 0; i < jsonArray.length(); i++) {
                entries.add(jsonArray.getString(i));
            }
        }
        return entries;
    }
}
