/*
 * Copyright 2013-present Facebook, Inc.
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

package com.facebook.buck.apple.xcode.xcodeproj;

import com.facebook.buck.apple.xcode.XcodeprojSerializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Information for building a specific artifact (a library, binary, or test).
 */
public abstract class PBXTarget extends PBXProjectItem {
  public enum ProductType {
    STATIC_LIBRARY("com.apple.product-type.library.static"),
    DYNAMIC_LIBRARY("com.apple.product-type.library.dynamic"),
    TOOL("com.apple.product-type.tool"),
    BUNDLE("com.apple.product-type.bundle"),
    FRAMEWORK("com.apple.product-type.framework"),
    STATIC_FRAMEWORK("com.apple.product-type.framework.static"),
    APPLICATION("com.apple.product-type.application"),
    UNIT_TEST("com.apple.product-type.bundle.unit-test"),
    IN_APP_PURCHASE_CONTENT("com.apple.product-type.in-app-purchase-content"),
    APP_EXTENSION("com.apple.product-type.app-extension");

    public final String identifier;
    private ProductType(String identifier) {
      this.identifier = identifier;
    }

    @Override
    public String toString() {
      return identifier;
    }
  }

  private String name;
  @Nullable private String productName;
  @Nullable private ProductType productType;
  @Nullable private PBXFileReference productReference;
  private List<PBXTargetDependency> dependencies;
  private List<PBXBuildPhase> buildPhases;
  @Nullable private XCConfigurationList buildConfigurationList;

  public PBXTarget(String name) {
    this.name = Preconditions.checkNotNull(name);
    this.dependencies = Lists.newArrayList();
    this.buildPhases = Lists.newArrayList();
  }

  public String getName() {
    return name;
  }
  public void setName(String v) {
    name = v;
  }

  @Nullable
  public ProductType getProductType() {
    return productType;
  }
  public void setProductType(ProductType v) {
    productType = v;
  }

  @Nullable
  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  @Nullable
  public PBXFileReference getProductReference() {
    return productReference;
  }
  public void setProductReference(PBXFileReference v) {
    productReference = v;
  }
  public List<PBXTargetDependency> getDependencies() {
    return dependencies;
  }
  public void setDependencies(List<PBXTargetDependency> v) {
    dependencies = v;
  }
  public List<PBXBuildPhase> getBuildPhases() {
    return buildPhases;
  }
  public void setBuildPhases(List<PBXBuildPhase> v) {
    buildPhases = v;
  }

  @Nullable
  public XCConfigurationList getBuildConfigurationList() {
    return buildConfigurationList;
  }
  public void setBuildConfigurationList(XCConfigurationList v) {
    buildConfigurationList = v;
  }

  @Override
  public String isa() {
    return "PBXTarget";
  }

  @Override
  public int stableHash() {
    return name.hashCode();
  }

  @Override
  public void serializeInto(XcodeprojSerializer s) {
    super.serializeInto(s);

    s.addField("name", name);
    if (productType != null) {
      s.addField("productType", productType.toString());
    }
    if (productName != null) {
      s.addField("productName", productName);
    }
    if (productReference != null) {
      s.addField("productReference", productReference);
    }
    s.addField("dependencies", dependencies);
    s.addField("buildPhases", buildPhases);
    if (buildConfigurationList != null) {
      s.addField("buildConfigurationList", buildConfigurationList);
    }
  }
}
