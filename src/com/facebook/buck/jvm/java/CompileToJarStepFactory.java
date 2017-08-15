/*
 * Copyright 2012-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.jvm.java;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.step.Step;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

/**
 * Creates the necessary steps to compile the source files, apply post process classes commands, and
 * pack the output .class files into a Jar.
 */
public interface CompileToJarStepFactory {

  default BuildRuleParams addInputs(BuildRuleParams params, SourcePathRuleFinder ruleFinder) {
    return params
        .withDeclaredDeps(
            () ->
                ImmutableSortedSet.copyOf(
                    Iterables.concat(params.getDeclaredDeps().get(), getDeclaredDeps(ruleFinder))))
        .withExtraDeps(
            () ->
                ImmutableSortedSet.copyOf(
                    Iterables.concat(params.getExtraDeps().get(), getExtraDeps(ruleFinder))));
  }

  Iterable<BuildRule> getDeclaredDeps(SourcePathRuleFinder ruleFinder);

  Iterable<BuildRule> getExtraDeps(SourcePathRuleFinder ruleFinder);

  void createCompileStep(
      BuildContext context,
      BuildTarget invokingRule,
      SourcePathResolver resolver,
      ProjectFilesystem filesystem,
      CompilerParameters parameters,
      /* output params */
      ImmutableList.Builder<Step> steps,
      BuildableContext buildableContext);

  void createJarStep(
      ProjectFilesystem filesystem, JarParameters parameters, ImmutableList.Builder<Step> steps);

  void createCompileToJarStep(
      BuildContext context,
      BuildTarget invokingRule,
      SourcePathResolver resolver,
      SourcePathRuleFinder ruleFinder,
      ProjectFilesystem filesystem,
      CompilerParameters compilerParameters,
      ImmutableList<String> postprocessClassesCommands,
      JarParameters jarParameters,
      /* output params */
      ImmutableList.Builder<Step> steps,
      BuildableContext buildableContext);
}
