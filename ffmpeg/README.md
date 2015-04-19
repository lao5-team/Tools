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
####(4) mkconfig.sh中的$PREFIX变量指定了编译文件的生成路径，若编译成功，则相关so库会在该路径中生成。

ndk build命令 ndk-build APP_ABI=armeabi-v7a

