package com.jtmelton.asa.analysis.visitors;

import com.jtmelton.asa.analysis.RouteAnalyzer;
import com.jtmelton.asa.domain.Route;

import java.util.Collection;

public interface IRouteVisitor {
    enum Phase {
        ONE,
        TWO,
        THREE
    }
    void setPhase(Phase phase);
    Collection<Route> getRoutes();
    boolean acceptedPhase(Phase phase);
    boolean acceptedLang(RouteAnalyzer.Language lang);
}
