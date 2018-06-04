package com.yiche.litecomponent

import com.yiche.litecomponent.exten.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

public class ComBuild implements Plugin<Project> {

    static final String KEY_FOR_AAR = "publishaar";
    static final String TASK_PUB_AAR = "ycpubaar";
    //当前编译的项目名
    //默认是app，直接运行assembleRelease的时候，等同于运行app:assembleRelease
    String launchmodule = "app"
    String mainmodulename
    StringBuilder stringBuilder;



    void apply(Project project) {

        if (!project.rootProject.hasProperty("applikename")||!project.rootProject.hasProperty("mainmodulename")) {
            throw new RuntimeException("需要在根目录gradle.properties配置：applikename、mainmodulename！\n " +
                    "you should set applikename、mainmodulename in rootproject's gradle.properties")
        }

        //!project.hasProperty("isRunAlone")||
        if (!project.hasProperty("applicationName")) {
            throw new RuntimeException("需要在" + module + "'s gradle.properties配置isRunAlone、applicationName" +
                    "you should set isRunAlone、applicationName in " + module + "'s gradle.properties")
        }

        stringBuilder = new StringBuilder("Task names：");

        //当前的模块
        String module = project.path.replace(":", "")

        AssembleTask assembleTask = getTaskInfo(project.gradle.startParameter.taskNames)

        for (String task : project.gradle.startParameter.taskNames) {
            stringBuilder.append("\n│    "+task);
        }
        stringBuilder.append("\n│  ")
        stringBuilder.append("\n│  isDebug : " + assembleTask.isDebug)
        stringBuilder.append("\n│  Current module is: " + module)

        if (assembleTask.isAssemble) {
            fetchLaunchModulename(project, assembleTask);
            stringBuilder.append("  Launch module  is: " + launchmodule);
        }


        mainmodulename = project.rootProject.property("mainmodulename")

        if(assembleTask.isPublishAAR&&launchmodule.equals(mainmodulename)){
            throw new RuntimeException("main module can't publish aar!")
        }


        //module  当前模块
        //mainmodulename  主模块
        //launchmodule 运行的模块
        //assembleTask.isAssemble


        //对于isLibrary==true该module作为一个lib，运行module:assembleRelease或者执行bundleRelease则发布aar到中央仓库
        String pps = project.properties.get("isLibrary");
        boolean isLibrary = pps!=null&&Boolean.parseBoolean(pps)==true

        //主模块不能设为library
        if(isLibrary&&module.equals(mainmodulename)){
            throw new RuntimeException("main module can't set to be library!")
        }



        if (assembleTask.isAssemble) {
            //对于编译任务，除了编译的模块，和主模块，其他模块isLibrary设为true
            if (!module.equals(launchmodule) &&!module.equals(mainmodulename)) {
                isLibrary = true;
            }
        }

        //根据配置添加各种组件依赖，并且自动化生成组件加载代码
        if (isLibrary) {
            project.apply plugin: 'com.android.library'
            stringBuilder.append("\n│  "+"$module apply plugin: " + 'com.android.library');
            Say.say(stringBuilder.toString());
            project.afterEvaluate {
                //assembleRelease才会拷贝aar包
                def copyaar = {
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
                Task assr = project.tasks.findByPath("assembleRelease")

                if(assr!=null){
                    assr.doLast {copyaar}
                }
            }
        } else {
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
            if (assembleTask.isAssemble && module.equals(launchmodule)) {
                compileComponents(assembleTask, project)
                project.android.registerTransform(new ComCodeTransform(project,assembleTask.isDebug))
            }
            Say.say(stringBuilder.toString());
        }

//
//
//
//        if(assembleTask.isPublishAAR ){
//            project.apply plugin: 'com.android.library'
//            project.afterEvaluate {
//                //assembleRelease才会拷贝aar包
//                Task lastTask = project.tasks.findByPath("bundleRelease")
//
//                if (lastTask != null) {
//                    lastTask.doLast {
//                        File infile = project.file("build/outputs/aar/$module-release.aar")
//                        File outfile = project.file("../release_aars")
//                        File desFile = project.file("$module-release.aar");
//                        project.copy {
//                            from infile
//                            into outfile
//                            rename {
//                                String fileName -> desFile.name
//                            }
//                        }
//                        Say.say("$module-release.aar copy success ");
//                    }
//                }
//
//            }
//        }else if (assembleTask.isAssemble) {
//            if(module.equals(launchmodule)){
//                project.apply plugin: 'com.android.application'
//                if(!module.equals(mainmodulename)){
//                    project.android.sourceSets {
//                        main {
//                            manifest.srcFile 'src/main/runalone/AndroidManifest.xml'
//                            java.srcDirs = ['src/main/java', 'src/main/runalone/java']
//                            res.srcDirs = ['src/main/res', 'src/main/runalone/res']
//                        }
//                    }
//                }
//                compileComponents(assembleTask, project)
//                project.android.registerTransform(new ComCodeTransform(project,assembleTask.isDebug))
//            }else{
//                project.apply plugin: 'com.android.library'
//            }
//
//        } else {
//            project.apply plugin: 'com.android.application'
////            project.tasks.create("ycaar",{
////                project.exec {
////                    executable = getAdbExe()
////                    args 'install'
////                    args '-r'
////                    args getPackageFile()
////                }
////            })
//        }

    }

    /**
     * 根据当前的task，获取要运行的组件，规则如下：
     * assembleRelease ---app
     * app:assembleRelease :app:assembleRelease ---app
     * sharecomponent:assembleRelease :sharecomponent:assembleRelease ---sharecomponent
     * @param assembleTask
     */
    private void fetchLaunchModulename(Project project, AssembleTask assembleTask) {

        if(assembleTask.modules.size() > 0){
            Say.say("FetchMainmodulename--size:"+assembleTask.modules.size()+" Task0: "+assembleTask.modules.get(0).toString());
        }
        if (assembleTask.modules.size() > 0 && assembleTask.modules.get(0) != null
                && assembleTask.modules.get(0).trim().length() > 0
                && !assembleTask.modules.get(0).equals("all")) {
            launchmodule = assembleTask.modules.get(0);
        } else {
            launchmodule = project.rootProject.property("mainmodulename")
        }
        if (launchmodule == null || launchmodule.trim().length() <= 0) {
            launchmodule = "app"
        }
    }

    private AssembleTask getTaskInfo(List<String> taskNames) {
        AssembleTask assembleTask = new AssembleTask();
        for (String task : taskNames) {
            if(task.contains(TASK_PUB_AAR)){
                assembleTask.isPublishAAR = true;
                return;
            }
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
     * 支持两种语法：module或者aar:module,前者之间引用module工程，后者使用componentrelease中已经发布的aar
     * 例如：compile=ycpublishlib、compile=aar:ycpublishlib,ycuserlib
     * 1.多个之间逗号隔开
     * 2.debug下的依赖用debugComile关键字，release下的依赖用releaseCompile关键字
     * @param assembleTask
     * @param project
     */
    private void compileComponents(AssembleTask assembleTask, Project project) {
        String components;

        if (assembleTask.isDebug) {
            components = (String) project.properties.get("debugComile")
        } else {
            components = (String) project.properties.get("releaseCompile")
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
            if(str.contains(mainmodulename)){
                throw new RuntimeException("can't compile main module!!!");
            }
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
        boolean isPublishAAR = false;
        boolean isAssemble = false;
        boolean isDebug = false;
        List<String> modules = new ArrayList<>();
    }
}