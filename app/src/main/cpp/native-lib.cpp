#include <jni.h>
#include <unistd.h>
#include <string>
#include "AnnBP.h"

#include "android/log.h"
#include <android/asset_manager.h>

static const char *TAG = "Ann-bp";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
double out [6];

extern "C"
JNIEXPORT jdoubleArray

JNICALL
Java_com_example_jhanlu_textgrabber_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */,
        jdoubleArray Attr,
        jobject fd_sys,
        jlong off,
        jlong len) {

    /*
     * 获取输入bp.dat输入流
     */
    jclass fdClass = (*env).FindClass("java/io/FileDescriptor");
    if(fdClass != NULL) {
        LOGD("fdClass is not null");
        jfieldID fdClassDescriptorFieldID = (*env).GetFieldID(fdClass, "descriptor", "I");
        if (fdClassDescriptorFieldID != NULL && fd_sys != NULL) {
            LOGD("fdClassDescriptorFieldID is not null");
            jint fd = (*env).GetIntField(fd_sys, fdClassDescriptorFieldID);
            int mfd = dup(fd);

            /*
             * 获取输入指针
             */
            jdouble *in;
            jboolean iscopy;
            in = (*env).GetDoubleArrayElements(Attr, &iscopy);
            CAnnBP bp;
            bp.Read(mfd, off);
            bp.Identify(in, 28 * 28, out, 6);
            jdoubleArray newArr = (*env).NewDoubleArray(6);
            jdouble *nArr = (*env).GetDoubleArrayElements(newArr, NULL);
            for (int i = 0; i < 6; i++) {
                nArr[i] = out[i];
            }
            (*env).ReleaseDoubleArrayElements(newArr, nArr, 0);
            return newArr;
        }
    }
}


