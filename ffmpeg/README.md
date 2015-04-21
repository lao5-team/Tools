#一 mac平台移植ffmpeg for android

##1 下载源码
   要移植ffmpeg，首先得下载ffmpeg的源码
   可以通过下面的命令来获取源码，前提是你得在mac上安装git工具
      **git clone git://source.ffmpeg.org/ffmpeg.git**

##2 下载android ndk开发包
   由于android官方开发者网站登录不方便，所以这里提供百度网盘下载
(http://pan.baidu.com/s/1ntkP1MT)

##3 开始编译
主要参考下面的文章，尽量参照这篇文章的步骤，网上的其他文章都存在不同的错误。
http://blog.yikuyiku.com/?p=4533

比较简单的步骤：
####(1)将mkconfig.sh拷贝到ffmpeg源码处
####(2)修改里面关于ndk的路径
####(3)在源码下执行./mkconfig.sh(根据需要可以将.sh文件的权限赋成755)
####(4)mkconfig.sh中的$PREFIX变量指定了编译文件的生成路径，若编译成功，则相关so库会在该路径中生成。该例子中，这些so库会在ffmpeg/android这个目录中生成。还有so库相关的.h头文件也会在这里边生成。
####(5)本例中生成的so库包括libavcodec-56.so,libavfilter-5.so,libavformat-56.so,libavutil-54.so,libswresample-1.so,libswscale-3.so

##4 在android 应用中调用ffmpeg的so库
####(1)新建一个普通的android应用工程
####(2)在工程根目录下新建一个jni的目录，然后在里面新建一个prebuilt的目录。把编译ffmpeg库生成的头文件拷贝到jni目录下，把so库拷贝到prebuilt目录下。
####(3)在jni目录下，创建Android.mk文件，文件内容见代码。
####(4)在jni目录下，（如果你已经将ndk目录添加到环境变量中），在控制台输入ndk build命令： ndk-build APP_ABI=armeabi-v7a 进行编译。编译成功，会在整个工程目录下生成一个libs的目录，并将编译出来的so库拷贝过去。
####(5)在的Java代码中加载这些库。注意加载so库的顺序是有依赖的，不能错。
	    static {
        System.loadLibrary("avutil-54");
        System.loadLibrary("swresample-1");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("swscale-3");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("ffmpeg_codec");
    }
####(6)调用jni函数来调用jni函数。这里仅仅打印出了视频的编码信息。
	String version = getStringFromNative();
####(7)向手机的mnt/sdcard/路径中存放一个有效的1.mp4文件，运行程序。

		

