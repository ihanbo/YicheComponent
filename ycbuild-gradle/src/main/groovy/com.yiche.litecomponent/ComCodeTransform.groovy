package com.yiche.litecomponent

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.*
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import com.yiche.litecomponent.exten.*

public class ComCodeTransform extends Transform {

    private Project project
    ClassPool classPool
    String applicationName;
    String applikeName;
    StringBuilder mStringBuilder;
    boolean isDebugTask = false;    //标记是debug还是release

    ComCodeTransform(Project project,boolean isDebug) {
        this.project = project
        isDebugTask = isDebug;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        mStringBuilder = new StringBuilder();
        getRealApplicationName(transformInvocation.getInputs());
        getAppLikeName(transformInvocation.getInputs());
        classPool = new ClassPool()
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }
        def box = ConvertUtils.toCtClasses(transformInvocation.getInputs(), classPool)

        //要收集的application，一般情况下只有一个
        List<CtClass> applications = new ArrayList<>();
        //要收集的applicationlikes，一般情况下有几个组件就有几个applicationlike
        List<CtClass> activators = new ArrayList<>();

        for (CtClass ctClass : box) {
            if (isApplication(ctClass)) {
                applications.add(ctClass)
                continue;
            }
            if (isActivator(ctClass)) {
                activators.add(ctClass)
            }
        }
        mStringBuilder.append("\n│  "+"Config ApplicationName is: " + applicationName);
        mStringBuilder.append("\n│  "+"Config AppLikeName is: " + applikeName);

        mStringBuilder.append("\n│");
        if(applications.isEmpty()){
            mStringBuilder.append("\n│  "+"Not Found Application,You Must Set One");
        }else{
            for (CtClass ctClass : applications) {
                mStringBuilder.append("\n│  "+"Found application is:  " + ctClass.getName());
            }
        }

        mStringBuilder.append("\n│");
        if(activators.isEmpty()){
            mStringBuilder.append("\n│  "+"Not Found Any Applicationlike!!!");
        }else{
            for (CtClass ctClass : activators) {
                mStringBuilder.append("\n│  "+"Found applicationlike is: " + ctClass.getName());
            }
        }



        transformInvocation.inputs.each { TransformInput input ->
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)

            }
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
                String fileName = directoryInput.file.absolutePath
                File dir = new File(fileName)
                String applicationFilePath  = "Oops not found Application have you set?";
                dir.eachFileRecurse { File file ->
                    String filePath = file.absolutePath

                    String classNameTemp = filePath.replace(fileName, "")
                            .replace("\\", ".")
                            .replace("/", ".")
                    if (classNameTemp.endsWith(".class")) {
                        String className = classNameTemp.substring(1, classNameTemp.length() - 6)
                        if (className.equals(applicationName)) {
                            applicationFilePath= "Application filepath:"+filePath+" classNameTemp1-6:"+classNameTemp;
                            injectApplicationCode(applications.get(0), activators, fileName);
                        }
                    }
                }
                mStringBuilder.append("\n│");
                mStringBuilder.append("\n│  "+applicationFilePath);
                Say.say(mStringBuilder.toString());
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes, Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }


    private void getRealApplicationName(Collection<TransformInput> inputs) {
        applicationName = project.properties.get("applicationName");
        if (applicationName == null || applicationName.isEmpty()) {
            throw new RuntimeException("you should set applicationName in rootproject's gradle.properties")
        }
    }

    private void getAppLikeName(Collection<TransformInput> inputs) {
        applikeName = project.rootProject.property("applikename");
        if (applikeName == null || applikeName.isEmpty()) {
            throw new RuntimeException("you should set applicationName in combuild gradle.properties")
        }
    }


    private void injectApplicationCode(CtClass ctClassApplication, List<CtClass> activators, String patch) {
        ctClassApplication.defrost();

        //插入OnCreate代码
        try {
            CtMethod attachBaseContextMethod = ctClassApplication.getDeclaredMethod("onCreate", null)
            attachBaseContextMethod.insertAfter(getOnCreateComCode(activators))
        } catch (CannotCompileException | NotFoundException e) {
            StringBuilder methodBody = new StringBuilder();
            methodBody.append("protected void onCreate() {");
            methodBody.append("super.onCreate();");
            methodBody.
                    append(getOnCreateComCode(activators));
            methodBody.append("}");
            ctClassApplication.addMethod(CtMethod.make(methodBody.toString(), ctClassApplication));
        } catch (Exception e) {

        }

        //插入onTrimMemory代码
        try {
            //CtClass[] paramTypes = {classPool.get(String.class.getName())};
            CtClass[] paramTypes = new  CtClass[1];
            paramTypes[0] = CtClass.intType;
            CtMethod attachBaseContextMethod = ctClassApplication.getDeclaredMethod("onTrimMemory", paramTypes)
            attachBaseContextMethod.insertAfter(getOnTrimMemoryComCode(activators))
        } catch (CannotCompileException | NotFoundException e) {
            StringBuilder methodBody = new StringBuilder();
            methodBody.append("public void onTrimMemory(int level) {");
            methodBody.append("super.onTrimMemory(level);");
            methodBody.
                    append(getOnTrimMemoryComCode(activators));
            methodBody.append("}");
            ctClassApplication.addMethod(CtMethod.make(methodBody.toString(), ctClassApplication));
        } catch (Exception e) {
            Say.say("Exception: "+e.toString());
        }

        //插入exitApp代码
        try {
            CtMethod attachBaseContextMethod = ctClassApplication.getDeclaredMethod("exitApp", null)
            attachBaseContextMethod.insertAfter(getExitAppComCode(activators))
        } catch (Exception e) {
            Say.say("Exception: "+e.toString());
        }
        ctClassApplication.writeFile(patch)
        ctClassApplication.detach()
    }

    private String getOnCreateComCode(List<CtClass> activators) {
        StringBuilder autoLoadComCode = new StringBuilder();
        for (CtClass ctClass : activators) {
            autoLoadComCode.append("new " + ctClass.getName() + "()" + ".onCreate(this,"+isDebugTask+");")
        }
        return autoLoadComCode.toString()
    }

    private String getOnTrimMemoryComCode(List<CtClass> activators) {
        StringBuilder autoLoadComCode = new StringBuilder();
        for (CtClass ctClass : activators) {
            autoLoadComCode.append("new " + ctClass.getName() + "().onTrimMemory(\$1);")
        }
        return autoLoadComCode.toString()
    }

    private String getExitAppComCode(List<CtClass> activators) {
        StringBuilder autoLoadComCode = new StringBuilder();
        for (CtClass ctClass : activators) {
            autoLoadComCode.append("new " + ctClass.getName() + "()" + ".exitApp();")
        }
        return autoLoadComCode.toString()
    }


    private boolean isApplication(CtClass ctClass) {
        try {
            if (applicationName != null && applicationName.equals(ctClass.getName())) {
                return true;
            }
        } catch (Exception e) {
            println "class not found exception class name:  " + ctClass.getName()
        }
        return false;
    }

    private boolean isActivator(CtClass ctClass) {
        try {
            for (CtClass ctClassInter : ctClass.getInterfaces()) {
                if (applikeName.equals(ctClassInter.name)) {
                    return true;
                }
            }
        } catch (Exception e) {
            println "class not found exception class name:  " + ctClass.getName()
        }

        return false;
    }

    @Override
    String getName() {
        return "ComponentCode"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

}