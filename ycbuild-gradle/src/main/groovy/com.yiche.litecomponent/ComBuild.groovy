package com.yiche.litecomponent

import com.yiche.litecomponent.exten.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

public class ComBuild implements Plugin<Project> {

    static final String KEY_FOR_AAR = "publishaar";
    //当前编译的项目名
    //默认是app，直接运行assembleRelease的时候，等同于运行app:assembleRelease
    String compilemodule = "app"
    StringBuilder stringBuilder;

    void apply(Project project) {

        if (!project.rootProject.hasProperty("applikename")||!project.rootProject.hasProperty("mainmodulename")) {
            throw new RuntimeException("需要在根目录gradle.properties配置：applikename、mainmodulename！\n " +
                    "you should set applikename、mainmodulename in rootproject's gradle.properties")
        }

        if (!project.hasProperty("isRunAlone")||!project.hasProperty("applicationName")) {
            throw new RuntimeException("需要在" + module + "'s gradle.properties配置isRunAlone、applicationName" +
                    "you should set isRunAlone、applicationName in " + module + "'s gradle.properties")
        }

        stringBuilder = new StringBuilder();

        String taskNames = project.gradle.startParameter.taskNames.toString()
        String module = project.path.replace(":", "")
        AssembleTask assembleTask = getTaskInfo(project.gradle.startParameter.taskNames)

        stringBuilder.append("taskNames is: " + taskNames)
        stringBuilder.append("\n│  Current module is: " + module)

        if (assembleTask.isAssemble) {
            fetchMainmodulename(project, assembleTask);
            stringBuilder.append("  Launch module  is: " + compilemodule);
        }


        //对于isRunAlone==true的情况需要根据实际情况修改其值，
        // 但如果是false，则不用修改，该module作为一个lib，运行module:assembleRelease则发布aar到中央仓库
        boolean isRunAlone = Boolean.parseBoolean((project.properties.get("isRunAlone")))
        String mainmodulename = project.rootProject.property("mainmodulename")
        if (isRunAlone && assembleTask.isAssemble) {
            //对于要编译的组件和主项目，isRunAlone修改为true，其他组件都强制修改为false
            //这就意味着组件不能引用主项目，这在层级结构里面也是这么规定的
            if (module.equals(compilemodule) || module.equals(mainmodulename)) {
                isRunAlone = true;
            } else {
                isRunAlone = false;
            }
        }
        project.setProperty("isRunAlone", isRunAlone)

        //根据配置添加各种组件依赖，并且自动化生成组件加载代码
        if (isRunAlone) {
            project.apply plugin: 'com.android.application'
            if (!module.equals(mainmodulename)) {
                project.android.sourceSets {
                    main {
                        manifest.srcFile 'src/main/runalone/AndroidManifest.xml'
                        java.srcDirs = ['src/main/java', 'src/main/runalone/java']
                        res.srcDirs = ['src/main/res', 'src/main/runalone/res']
                    }
                }
            }
            stringBuilder.append("\n│  "+"$module apply plugin: " + 'com.android.application');
            if (assembleTask.isAssemble && module.equals(compilemodule)) {
                compileComponents(assembleTask, project)
                project.android.registerTransform(new ComCodeTransform(project))
            }
            Say.say(stringBuilder.toString());
        } else {
            project.apply plugin: 'com.android.library'
            stringBuilder.append("\n│  "+"$module apply plugin: " + 'com.android.library');
            Say.say(stringBuilder.toString());
            project.afterEvaluate {
                Task assembleReleaseTask = project.tasks.findByPath("assembleRelease")
                if (assembleReleaseTask != null) {
                    assembleReleaseTask.doLast {
                        File infile = project.file("build/outputs/aar/$module-release.aar")
                        File outfile = project.file("../release_aars")
                        File desFile = project.file("$module-release.aar");
                        project.copy {
                            from infile
                            into outfile
                            rename {
                                String fileName -> desFile.name
                            }
                        }
                        Say.say("$module-release.aar copy success ");
                    }
                }else{
                    Say.say("$module no need copy aar");
                }
            }
        }

    }

    /**
     * 根据当前的task，获取要运行的组件，规则如下：
     * assembleRelease ---app
     * app:assembleRelease :app:assembleRelease ---app
     * sharecomponent:assembleRelease :sharecomponent:assembleRelease ---sharecomponent
     * @param assembleTask
     */
    private void fetchMainmodulename(Project project, AssembleTask assembleTask) {

        if (assembleTask.modules.size() > 0 && assembleTask.modules.get(0) != null
                && assembleTask.modules.get(0).trim().length() > 0
                && !assembleTask.modules.get(0).equals("all")) {
            compilemodule = assembleTask.modules.get(0);
        } else {
            compilemodule = project.rootProject.property("mainmodulename")
        }
        if (compilemodule == null || compilemodule.trim().length() <= 0) {
            compilemodule = "app"
        }
    }

    private AssembleTask getTaskInfo(List<String> taskNames) {
        AssembleTask assembleTask = new AssembleTask();
        for (String task : taskNames) {
            if (task.toUpperCase().contains("ASSEMBLE")
                    || task.contains("aR")
                    || task.toUpperCase().contains("RESGUARD")) {
                if (task.toUpperCase().contains("DEBUG")) {
                    assembleTask.isDebug = true;
                }
                assembleTask.isAssemble = true;
                String[] strs = task.split(":")
                assembleTask.modules.add(strs.length > 1 ? strs[strs.length - 2] : "all");
                break;
            }
        }
        return assembleTask
    }

    /**
     * 自动添加依赖，只在运行assemble任务的才会添加依赖，因此在开发期间组件之间是完全感知不到的，这是做到完全隔离的关键
     * 支持两种语法：module或者modulePackage:module,前者之间引用module工程，后者使用componentrelease中已经发布的aar
     * @param assembleTask
     * @param project
     */
    private void compileComponents(AssembleTask assembleTask, Project project) {
        String components;
        if (assembleTask.isDebug) {
            components = (String) project.properties.get("debugComponent")
        } else {
            components = (String) project.properties.get("compileComponent")
        }

        if (components == null || components.length() == 0) {
            stringBuilder.append("\n│  "+"there is no add dependencies ");
            return;
        }
        String[] compileComponents = components.split(",")
        if (compileComponents == null || compileComponents.length == 0) {
            stringBuilder.append("\n│  "+"there is no add dependencies ");
            return;
        }
        for (String str : compileComponents) {
            stringBuilder.append("\n│  "+"compile: " + str);
            if (str.contains(":")) {
                File file = project.file("../release_aars/" + str.split(":")[1] + "-release.aar")
                stringBuilder.append("\n│  "+"aar filepath: :AbsolutePath:"+file.getAbsolutePath());
                if (file.exists()) {
                    project.dependencies.add("compile", str + "-release@aar")
                    System.out.println("add dependencies : " + str + "-release@aar");
                } else {
                    throw new RuntimeException(str + " not found ! maybe you should generate a new one ")
                }
            } else {
                project.dependencies.add("compile", project.project(':' + str))
                System.out.println("add dependencies project : " + str);
            }
        }
    }

    private class AssembleTask {
        boolean isAssemble = false;
        boolean isDebug = false;
        List<String> modules = new ArrayList<>();
    }
}