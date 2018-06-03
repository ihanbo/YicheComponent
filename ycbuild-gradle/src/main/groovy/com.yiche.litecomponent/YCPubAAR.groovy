package com.yiche.litecomponent;

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
/**
 * Task installing an app.
 */
public class YCPubAAR extends DefaultTask {
    @InputFile
    File adbExe
    @InputFile
    File packageFile
    @TaskAction
    void generate() {
        project.exec {
            executable = getAdbExe()
            getGradle()
            getPackageFile()
            getGradle()
            args 'install'
            args '-r'
            args getPackageFile()
        }
    }
}