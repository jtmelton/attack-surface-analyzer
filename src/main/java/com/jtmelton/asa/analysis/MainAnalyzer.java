package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.visitors.IBaseVisitor;
import com.jtmelton.asa.analysis.visitors.IFrameworkVisitor;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor;
import com.jtmelton.asa.config.Configuration;
import com.jtmelton.asa.domain.Framework;
import com.jtmelton.asa.domain.Package;
import com.jtmelton.asa.domain.Route;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class MainAnalyzer extends BaseAnalyzer {

    private final Collection<Framework> detectedFrameworks = new ArrayList<>();

    private final Collection<Package> unknownPackages = new ArrayList<>();

    private final Collection<Route> routes = new ArrayList<>();

    public MainAnalyzer(Configuration configuration, Collection<String> userExclusions,
                        int threads , boolean parserStderr) {
        super(threads);

        registerVisitors(configuration);

        this.exclusions.add(".+node_modules.+");
        this.exclusions.addAll(userExclusions);

        this.parserStderr = parserStderr;
    }

    public void analyze(File sourceDirectory) {

        final Collection<Path> paths = getPaths(sourceDirectory);

        scan(paths);

        for (IBaseVisitor visitor : getVisitors()) {
            if (visitor instanceof IRouteVisitor) {
                routes.addAll(((IRouteVisitor) visitor).getRoutes());
            } else if (visitor instanceof IFrameworkVisitor) {
                detectedFrameworks.addAll(((IFrameworkVisitor) visitor).getDetectedFrameworks());
                unknownPackages.addAll(((IFrameworkVisitor) visitor).getUnknownPackages());
            }
        }
    }

    public Collection<Route> getRoutes() {
        return routes;
    }

    public Collection<Framework> getDetectedFrameworks() {
        return detectedFrameworks;
    }

    public Collection<Package> getUnknownPackages() {
        return unknownPackages;
    }

}