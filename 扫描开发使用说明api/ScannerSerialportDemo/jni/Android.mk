LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
TARGET_PLATFORM := android-3
LOCAL_MODULE    := cepri
LOCAL_SRC_FILES := cepri.c
LOCAL_LDLIBS    := -llog
include $(BUILD_SHARED_LIBRARY)

