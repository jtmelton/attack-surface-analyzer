package com.jtmelton.asa.analysis.visitors;

import com.jtmelton.asa.domain.Route;

import java.util.Collection;

public interface IRouteVisitor extends IBaseVisitor {
    Collection<Route> getRoutes();
}
