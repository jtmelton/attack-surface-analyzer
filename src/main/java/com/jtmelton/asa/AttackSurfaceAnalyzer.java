package com.jtmelton.asa;

import com.jtmelton.asa.analysis.RouteAnalyzer;
import com.jtmelton.asa.analysis.utils.Settings;
import com.jtmelton.asa.domain.Route;

import com.google.common.io.Files;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

  @Argument(alias = "properties",
      description = "Properties file to load")
  private static String props = "";

  public void parseArguments(String[] args) {
    Args.parse(this, args);
  }

  public static void main(String[] args) {
    long startMillis = System.currentTimeMillis();

    AttackSurfaceAnalyzer main = new AttackSurfaceAnalyzer();
    main.parseArguments(args);

    Settings settings;

    try {
      if (props.isEmpty()) {
        settings = new Settings();
      } else {
        settings = new Settings(props);
      }
    } catch (IOException ioe) {
      log.error("Failed to read properties", ioe);
      return;
    }

    Collection<String> exclusionsList = new ArrayList<>();
    if(exclusions != null) {
      exclusionsList.addAll(Arrays.asList(exclusions));
    }

    RouteAnalyzer analyzer = new RouteAnalyzer(settings, exclusionsList, parserStderr);
    analyzer.analyze(new File(sourceDirectory));

    JSONObject jsonRoot = new JSONObject();
    JSONArray jsonRoutes = new JSONArray();
    jsonRoot.put("routes", jsonRoutes);

    log.info("Printing routes");
    for(Route route : analyzer.getRoutes()) {
      log.info(route.toString());
      jsonRoutes.put(route.toJSON());
    }

    log.info("Found {} routes", analyzer.getRoutes().size());

    try {
      Files.write(jsonRoot.toString(2), new File(outputFile), StandardCharsets.UTF_8);
    } catch (IOException ioe) {
      log.error("Failed to write results", ioe);
    }

    long endMillis = System.currentTimeMillis();

    log.info("Execution completed in {}s", (endMillis - startMillis) / 1000);
  }
}
