## 组件化方案插件
参考得到的方案
## 一.说明：
作用：
1. 自动处理application还是libreay问题
2. 处理依赖，解决代码隔离问题
3. 其他：字节码插入等



#### 1. 处理application还是libreay问题：
每个模块默认都是`application`可以直接运行，其**依赖的模块则在编译期间自动替换成library**。

![同步后，每个模块可以直接单独运行](http://upload-images.jianshu.io/upload_images/2288693-709a07e2ad1ff222.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/600)

`SourceSet`处理：
一个模块单独运行时和作为library相比肯定会有一些额外的代码和资源，以及不同的`AndroidManifest`文件，这部分单独放在`runalone`目录,当单独运行时会自动合并。
![runalone目录](https://upload-images.jianshu.io/upload_images/2288693-90a7dbce51fe8485.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/600)



#### 2. 依赖处理：
对其他模块的依赖不写在`build.gradle`里，而是在
`module`的`gradle.properties`里配置：
```
debugCompile=ycpublishlib,yccarlib
releaseCompile=aar:ycpublishlib,yccarlib
```
在编译时会读取配置帮你添加依赖，这样就做到了代码隔离。依赖可使用两种方式：`compile project`和`compile aar`方式。

#### 3.字节码插入功能：
每个模块都有个入口文件，类似于`Application`这里采用，`Applike`接口的方式使用。每个模块都有一个`Applike`的实例，来模拟`Application`的功能，在编译完成会读取所有的`Applike`实例，通过字节码插入的方式在真正的`Application`里插入调用。
> 有一些性能问题，因为要查找类并注入代码，后期解决


## 二.使用：
#### 1. 插件使用：
根目录build.gradle里buildscript依赖:

![根目录build.gradle里buildscript依赖](https://upload-images.jianshu.io/upload_images/2288693-1ab123172ad0bd0f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/600)

在组件的build.gradle里:

![在组件的build.gradle里](https://upload-images.jianshu.io/upload_images/2288693-b9f004568a302581.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/600)


#### 2.配置：
1.在项目根目录的gradle.properties里添加如下配置：

```gradle
mainmodulename=app    //主项目是哪个module
applikename = com.yiche.ycbaselib.component.IApplicationLike    //IApplicationLike接口全名
```
2.在每个模块下添加gradle.properties文件，然后添加如下配置：

```gradle
isRunAlone=true
applicationName = 'com.yiche.circles.CirclesApplication'  //该模块单独运行时Application全名
debugCompile=ycpublishlib    //debug依赖
releaseCompile=ycpublishlib  //release依赖
```
3.在项目根目录build.gradle里：

```gradle
buildscript {
    repositories {
        //...略...
         jcenter()
    }
    dependencies {
        clclasspath 'com.yiche.litecomponent:ycbuild-gradle:1.0.x'
         //...略...
    }
}

allprojects {
    repositories {
        flatDir {
            dirs '../release_aars' //本地依赖aar时指定目录
        }
    }
    //...略...
}
```

4.在每个模块的build.gradle里：

```gradle
apply plugin: 'com.yiche.litecomponent'//注意这里，不再是android.application或者library之类
//...略...
```
5.代码部分，**在除主模块以外其他每个模块main目录下新建runalone目录**，这个是单独运行时额外的代码和资源以及AndroidManifest,单独运行时会和main资源合并。
![添加runalone目录](http://upload-images.jianshu.io/upload_images/2288693-3b492de02edb5cfa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/600)

>备注：`AndroidManifest`不能合并，单独运行和作为library时各自用各自的

###配置说明：
#### 1.根目录的gradle.properties
**mainmodulename**：标记哪个模块是主项目，一般都是app，设置这个是为了当直接输入`assembleRelease`等构建命令的时候知道哪个是主项目入口。
> ps：编译某个moudle的命令是`gradlew modulename:assembleRelease`这样。咱们一般敲`assembleRelease`相当于`app:assembleRelease`

**applikename** :`IApplicationLike`是一个接口，模仿Application的功能，放在base包下(比如本项目的`ycbaselib`包下)。在编译时要动态添加代码，会根据这个全名找到所有实现了该接口的类，然后在真正的`Application`里插入代码。


#### 2.模块目录的gradle.properties
**isLibrary**：可以不加，只有当本模块需要发布library包也就是打aar包的的时候加上`isLibrary=true`,因为默认都是当`application`来运行的
>这个是临时方案，后期会改为task来实现

**applicationName** ：本模块单独运行时指定的Application类全名。每个模块都必须定义一个，单独运行时，字节码插入就是在运行模块的Application里插入的。
>备注: 在编译期间需要动态添加代码，根据`applicationName`找到运行模块的`Application`类，然后再找到所有实现了`IApplicationLike`接口的类，然后在`Application`类的对应方法里添加所有`IApplicationLike`实现类的调用代码。

**debugCompile、releaseCompile**：本模块debug依赖以及release依赖。用于配置依赖的模块。在编译期间根据这个配置帮你引用其他模块。
>备注：依赖有两种方式，直接依赖项目和依赖aar，比如`debugCompile:ycuserlib,aar:yccarlib`表示依赖`ycuserlib`和`yccarlib`两个模块，前者是直接直接`compile   projrct`，后者是`compile aar`文件。每个模块可以发布aar包到根目录里的`release_aars`文件夹。当使用`aar:ycxxlib`依赖时，会到这个目录下找对应的aar文件并添加依赖。



# 其他：
ycuserblib提供了一个无侵入初始化的方案，通过ContentProvider来实现，原理参考[使用ContentProvider初始化你的Library](https://www.jianshu.com/p/5c0570263dfd)，可以不用在Application的onCreate里加代码来时现模块的初始化。



