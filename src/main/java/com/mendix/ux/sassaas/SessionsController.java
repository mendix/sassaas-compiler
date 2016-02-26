package com.mendix.ux.sassaas;

import com.mendix.ux.sassaas.specs.api.SessionsApi;
import com.mendix.ux.sassaas.specs.model.ResultResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/v1/sessions/{sessionId}")
public class SessionsController implements SessionsApi {

    @Autowired
    HttpServletResponse response;

    @Autowired
    HttpServletRequest request;

    @Override
    @RequestMapping(value="/logo", method = RequestMethod.POST)
    public ResultResponse uploadLogo(@PathVariable("sessionId") String sessionId, @RequestPart("file") MultipartFile fileDetail) throws Exception {
        validateSessionId(sessionId);
        String extension = FilenameUtils.getExtension(fileDetail.getOriginalFilename());
        String targetFilename = String.format("logo.%s", extension);
        File outfile = new File(getSessionDir(sessionId), targetFilename);
        OutputStream outputStream = new FileOutputStream(outfile);
        try {
            IOUtils.copy(fileDetail.getInputStream(), outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(fileDetail.getInputStream());
        }
        ResultResponse result = new ResultResponse();
        result.setMessage("Upload completed");
        return result;
    }

    private File getSessionDir(String sessionId) {
        File sessionDir = new File(Application.SESSION_DIR, sessionId);
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
}
