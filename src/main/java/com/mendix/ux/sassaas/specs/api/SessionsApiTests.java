package com.mendix.ux.sassaas.specs.api;

public interface SessionsApiTests {
/* @Api(value = "/sessions", description = "The sessions API") */
    /**
    * @ApiOperation(value = "", notes = "Upload a logo", response = ResultResponse.class)
    * @ApiResponses(value = { 
        *     @ApiResponse(code = 200, message = "Success"),
        *     @ApiResponse(code = 0, message = "Error") })
    **/
    public void uploadLogo() throws Exception;

    /**
    * @ApiOperation(value = "", notes = "Set variable values", response = ResultResponse.class)
    * @ApiResponses(value = { 
        *     @ApiResponse(code = 200, message = "Success"),
        *     @ApiResponse(code = 0, message = "Error") })
    **/
    public void setVariables() throws Exception;

}
