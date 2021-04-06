package com.jtmelton.asa.analysis.visitors;

import com.jtmelton.asa.domain.Framework;
import com.jtmelton.asa.domain.Package;

import java.util.Collection;

public interface IBaseVisitor {

    void setPhase(Phase phase);
    boolean acceptedPhase(Phase phase);
    boolean acceptedLang(Language lang);
}
