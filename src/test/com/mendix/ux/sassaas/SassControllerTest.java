package com.mendix.ux.sassaas;

import com.mendix.ux.sassaas.specs.api.SassApiTests;
import com.vaadin.sass.SassCompiler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public class SassControllerTest implements SassApiTests {

    @Mock
    MultipartFile multipartFile;

    @InjectMocks
    SassController controller = new SassController();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    @Test
    public void compileSass() throws Exception {
        // Given
        Mockito.when(multipartFile.getInputStream()).thenReturn(getClass().getResourceAsStream("/template.zip"));

        // When
        File output = controller.compileSass(multipartFile, null, null);


        // Then
        // output is compiled css; no exception is good
    }

    @Test
    public void singleFileTest() throws Exception {
        String inputFilePath = getClass().getResource("/sample.scss").getPath();
        File outFile = File.createTempFile("test", ".css");
        String outputFilePath =outFile.getAbsolutePath();
        String args[] = {inputFilePath, outputFilePath};
        try {
            SassCompiler.main(args);
        } finally {
            outFile.delete();
        }
    }
}
