package com.jtmelton.asa;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class AttackSurfaceAnalyzerTest {

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void givenAJaxRsCodebase_whenCodeHasOnePath_thenPathIdentified() throws IOException {
        String in = "@Path(\"/jaxrspath\")" +
                "public class TodoResource {" +
                "    @GET" +
                "    public void foo() {" +
                "    }" +
                "}";

        JSONObject obj = analyze(in, "input.java");

        assertEquals(1, obj.getJSONArray("routes").length());
        assertEquals("/jaxrspath", obj.getJSONArray("routes").getJSONObject(0).get("path"));
    }

    @Test
    public void givenAJSExpressCodebase_whenCodeHasOnePath_thenPathIdentified() throws IOException {
        String in = "\n" +
                "const express = require('express')\n" +
                "const app = express()\n" +
                "app.get('/jsexpresspath', (req, res) => {\n" +
                "})\n";

        JSONObject obj = analyze(in, "input.js");

        assertEquals(1, obj.getJSONArray("routes").length());
        assertEquals("'/jsexpresspath'", obj.getJSONArray("routes").getJSONObject(0).get("path"));
    }

    private JSONObject analyze(String in, String fileName) throws IOException {
        Path filePath = testFolder.newFile(fileName).toPath();
        Files.write(filePath, in.getBytes());

        Path dirPath = testFolder.newFile("out.json").toPath();

        AttackSurfaceAnalyzer.main(new String[]{"-sourceDirectory", filePath.toAbsolutePath().toString(), "-outputFile", dirPath.toAbsolutePath().toString()});

        return new JSONObject(new String(Files.readAllBytes(dirPath)));
    }
}