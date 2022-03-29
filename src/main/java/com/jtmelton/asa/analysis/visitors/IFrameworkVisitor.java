package com.jtmelton.asa.analysis.visitors;

import com.jtmelton.asa.domain.Framework;
import com.jtmelton.asa.domain.Package;

import java.util.Collection;

public interface IFrameworkVisitor extends IBaseVisitor {
    Collection<Framework> getDetectedFrameworks();

    Collection<Package> getUnknownPackages();
}
