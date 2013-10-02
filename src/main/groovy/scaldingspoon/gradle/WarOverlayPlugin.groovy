package scaldingspoon.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.War

/**
 * Plugin class to support WAR overlay
 */
class WarOverlayPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.plugins.apply(WarPlugin)
        project.convention.plugins.warOverlay = new WarOverlayPluginConvention()

        project.tasks.withType(War, new Action<War>() {
            @Override
            void execute(War war) {
                war.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                war.doFirst {
                    war.classpath = war.classpath.filter { !it.name.endsWith(".war") }

                    war.project.configurations.runtime.each {
                        if (it.name.endsWith(".war")) {
                            def fileList = war.project.zipTree(it)
                            if (project.convention.plugins.warOverlay.includeWarJars) {
                                war.from fileList
                            } else {
                                war.from fileList.matching { exclude "**/*.jar" }
                            }
                        }
                    }
                }
            }
        })
    }
}

/**
 * Plugin convention to configure war overlay specific parameters
 */
class WarOverlayPluginConvention {
    boolean includeWarJars = false

    def warOverlay(Closure c) {
        c.delegate = this
        c()
    }

    def methodMissing(String name, args) {
        this."${name}" = args[0]
    }
}