package com.jtmelton.asa;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.jtmelton.asa.analysis.MainAnalyzer;
import com.jtmelton.asa.config.Configuration;
import com.jtmelton.asa.domain.Framework;
import com.jtmelton.asa.domain.Package;
import com.jtmelton.asa.domain.Route;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class AttackSurfaceAnalyzer {

  private static final Logger log = LoggerFactory.getLogger(AttackSurfaceAnalyzer.class);

  @Argument(value = "sourceDirectory",
      required = true,
      description = "Directory containing source code for analysis")
  private static String sourceDirectory = null;

  @Argument(value = "outputFile",
      required = true,
      description = "File containing output with discovered routes")
  private static String outputFile = null;

  @Argument(value = "exclusions",
      description = "Comma delimited regex patterns")
  private static String[] exclusions = null;

  @Argument(alias = "parser-stderr",
      description = "Enable stderr logging from parsers")
  private static boolean parserStderr = false;

//  @Argument(alias = "properties",
//      description = "Properties file to load")
//  private static String props = "";

  @Argument(alias = "configFile",
          description = "Configuration file to load")
  private static String configFile = "";

  @Argument(alias = "threads",
      description = "Number of threads to use, defaults to 1")
  private static Integer threads = 1;


  public void parseArguments(String[] args) {
    Args.parse(this, args);
  }

  public static void main(String[] args) {
    long startMillis = System.currentTimeMillis();

    AttackSurfaceAnalyzer main = new AttackSurfaceAnalyzer();
    main.parseArguments(args);

//    Settings settings;
//
//    try {
//      if (props.isEmpty()) {
//        settings = new Settings();
//      } else {
//        settings = new Settings(props);
//      }
//    } catch (IOException ioe) {
//      log.error("Failed to read properties", ioe);
//      return;
//    }

    Configuration configuration;

    try {
      if (configFile.isEmpty()) {
        configuration = main.loadConfiguration();
      } else {
        configuration = main.loadConfiguration(configFile);
      }
    } catch (IOException ioe) {
      log.error("Failed to read properties", ioe);
      return;
    }

    Collection<String> exclusionsList = new ArrayList<>();
    if(exclusions != null) {
      exclusionsList.addAll(Arrays.asList(exclusions));
    }

    MainAnalyzer mainAnalyzer = new MainAnalyzer(configuration, exclusionsList, threads, parserStderr);
    mainAnalyzer.analyze(new File(sourceDirectory));

    JSONObject jsonRoot = new JSONObject();
    JSONArray jsonRoutes = new JSONArray();
    jsonRoot.put("routes", jsonRoutes);
    JSONArray jsonDetectedFrameworks = new JSONArray();
    jsonRoot.put("detectedFrameworks", jsonDetectedFrameworks);
    JSONArray jsonUnknownPackages = new JSONArray();
    jsonRoot.put("unnknownPackages", jsonUnknownPackages);

    log.info("Printing routes");
    for(Route route : mainAnalyzer.getRoutes()) {
      log.info(route.toString());
      jsonRoutes.put(route.toJSON());
    }

    log.info("Found {} routes", mainAnalyzer.getRoutes().size());

    log.info("Printing detected frameworks");
    for(Framework framework : mainAnalyzer.getDetectedFrameworks()) {
      log.info(framework.toString());
      jsonDetectedFrameworks.put(framework.toJSON());
    }

    log.info("Found {} detected frameworks", mainAnalyzer.getDetectedFrameworks().size());

    log.info("Printing unknown packages");
    for(Package pkg : mainAnalyzer.getUnknownPackages()) {
      log.info(pkg.toString());
      System.err.println("wrote! " + pkg.toJSON());
      jsonUnknownPackages.put(pkg.toJSON());
    }

    log.info("Found {} unknown packages", mainAnalyzer.getUnknownPackages().size());

    try {
      Files.asCharSink(new File(outputFile), StandardCharsets.UTF_8).write(jsonRoot.toString(2));
    } catch (IOException ioe) {
      log.error("Failed to write results", ioe);
    }

    long endMillis = System.currentTimeMillis();

    log.info("Execution completed in {}s", (endMillis - startMillis) / 1000);
  }

  private Configuration loadConfiguration() throws IOException {
    ClassLoader loader = getClass().getClassLoader();
    InputStream is = loader.getResourceAsStream("asa-config.json");
    return loadConfiguration(is);
  }

  private Configuration loadConfiguration(String jsonConfigLocation) throws IOException {
    InputStream is = new FileInputStream(jsonConfigLocation);
    return loadConfiguration(is);
  }

  private Configuration loadConfiguration(InputStream is) throws IOException {
    Configuration configuration = new Configuration();

    Gson gson = new Gson();

    try (Reader reader = new InputStreamReader(is)) {
      // Convert JSON File to Java Object
      configuration = gson.fromJson(reader, Configuration.class);
    }

    return configuration;
  }

}
