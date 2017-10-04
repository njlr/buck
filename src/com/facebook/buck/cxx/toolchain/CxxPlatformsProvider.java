/*
 * Copyright 2017-present Facebook, Inc.
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

package com.facebook.buck.cxx.toolchain;

import com.facebook.buck.config.BuckConfig;
import com.facebook.buck.log.Logger;
import com.facebook.buck.model.Flavor;
import com.facebook.buck.model.FlavorDomain;
import com.facebook.buck.model.InternalFlavor;
import com.facebook.buck.util.environment.Platform;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Optional;

public class CxxPlatformsProvider {

  private static final Logger LOG = Logger.get(CxxPlatformsProvider.class);

  private final CxxPlatform defaultCxxPlatform;
  private final FlavorDomain<CxxPlatform> cxxPlatforms;

  public CxxPlatformsProvider(
      CxxPlatform defaultCxxPlatform, FlavorDomain<CxxPlatform> cxxPlatforms) {
    this.defaultCxxPlatform = defaultCxxPlatform;
    this.cxxPlatforms = cxxPlatforms;
  }

  public CxxPlatform getDefaultCxxPlatform() {
    return defaultCxxPlatform;
  }

  public FlavorDomain<CxxPlatform> getCxxPlatforms() {
    return cxxPlatforms;
  }

  public static CxxPlatformsProvider create(
      BuckConfig config, ImmutableMap<Flavor, CxxPlatform> cxxSystemPlatforms) {
    Platform platform = Platform.detect();
    CxxBuckConfig cxxBuckConfig = new CxxBuckConfig(config);

    // Create a map of system platforms.
    ImmutableMap.Builder<Flavor, CxxPlatform> cxxSystemPlatformsBuilder = ImmutableMap.builder();

    cxxSystemPlatformsBuilder.putAll(cxxSystemPlatforms);

    CxxPlatform defaultHostCxxPlatform = DefaultCxxPlatforms.build(platform, cxxBuckConfig);
    cxxSystemPlatformsBuilder.put(defaultHostCxxPlatform.getFlavor(), defaultHostCxxPlatform);
    ImmutableMap<Flavor, CxxPlatform> cxxSystemPlatformsMap = cxxSystemPlatformsBuilder.build();

    // Add the host platform if needed (for example, when building on Linux).
    Flavor hostFlavor = CxxPlatforms.getHostFlavor();
    if (!cxxSystemPlatformsMap.containsKey(hostFlavor)) {
      cxxSystemPlatformsBuilder.put(
          hostFlavor,
          CxxPlatform.builder().from(defaultHostCxxPlatform).setFlavor(hostFlavor).build());
      cxxSystemPlatformsMap = cxxSystemPlatformsBuilder.build();
    }

    // Add platforms for each cxx flavor obtained from the buck config files
    // from sections of the form cxx#{flavor name}.
    // These platforms are overrides for existing system platforms.
    ImmutableSet<Flavor> possibleHostFlavors = CxxPlatforms.getAllPossibleHostFlavors();
    HashMap<Flavor, CxxPlatform> cxxOverridePlatformsMap =
        new HashMap<Flavor, CxxPlatform>(cxxSystemPlatformsMap);
    ImmutableSet<Flavor> cxxFlavors = CxxBuckConfig.getCxxFlavors(config);
    for (Flavor flavor : cxxFlavors) {
      CxxPlatform baseCxxPlatform = cxxSystemPlatformsMap.get(flavor);
      if (baseCxxPlatform == null) {
        if (possibleHostFlavors.contains(flavor)) {
          // If a flavor is for an alternate host, it's safe to skip.
          continue;
        }
        LOG.info("Applying \"%s\" overrides to default host platform", flavor);
        baseCxxPlatform = defaultHostCxxPlatform;
      }
      cxxOverridePlatformsMap.put(
          flavor,
          CxxPlatforms.copyPlatformWithFlavorAndConfig(
              baseCxxPlatform, platform, new CxxBuckConfig(config, flavor), flavor));
    }

    // Finalize our "default" host.
    Optional<String> hostCxxPlatformOverride = cxxBuckConfig.getHostPlatform();
    if (hostCxxPlatformOverride.isPresent()) {
      Flavor overrideFlavor = InternalFlavor.of(hostCxxPlatformOverride.get());
      if (cxxOverridePlatformsMap.containsKey(overrideFlavor)) {
        hostFlavor = overrideFlavor;
      }
    }

    CxxPlatform hostCxxPlatform = cxxOverridePlatformsMap.get(hostFlavor);

    ImmutableMap<Flavor, CxxPlatform> cxxPlatformsMap =
        ImmutableMap.<Flavor, CxxPlatform>builder().putAll(cxxOverridePlatformsMap).build();

    // Build up the final list of C/C++ platforms.
    FlavorDomain<CxxPlatform> cxxPlatforms = new FlavorDomain<>("C/C++ platform", cxxPlatformsMap);

    // Get the default target platform from config.
    CxxPlatform defaultCxxPlatform = 
        CxxPlatforms.getConfigDefaultCxxPlatform(cxxBuckConfig, cxxPlatformsMap, hostCxxPlatform);

    return new CxxPlatformsProvider(defaultCxxPlatform, cxxPlatforms);
  }
}
