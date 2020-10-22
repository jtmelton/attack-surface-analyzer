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
        String in = "const express = require('express')\n" +
                "const app = express()\n" +
                "app.get('/jsexpresspath', (req, res) => {\n" +
                "})\n";

        JSONObject obj = analyze(in, "input.js");

        assertEquals(1, obj.getJSONArray("routes").length());
        assertEquals("'/jsexpresspath'", obj.getJSONArray("routes").getJSONObject(0).get("path"));
    }

    @Test
    public void givenASpringCodebase_whenCodeHasOnePath_thenPathIdentified() throws IOException {
        String in = "@RestController\n" +
                "@RequestMapping(\"/home\")\n" +
                "public class IndexController {\n" +
                "  @RequestMapping(\"/index\")\n" +
                "  String index(){\n" +
                "  }\n";

        JSONObject obj = analyze(in, "input.java");

        assertEquals(1, obj.getJSONArray("routes").length());
        assertEquals("/home/index", obj.getJSONArray("routes").getJSONObject(0).get("path"));
    }

    @Test
    public void givenASpringCodebase_whenCodeHasOnePathAndRequestMethod_thenPathIdentified() throws IOException {
        String in = "@RestController\n" +
                "@RequestMapping(\"/home\")\n" +
                "public class IndexController {\n" +
                "      @RequestMapping(value = \"/index\", method = RequestMethod.GET)\n" +
                "  String index(){\n" +
                "  }\n";

        JSONObject obj = analyze(in, "input.java");

        assertEquals(1, obj.getJSONArray("routes").length());
        assertEquals("/home/index", obj.getJSONArray("routes").getJSONObject(0).get("path"));
    }

    @Test
    public void givenASpringCodebase_whenCodeHasOnePathAndRequestMethodInDifferentOrder_thenPathIdentified() throws IOException {
        String in = "@RestController\n" +
                "@RequestMapping(\"/home\")\n" +
                "public class IndexController {\n" +
                "      @RequestMapping(method = RequestMethod.GET, value = \"/index\")\n" +
                "  String index(){\n" +
                "  }\n";

        JSONObject obj = analyze(in, "input.java");

        assertEquals(1, obj.getJSONArray("routes").length());
        assertEquals("/home/index", obj.getJSONArray("routes").getJSONObject(0).get("path"));
    }

    @Test
    public void givenASpringCodebase_whenCodeHasTwoPaths_thenPathIdentified() throws IOException {
        String in = "@RestController\n" +
                "@RequestMapping(\"/home\")\n" +
                "public class IndexController {\n" +
                "  @RequestMapping(\"/first\")\n" +
                "  String first(){\n" +
                "  }\n" +
                "  @RequestMapping(\"/second\")\n" +
                "  String second(){\n" +
                "  }\n" +
                "}";

        JSONObject obj = analyze(in, "input.java");

        assertEquals(2, obj.getJSONArray("routes").length());
        assertEquals("/home/first", obj.getJSONArray("routes").getJSONObject(0).get("path"));
        assertEquals("/home/second", obj.getJSONArray("routes").getJSONObject(1).get("path"));
    }

    @Test
    public void givenASpringCodebase_whenCodeHasGetPostAndDeleteMapping_thenPathIdentified() throws IOException {
        String in = "@RestController\n" +
                "@RequestMapping(\"/top\")\n" +
                "public class IndexController {\n" +
                "  @GetMapping(\"/get\")\n" +
                "  String get(){\n" +
                "  }\n" +
                "  @PostMapping(\"/post\")\n" +
                "  String post(){\n" +
                "  }\n" +
                "  @DeleteMapping(\"/delete\")\n" +
                "  String delete(){\n" +
                "  }\n" +
                "  @PutMapping(\"/put\")\n" +
                "  String put(){\n" +
                "  }\n" +
                "  @PatchMapping(\"/patch\")\n" +
                "  String patch(){\n" +
                "  }\n" +
                "  @GetMapping\n" +
                "  String get2(){\n" +
                "  }\n" +
                "}";

        JSONObject obj = analyze(in, "input.java");

        assertEquals(6, obj.getJSONArray("routes").length());
        assertEquals("/top/get", obj.getJSONArray("routes").getJSONObject(0).get("path"));
        assertEquals("/top/post", obj.getJSONArray("routes").getJSONObject(1).get("path"));
        assertEquals("/top/delete", obj.getJSONArray("routes").getJSONObject(2).get("path"));
        assertEquals("/top/put", obj.getJSONArray("routes").getJSONObject(3).get("path"));
        assertEquals("/top/patch", obj.getJSONArray("routes").getJSONObject(4).get("path"));
        assertEquals("/top", obj.getJSONArray("routes").getJSONObject(5).get("path"));
    }

    private JSONObject analyze(String in, String fileName) throws IOException {
        Path filePath = testFolder.newFile(fileName).toPath();
        Files.write(filePath, in.getBytes());

        Path dirPath = testFolder.newFile("out.json").toPath();

        AttackSurfaceAnalyzer.main(new String[]{"-sourceDirectory", filePath.toAbsolutePath().toString(), "-outputFile", dirPath.toAbsolutePath().toString()});

        return new JSONObject(new String(Files.readAllBytes(dirPath)));
    }
}