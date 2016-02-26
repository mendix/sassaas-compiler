package com.mendix.ux.sassaas;

import com.mendix.ux.sassaas.specs.api.SessionsApi;
import com.mendix.ux.sassaas.specs.model.KeyValue;
import com.mendix.ux.sassaas.specs.model.ResultResponse;
import com.mendix.ux.sassaas.utils.SassCompiler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/v1/sessions/{sessionId}")
public class SessionsController implements SessionsApi {

    private final static Logger logger = LoggerFactory.getLogger(SessionsController.class);

    @Autowired
    HttpServletResponse response;

    @Autowired
    HttpServletRequest request;

    private final String VARIABLE_FILENAME = "custom_variables.json";
    private final String WORKSPACE_NAME = "workspace";
    private final String OUTPUT_CSS = "out.css";
    private final String OUTPUT_ZIP = "theme.zip";
    private final String METADATA = "metadata.json";
    private final String METADATA_LOGO = "logo";
    private final String DEFAULT_THEME = "mendix-ui-theme-silverlinings";
    private final String METADATA_THEME = "theme";

    @Value(value = "${basethemeurl:http://localhost:8000}")
    private String BASE_THEME_URL;

    @Override
    @RequestMapping(value="", method = RequestMethod.PUT)
    public ResultResponse createSession(@PathVariable("sessionId") String sessionId, @RequestParam(value = "theme", required = false) String theme) throws Exception {
        validateSessionId(sessionId);
        validateSessionId(theme);
        if (theme == null)
            theme = DEFAULT_THEME;

        File sessionDir = getSessionDir(sessionId);
        JSONObject metadata = loadMetadata(sessionDir);
        metadata.put(METADATA_THEME, theme);
        saveMetadata(metadata, sessionDir);

        ResultResponse result = new ResultResponse();
        result.setMessage(theme);
        return result;
    }

