package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.visitors.IBaseVisitor;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor;
import com.jtmelton.asa.config.Configuration;
import com.jtmelton.asa.domain.Route;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class RouteAnalyzer extends BaseAnalyzer {

    private final Collection<Route> routes = new ArrayList<>();

    public RouteAnalyzer(Configuration configuration, Collection<String> userExclusions, boolean parserStderr) {
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
            }
        }
    }

    public Collection<Route> getRoutes() {
        return routes;
    }

}