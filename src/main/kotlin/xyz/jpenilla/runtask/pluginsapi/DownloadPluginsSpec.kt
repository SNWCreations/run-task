/*
 * Run Task Gradle Plugins
 * Copyright (c) 2023 Jason Penilla
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
package xyz.jpenilla.runtask.pluginsapi

import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectCollectionSchema
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Namer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.Rule
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import xyz.jpenilla.runtask.pluginsapi.github.GitHubApi
import xyz.jpenilla.runtask.pluginsapi.github.GitHubApiImpl
import xyz.jpenilla.runtask.pluginsapi.hangar.HangarApi
import xyz.jpenilla.runtask.pluginsapi.hangar.HangarApiImpl
import xyz.jpenilla.runtask.pluginsapi.modrinth.ModrinthApi
import xyz.jpenilla.runtask.pluginsapi.modrinth.ModrinthApiImpl
import xyz.jpenilla.runtask.util.configure
import xyz.jpenilla.runtask.util.registerFactory
import java.util.SortedMap
import java.util.SortedSet
import javax.inject.Inject

public abstract class DownloadPluginsSpec @Inject constructor(
  private val registry: ExtensiblePolymorphicDomainObjectContainer<PluginApi<*, *>>
) : PolymorphicDomainObjectContainer<PluginApi<*, *>> by registry {

  @get:Nested
  public val downloads: List<PluginApiDownload>
    get() = registry.asSequence().flatMap { it.downloads }.distinct().toList()

  @get:Inject
  protected abstract val objects: ObjectFactory

  init {
    registry.registerFactory(HangarApi::class) { name -> objects.newInstance(HangarApiImpl::class, name) }
    registry.registerFactory(ModrinthApi::class) { name -> objects.newInstance(ModrinthApiImpl::class, name) }
    registry.registerFactory(GitHubApi::class) { name -> objects.newInstance(GitHubApiImpl::class, name) }

    register("hangar", HangarApi::class) {
      url.set("https://hangar.papermc.io")
    }
    register("modrinth", ModrinthApi::class) {
      url.set("https://api.modrinth.com")
    }
    register("github", GitHubApi::class)
  }

  public fun from(spec: DownloadPluginsSpec) {
    fun <T : PluginApi<*, *>> cast(o: Any): T {
      @Suppress("UNCHECKED_CAST")
      return o as T
    }
    // copy from the given spec to this spec (useful for sharing)
    for (name in spec.names) {
      val api = spec[name]
      val type = when (api) {
        is HangarApi -> HangarApi::class
        is ModrinthApi -> ModrinthApi::class
        is GitHubApi -> GitHubApi::class
        else -> throw IllegalStateException("Unknown PluginApi type: ${api.javaClass.name}")
      }
      configure(name, type) {
        copyConfiguration(cast(api))
      }
    }
  }

  // hangar extensions

  @get:Internal
  public val hangar: NamedDomainObjectProvider<HangarApi>
    get() = named("hangar", HangarApi::class)
  public fun hangar(author: String, plugin: String, version: String): NamedDomainObjectProvider<HangarApi> =
    named("hangar", HangarApi::class) {
      add(author, plugin, version)
    }
  public fun hangar(author: String, plugin: String, version: String, configurationAction: Action<HangarApi>): NamedDomainObjectProvider<HangarApi> =
    named("hangar", HangarApi::class) {
      add(author, plugin, version)
      configurationAction.execute(this)
    }
  public fun hangar(configurationAction: Action<HangarApi>): NamedDomainObjectProvider<HangarApi> =
    named("hangar", HangarApi::class, configurationAction)

  public fun NamedDomainObjectProvider<HangarApi>.add(author: String, plugin: String, version: String) {
    configure { add(author, plugin, version) }
  }
  public operator fun NamedDomainObjectProvider<HangarApi>.invoke(author: String, plugin: String, version: String) {
    add(author, plugin, version)
  }

  // modrinth extensions

  @get:Internal
  public val modrinth: NamedDomainObjectProvider<ModrinthApi>
    get() = named("modrinth", ModrinthApi::class)
  public fun modrinth(id: String, version: String): NamedDomainObjectProvider<ModrinthApi> =
    named("modrinth", ModrinthApi::class) {
      add(id, version)
    }
  public fun modrinth(id: String, version: String, configurationAction: Action<ModrinthApi>): NamedDomainObjectProvider<ModrinthApi> =
    named("modrinth", ModrinthApi::class) {
      add(id, version)
      configurationAction.execute(this)
    }
  public fun modrinth(configurationAction: Action<ModrinthApi>): NamedDomainObjectProvider<ModrinthApi> =
    named("modrinth", ModrinthApi::class, configurationAction)

  public fun NamedDomainObjectProvider<ModrinthApi>.add(id: String, version: String) {
    configure { add(id, version) }
  }
  public operator fun NamedDomainObjectProvider<ModrinthApi>.invoke(id: String, version: String) {
    add(id, version)
  }

  // github extensions

  @get:Internal
  public val github: NamedDomainObjectProvider<GitHubApi>
    get() = named("github", GitHubApi::class)
  public fun github(owner: String, repo: String, tag: String, assetName: String): NamedDomainObjectProvider<GitHubApi> =
    named("github", GitHubApi::class) {
      add(owner, repo, tag, assetName)
    }
  public fun github(owner: String, repo: String, tag: String, assetName: String, configurationAction: Action<GitHubApi>): NamedDomainObjectProvider<GitHubApi> =
    named("github", GitHubApi::class) {
      add(owner, repo, tag, assetName)
      configurationAction.execute(this)
    }
  public fun github(configurationAction: Action<GitHubApi>): NamedDomainObjectProvider<GitHubApi> =
    named("github", GitHubApi::class, configurationAction)

  public fun NamedDomainObjectProvider<GitHubApi>.add(owner: String, repo: String, tag: String, assetName: String) {
    configure { add(owner, repo, tag, assetName) }
  }
  public operator fun NamedDomainObjectProvider<GitHubApi>.invoke(owner: String, repo: String, tag: String, assetName: String) {
    add(owner, repo, tag, assetName)
  }

  // All zero-arg methods must be annotated or Gradle will think it's an input
  @Internal
  override fun getAsMap(): SortedMap<String, PluginApi<*, *>> = registry.asMap
  @Internal
  override fun getNames(): SortedSet<String> = registry.names
  @Internal
  override fun getRules(): MutableList<Rule> = registry.rules
  @Internal
  override fun getCollectionSchema(): NamedDomainObjectCollectionSchema = registry.collectionSchema
  @Internal
  override fun isEmpty(): Boolean = registry.isEmpty()
  @Internal
  override fun getNamer(): Namer<PluginApi<*, *>> = registry.namer
  @get:Internal
  override val size: Int
    get() = registry.size
}