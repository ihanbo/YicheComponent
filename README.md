## 清量组件化方案一代目
参考得到的方案

`ycbuild-gradle` 目录为写的gradle插件源码，想看的可以看看

# 一. 组件化我们要实现什么：
1.各模块可以单独运行
![image.png](http://upload-images.jianshu.io/upload_images/2288693-13040f7d10867275.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

2.任意模块联合调试：
![联合调试](http://upload-images.jianshu.io/upload_images/2288693-05a1a5739c696d30.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

3.代码隔离，看上图，不管单独运行还是联合调试，每个模块只引用一个一个项目：`ycbaselib`，，依赖通过配置文件来管理，在整个开发周期中都不用`compile`其他`module`，所以各个模块之间看不到其他模块的代码，各个模块交互通过**路由协议**及`ycbaselib`中的**module服务管理**。

#二.怎么用 
##### 问：怎么单独运行模块
答：采用此框架后，如下图所示每个模块可以单独运行
![image.png](http://upload-images.jianshu.io/upload_images/2288693-709a07e2ad1ff222.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/840)

##### 问：如何挂载其他模块运行
答：挂载其他module只需要在当前module根目录的gradle.properties里加下配置`debugComponent=ycpublishlib,yccarlib`，而不需要在`build.gradle`中手动添加,在编译时会根据配置帮你添加依赖，这样就做到了各个模块的代码隔离。

##### 问：每个模块需要在Application里做初始化等一些操作，如何做到？
答：在ycbaselib里定义了一个接口`IApplicationLike`，定义了一些类似Application的方法，该接口定义如下：
 ```
public interface IApplicationLike {
    void onCreate(Application application);  //初始化
    void exitApp();          //退出app
    void onTrimMemory(int level);    //内存等级
}
```
每个模块可以写一个实现了该接口的类，在对应的方法下做相应的操作。在编译期会使用AOP的方式，在真正的Application类里插入这些代码的调用。

##### 问：依赖其他module有几种方式
答：有两种依赖方式，`compile project`或者`compile aar`，根据配置，假如为：`debugComponent=ycpublishlib`则会以`compile project`的方式添加`ycpublishlib`依赖，如果是`debugComponent=aar:ycpublishlib`则会以`compile aar`的方式添加`ycpublishlib`依赖，当然前提是你依赖的模块要发布过aar文件。

# 三.ycbaselib放什么
1. 公共的第三方库，**注意：是所有模块都要用到的**，比如：网络框架、数据库、图片加载、Rxjava，**所有业务相关的第三方库都放各自模块**。
2. 公共res资源：图片、颜色等
3. **服务管理**，各个模块需要对外提供的服务会放此处：
![image.png](http://upload-images.jianshu.io/upload_images/2288693-a49a60bb331ff661.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> 此处只定义各个服务接口，具体的服务实现放在各个业务的模块下。

`ServiceHost`为服务管理商，有两个方法：获取服务和注册服务。
```
//获取服务
public static<T>  T getService(Class<T> clazz)；
//注册服务
public static<T>  void addService(Class<T> clazz, T instance)；  
```
比如我想获取用户模块的某些东西我先从`ServiceHost`获取用户模块的服务类：
```
IUserService service = ServiceHost.getService(IUserService.class);
service.getUserName();
```
拿到用户服务类，就可以使用用户模块提供的的功能了，**注意：假如当前模块没有挂载用户模块， 此处获取到的用户服务为空，要做好判空处理**

> 备注：ycbaselib应该轻量，不含具体业务，只放所有模块都要用到的东西。


# 四.原理
假设：运行A模块，A模块挂载了B模块和C模块
1. 根据编译命令，如`ycpublishlib:assembleRelease`找到当前运行模块也就是A模块，将A模块设为`apply:application` 其他挂载模块B、C设为`apply:library`
2. 设置`SourceSet`，每个模块单独运行时和作为library时可能代码和res资源略有不同，此处根据运行模块（A）和挂载模块（B、C）对`SourceSet`做不同配置
3. 根据A模块里的配置文件（放在gradle.properties里），在编译时添加B、C模块依赖 ，类似于动态在buildgradle里添加`compile project(':Bproject')`。。。
4. 编译结束后，遍历所有的class文件，开始字节码插入功能，根据A模块配置文件里的`Application`类名全称，找到A模块的`Application`类，然后根据根项目配置里的applikename类名的全称，找到所有实现了`IApplicationLike`接口的类，然后在`Application`里挨个调用`IApplicationLike`对象的相关方法实现其他挂载模块的初始化工做等。

# 五.怎么做
### 配置：
1.在项目根目录的gradle.properties里添加如下配置：

```
mainmodulename=app    //主项目是哪个module
applikename = com.yiche.ycbaselib.component.IApplicationLike    //IApplicationLike接口全名
```
2.在每个模块下添加gradle.properties文件，然后添加如下配置：

```
isRunAlone=true
applicationName = 'com.yiche.circles.CirclesApplication'  //该模块单独运行时配置的Application全名
debugComponent=ycpublishlib    //debug依赖
compileComponent=ycpublishlib  //release依赖
```
3.在项目根目录build.gradle里：

```
buildscript {
    repositories {
        //...略...
         jcenter()
    }
    dependencies {
        clclasspath 'com.yiche.litecomponent:ycbuild-gradle:1.0.3'
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

```
apply plugin: 'com.yiche.litecomponent'//注意这里，不再是android.application或者library之类
//...略...
```
5.代码部分，**在除主模块以外其他每个模块main目录下新建runalone目录**：
这个是单独运行时使用的,可以只放置一个AndroidManifest.xml，用来配置application信息以及启动的Activity等信息，也可以放java目录和res目录用于存放代码和资源文件，运行时会将runalone里的代码和res和main目录下的合并。目录结构如下图所示。
![添加runalone目录](http://upload-images.jianshu.io/upload_images/2288693-3b492de02edb5cfa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

[参考]以下是插件里的部分源码，展示合并SourceSet，PS：AndroidManifest不能合并，单独运行和作为library时各自用各自的
![参考参考](http://upload-images.jianshu.io/upload_images/2288693-9248a630c890ccad.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

f每个模块写一个类，实现`ycbaselib`模块下的`IApplicationLike`接口，用于模仿`Application`该接口定义如下：
```
public interface IApplicationLike {
    void onCreate(Application application);
    void exitApp();
    void onTrimMemory(int level);
}
```
类似于Application的功能，在这里根据需求添加模块的初始化、或者退出app时的操作等。这里的代码会在编译时动态插入。
(完毕)
###配置说明：
#### 1.根目录的gradle.properties
**mainmodulename**：标记哪个模块是主项目，一般都是app，设置这个是为了当直接输入`assembleRelease`等构建命令的时候知道哪个是主项目入口。
> ps：编译某个moudle的命令是`gradlew modulename:assembleRelease`这样。咱们一般敲`assembleRelease`相当于`app:assembleRelease`

**applikename** :`IApplicationLike`是一个接口，模仿Application的功能，一般放在base包下(比如本项目的`ycbaselib`包下)。为什么要放这个全名呢，因为在编译时要动态添加代码，会根据这个全名找到所有实现了该接口的类，然后在Application里动态插入代码。   

> 问：如何实现模拟Application功能？
> 答：当编译完成后，本框架会通过字节码插入的方式，遍历所有的class文件找到所有实现了`IApplicationLike`接口的对象，然后在真正的Application下挨个插入调用代码。
#### 2.模块目录的gradle.properties
**isRunAlone**：一般为true，只有当本模块需要发布library包也就是打aar包的的时候改为false。

**applicationName** ：本模块单独运行时指定的Application类全名。每个模块都必须定义一个Application，因为每个模块都可能挂载其他模块联合运行，其他模块有可能需要在Application里做一些操作。所以就算本模块不需要使用Application也需要定义一个Application类。
>问：为什么需要在配置里加applicationName呢？
答：在编译期间需要动态添加代码，根据`applicationName`找到真正的`Application`类，然后再找到所有实现了`IApplicationLike`接口的类，然后在`Application`类的对应方法里添加所有`IApplicationLike`实现类的调用代码。

PS:`applicationName`是强制需要指定的。你定义的Application可以不写任何代码如下图，这样也不影响注入代码的：
```
//记得在runalone目录下的AndroidManifest里注册
public class CarApplication extends Application{
  //我是空的
}
```

**debugComponent、compileComponent**：本模块debug依赖以及realease依赖。用于配置联合其他模块调试时的依赖，会在编译期间根据这个配置帮你引用其他项目。
>依赖有两种方式，比如`debugComponent:ycuserlib,aar:yccarlib`表示依赖ycuserlib和yccarlib两个模块，前者是直接直接compile ycuserlib的projrct，后者是compile yccarlib发布的aar文件。当某个模块配置`isRunAlone`为false会打一个aar包并拷贝到根目录里的`release_aars`文件夹，当使用`aar:ycxxlib`依赖时，会到这个目录下找对应的aar文件并添加依赖。



# 其他：
ycuserblib提供了一个无侵入初始化的方案，通过ContentProvider来实现，原理参考[使用ContentProvider初始化你的Library](https://www.jianshu.com/p/5c0570263dfd)，可以不用在Application的onCreate里加代码来时现模块的初始化。



