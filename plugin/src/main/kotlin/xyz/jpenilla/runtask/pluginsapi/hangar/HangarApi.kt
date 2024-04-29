/*
 * Run Task Gradle Plugins
 * Copyright (c) 2024 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jpenilla.runtask.pluginsapi.hangar

import org.gradle.api.provider.Property
import xyz.jpenilla.runtask.pluginsapi.HangarApiDownload
import xyz.jpenilla.runtask.pluginsapi.PluginApi

/**
 * [PluginApi] implementation using the [Hangar](https://hangar.papermc.io/) API.
 */
public interface HangarApi : PluginApi<HangarApi, HangarApiDownload> {
  public companion object {
    /**
     * Default value for [url].
     */
    public const val DEFAULT_URL: String = "https://hangar.papermc.io"
  }

  /**
   * Hangar API URL.
   */
  public val url: Property<String>

  /**
   * Add a plugin download.
   *
   * @param plugin plugin name on Hangar
   * @param version plugin version
   */
  public fun add(plugin: String, version: String)
}
