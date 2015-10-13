package com.mendix.ux.sassaas.specs.api;

public interface SassApiTests {
/* @Api(value = "/sass", description = "The sass API") */
    /**
    * @ApiOperation(value = "", notes = "Returns CSS by compiling an input Sass template", response = File.class)
    * @ApiResponses(value = { 
        *     @ApiResponse(code = 200, message = "Success"),
        *     @ApiResponse(code = 0, message = "Error") })
    **/
    public void compileSass() throws Exception;

}
