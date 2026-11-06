#include <jni.h>
#include <SDL.h>
#include <cstring>

#ifndef OPENCLAW_TOUCH_EVENT
#define OPENCLAW_TOUCH_EVENT 0x8000
#endif

extern "C" JNIEXPORT void JNICALL
Java_com_openclaw_game_NativeTouchBridge_nativeTouchAction(
    JNIEnv* env, jclass, jstring action, jboolean pressed, jfloat x, jfloat y)
{
    const char* chars = env->GetStringUTFChars(action, 0);
    SDL_Event e; SDL_zero(e);
    e.type = SDL_USEREVENT;
    e.user.code = OPENCLAW_TOUCH_EVENT;
    // encode action into a stable small enum; do not retain Java string memory
    if (!std::strcmp(chars, "MOVE")) e.user.data1 = (void*)1;
    else if (!std::strcmp(chars, "JUMP")) e.user.data1 = (void*)2;
    else if (!std::strcmp(chars, "ATTACK")) e.user.data1 = (void*)3;
    else e.user.data1 = (void*)0;
    e.user.data2 = (void*)(intptr_t)((pressed ? 1 : 0));
    // Preserve analog joystick vector as integer fixed-point in data fields via event padding.
    e.user.timestamp = (Uint32)((int)(x * 1000.0f) & 0xffff) | ((Uint32)((int)(y * 1000.0f) & 0xffff) << 16);
    SDL_PushEvent(&e);
    env->ReleaseStringUTFChars(action, chars);
}