    @Override
    @RequestMapping(value="/css", method = RequestMethod.GET)
    public File getCSSOutput(@PathVariable("sessionId") String sessionId) throws Exception {
        validateSessionId(sessionId);
        File inputFile = writeInputStreamToFile(sessionId, getClass().getResourceAsStream("/default-theme.zip"));
        File sessionDir = getSessionDir(sessionId);
        File variables = new File(sessionDir, VARIABLE_FILENAME);
        File workspace = new File(sessionDir, WORKSPACE_NAME);
        File cssOutFile = new File(sessionDir, OUTPUT_CSS);
        SassCompiler compiler = new SassCompiler(inputFile, workspace, variables, null);
        compiler.exportCss(cssOutFile.getAbsolutePath());
        response.setContentType("text/css");
        response.setHeader("Content-Disposition", String.format("attachment; filename=windows-%s", OUTPUT_CSS));
        InputStream inputStream = new FileInputStream(cssOutFile);
        OutputStream outputStream = response.getOutputStream();
        try {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            logger.info(e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return null;
    }

    @Override
    @RequestMapping(value="/logo", method = RequestMethod.GET)
    public File getLogo(@PathVariable("sessionId") String sessionId) throws Exception {
        validateSessionId(sessionId);
        File sessionDir = getSessionDir(sessionId);
        JSONObject metadata = loadMetadata(sessionDir);
        String filename = metadata.getString(METADATA_LOGO);
        File logo = new File(sessionDir, filename);
        if (!logo.isFile()) {
            throw new IllegalAccessException("Logo not found");
        }
        response.setContentType("image");
        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", filename));
        InputStream inputStream = new FileInputStream(logo);
        OutputStream outputStream = response.getOutputStream();
        try {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            logger.info(e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return null;
    }

    @Override
    @CrossOrigin(origins = "*")
    @RequestMapping(value="/logo", method = RequestMethod.POST)
    public ResultResponse uploadLogo(@PathVariable("sessionId") String sessionId, @RequestPart("file") MultipartFile fileDetail) throws Exception {
        validateSessionId(sessionId);
        String extension = FilenameUtils.getExtension(fileDetail.getOriginalFilename());
        String targetFilename = String.format("logo.%s", extension);
        File sessionDir = getSessionDir(sessionId);
        File outfile = new File(sessionDir, targetFilename);
        OutputStream outputStream = new FileOutputStream(outfile);
        try {
            IOUtils.copy(fileDetail.getInputStream(), outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(fileDetail.getInputStream());
        }
        JSONObject metadata = loadMetadata(sessionDir);
        metadata.put(METADATA_LOGO, targetFilename);
        saveMetadata(metadata, sessionDir);
        ResultResponse result = new ResultResponse();
        result.setMessage(targetFilename);
        return result;
    }

    @Override
    @CrossOrigin(origins = "*")
    @RequestMapping(value="/variables", method = RequestMethod.PUT)
    public ResultResponse setVariables(@PathVariable("sessionId") String sessionId, @RequestBody List<KeyValue> variables) throws Exception {
        validateSessionId(sessionId);
        File outfile = new File(getSessionDir(sessionId), VARIABLE_FILENAME);
        JSONObject jsonObject = new JSONObject();
        for (KeyValue item : variables)
            jsonObject.put(item.getKey(), item.getValue());
        OutputStream outputStream = new FileOutputStream(outfile);
        try {
            outputStream.write(jsonObject.toString(2).getBytes());
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        ResultResponse result = new ResultResponse();
        result.setMessage("Variables are saved");
        return result;
    }

    @Override
    @RequestMapping(value="/zip", method = RequestMethod.GET)
    public File getZipOutput(@PathVariable("sessionId") String sessionId) throws Exception {
        validateSessionId(sessionId);
        File inputFile = writeInputStreamToFile(sessionId, getClass().getResourceAsStream("/default-theme.zip"));
        File sessionDir = getSessionDir(sessionId);
        File variables = new File(sessionDir, VARIABLE_FILENAME);
        File workspace = new File(sessionDir, WORKSPACE_NAME);
        File zipOutFile = new File(sessionDir, OUTPUT_ZIP);
        SassCompiler compiler = new SassCompiler(inputFile, workspace, variables, null);
        compiler.exportZip(zipOutFile.getAbsolutePath());
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", String.format("attachment; filename=mendix-%s", OUTPUT_ZIP));
        InputStream inputStream = new FileInputStream(zipOutFile);
        OutputStream outputStream = response.getOutputStream();
        try {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            logger.info(e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return null;
    }

    private File getSessionDir(String sessionId) {
        File sessionDir = new File(Application.CACHE_DIR, sessionId);
        if (!sessionDir.isDirectory()) {
            sessionDir.mkdir();
        }
        return sessionDir;
    }

    private void validateSessionId(String sessionId) {
        if (sessionId != null && sessionId.matches("[a-zA-Z0-9-]+"))
            return;
        throw new IllegalArgumentException("Invalid session format");
    }

    private File downloadTheme(String theme) throws IOException {
        File themesDir = new File(Application.CACHE_DIR, "__" + theme); // enduser cannot use this *special* session
        if (!themesDir.isDirectory())
            themesDir.mkdirs();
        File themeFile = new File(themesDir, theme + ".zip");
        if (!themeFile.isFile()) {
            try {
                URL url = new URL(String.format("%s/%s.zip", BASE_THEME_URL, theme));
                logger.info("Fetching theme: " + url);
                FileUtils.copyURLToFile(url, themeFile);
                logger.info("Finished downloading to: " + themeFile);
            } catch (IOException e) {
                if (themeFile.isFile())
                    themeFile.delete();
                throw e;
            }
        } else {
            logger.info("Using cached theme: " + themeFile);
        }
        return themeFile;
    }

    private File writeInputStreamToFile(String sessionId, InputStream inputStream) throws IOException {
        File sessionDir = getSessionDir(sessionId);
        JSONObject metadata = loadMetadata(sessionDir);
        if (metadata.has(METADATA_THEME)) {
            String theme = metadata.getString(METADATA_THEME);
            logger.info("Using non internal theme: " + theme);
            File themeFile = downloadTheme(theme);
            IOUtils.closeQuietly(inputStream); // re-open new one
            inputStream = new FileInputStream(themeFile);
        }

        File outfile = new File(getSessionDir(sessionId), "input.zip");
        FileOutputStream outStream = new FileOutputStream(outfile);
        try {
            IOUtils.copy(inputStream, outStream);
        } finally {
            IOUtils.closeQuietly(outStream);
            IOUtils.closeQuietly(inputStream);
        }
        return outfile;
    }

    private JSONObject loadMetadata(File sessionDir) throws IOException {
        File metadata = new File(sessionDir, METADATA);
        if (!metadata.isFile()) {
            return new JSONObject();
        }
        String metadataString = FileUtils.readFileToString(metadata);
        JSONTokener tokener = new JSONTokener(metadataString);
        return new JSONObject(tokener);
    }

    private void saveMetadata(JSONObject metadata, File sessionDir) throws IOException {
        File metadataFile = new File(sessionDir, METADATA);
        FileOutputStream outputStream1 = new FileOutputStream(metadataFile);
        try {
            outputStream1.write(metadata.toString(2).getBytes());
        } finally {
            IOUtils.closeQuietly(outputStream1);
        }
    }
}
