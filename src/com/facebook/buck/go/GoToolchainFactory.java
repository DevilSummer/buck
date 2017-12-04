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

package com.facebook.buck.go;

import com.facebook.buck.model.InternalFlavor;
import com.facebook.buck.toolchain.ToolchainCreationContext;
import com.facebook.buck.toolchain.ToolchainFactory;
import com.facebook.buck.toolchain.ToolchainProvider;
import com.facebook.buck.util.HumanReadableException;
import com.facebook.buck.util.environment.Architecture;
import com.facebook.buck.util.environment.Platform;
import java.util.Optional;

public class GoToolchainFactory implements ToolchainFactory<GoToolchain> {

  @Override
  public Optional<GoToolchain> createToolchain(
      ToolchainProvider toolchainProvider, ToolchainCreationContext context) {

    GoPlatformFlavorDomain platformFlavorDomain = new GoPlatformFlavorDomain();

    return Optional.of(
        GoToolchain.of(
            platformFlavorDomain,
            getDefaultPlatform(
                new GoBuckConfig(context.getBuckConfig(), context.getProcessExecutor()),
                platformFlavorDomain)));
  }

  private GoPlatform getDefaultPlatform(
      GoBuckConfig goBuckConfig, GoPlatformFlavorDomain platformFlavorDomain) {
    Optional<String> configValue = goBuckConfig.getDefaultPlatform();
    Optional<GoPlatform> goPlatform;
    if (configValue.isPresent()) {
      goPlatform = platformFlavorDomain.getValue(InternalFlavor.of(configValue.get()));
      if (!goPlatform.isPresent()) {
        throw new HumanReadableException(
            "Bad go platform value for go.default_platform = %s", configValue);
      }
    } else {
      Platform platform = goBuckConfig.getDelegate().getPlatform();
      Architecture architecture = goBuckConfig.getDelegate().getArchitecture();
      goPlatform = platformFlavorDomain.getValue(platform, architecture);
      if (!goPlatform.isPresent()) {
        throw new HumanReadableException(
            "Couldn't determine default go platform for %s %s", platform, architecture);
      }
    }
    return goPlatform.get();
  }
}