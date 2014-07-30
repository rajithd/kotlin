// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import com.google.dart.compiler.common.Symbol;
import org.jetbrains.annotations.NotNull;

/**
 * A JavaScript parameter.
 */
public final class JsParameter extends SourceInfoAwareJsNode implements HasName {
    @NotNull
    private final JsName name;
    private boolean hasDefaultValue = false;

    public JsParameter(@NotNull JsName name) {
        this.name = name;
    }

    @Override
    @NotNull
    public JsName getName() {
        return name;
    }

    @Override
    @NotNull
    public Symbol getSymbol() {
        return name;
    }

    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    public void setHasDefaultValue(boolean hasDefaultValue) {
        this.hasDefaultValue = hasDefaultValue;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitParameter(this);
    }

    @Override
    public void traverse(JsVisitorWithContext v, JsContext ctx) {
        v.visit(this, ctx);
        v.endVisit(this, ctx);
    }

    @NotNull
    @Override
    public JsParameter deepCopy() {
        JsParameter parameter = new JsParameter(name);
        parameter.setHasDefaultValue(hasDefaultValue);
        return parameter;
    }
}
