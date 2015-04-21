#include <stdio.h>
#include <stdlib.h>

#include "com_example_ffmpegtest_MyActivity.h"
#include "libavutil/avutil.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"


JNIEXPORT jstring JNICALL Java_com_example_ffmpegtest_MyActivity_getStringFromNative
  (JNIEnv * env , jobject obj)
  {
        const char *url = "/mnt/sdcard/1.mp4";
        av_register_all();

        AVFormatContext *pFormatCtx = NULL;
        int ret = avformat_open_input(&pFormatCtx, url, NULL, NULL);

        ret = avformat_find_stream_info(pFormatCtx, NULL);
        int streamNum = pFormatCtx->nb_streams;

        char wd[512];
        sprintf(wd, "AVCODEC VERSION %u\n, streamNum[%d]"
             , avcodec_version()
             , streamNum
             );
        return (*env)->NewStringUTF(env, wd);
  }