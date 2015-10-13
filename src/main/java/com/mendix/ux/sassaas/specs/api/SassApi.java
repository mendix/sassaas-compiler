// This file was generated by swagger-codegen.
//
// WARNING: Code you write here will be lost the next time you update the specification

package com.mendix.ux.sassaas.specs.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(value = "/sass", produces = {APPLICATION_JSON_VALUE})
public interface SassApi {
    /* @Api(value = "/sass", description = "The sass API") */


        /**
        * @ApiOperation(value = "", notes = "Returns CSS by compiling an input Sass template", response = File.class)
        * Parameters:
        * @ApiParam(value = "file detail") MultipartFile fileDetail
        * @ApiParam(value = "List of variables to apply in JSON format. For instance: [{\"color1\": \"#123\"}, {\"font\": \"Sans\"}]") String variables
        * @ApiParam(value = "List of entrypoints to apply in JSON format. For instance: [{\"index.scss\": \"index.css\"}, {\"custom.scss\": \"custom.css\"}]") String entrypoints
        * @ApiParam(value = "Type of output. Valid values are: zip or css; default is zip") String output
        *
        * @ApiResponses(value = { 
        *     @ApiResponse(code = 200, message = "Success"),
        *     @ApiResponse(code = 0, message = "Error") })
        **/
        @RequestMapping(value = "",

        consumes = { "multipart/form-data" },
        method = RequestMethod.POST)
        public File compileSass(

@RequestPart("file") MultipartFile fileDetail,@RequestParam(value = "variables", required = false) String variables

,@RequestParam(value = "entrypoints", required = false) String entrypoints

,@RequestParam(value = "output", required = false) String output

)
        throws Exception;
    }
