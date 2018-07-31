package com.jtmelton.asa;

import com.jtmelton.asa.analysis.RouteAnalyzer;
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

public class AttackSurfaceAnalyzer {

  private static final Logger logger = LoggerFactory.getLogger(AttackSurfaceAnalyzer.class);

  @Argument(value = "sourceDirectory",
      required = true,
      description = "Directory containing source code for analysis")
  private static String sourceDirectory = null;

  @Argument(value = "outputFile",
      required = true,
      description = "File containing output with discovered routes")
  private static String outputFile = null;

  public void parseArguments(String[] args) {
    Args.parse(this, args);
  }

  public static void main(String[] args) throws IOException {
    long startMillis = System.currentTimeMillis();

    AttackSurfaceAnalyzer main = new AttackSurfaceAnalyzer();
    main.parseArguments(args);

    RouteAnalyzer analyzer = new RouteAnalyzer();
    analyzer.analyze(new File(sourceDirectory));

    JSONObject jsonRoot = new JSONObject();
    JSONArray jsonRoutes = new JSONArray();
    jsonRoot.put("routes", jsonRoutes);

    for(Route route : analyzer.getRoutes()) {
      logger.info(route.toString());
      jsonRoutes.put(route.toJSON());
    }

    Files.write(jsonRoot.toString(2), new File(outputFile), StandardCharsets.UTF_8);

    long endMillis = System.currentTimeMillis();

    logger.info("Execution completed in {}s", (endMillis - startMillis) / 1000);
  }
}
