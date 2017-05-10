#include <jni.h>
#include <string>

#include "include/dll.h"

extern "C"
JNIEXPORT jint JNICALL
Java_com_fnee_carddetector_common_DdsAdapter_nativeCalcTable(
        JNIEnv* env,
        jclass classPointer,
        jintArray cards,
        jintArray results) {
    jint* nativeCards = env->GetIntArrayElements(cards, nullptr);
    jint* nativeResults = env->GetIntArrayElements(results, nullptr);
    int r = CalcDDtable(*reinterpret_cast<ddTableDeal*>(nativeCards), reinterpret_cast<ddTableResults*>(nativeResults));
    env->ReleaseIntArrayElements(cards, nativeCards, 0);
    env->ReleaseIntArrayElements(results, nativeResults, 0);
    return r;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_fnee_carddetector_common_DdsAdapter_nativeReleaseInternalMemory(
        JNIEnv* env) {
    FreeMemory();
}
