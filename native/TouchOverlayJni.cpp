#include <jni.h>
#include <SDL.h>
#include <cstdint>
#include <cstring>
#include <new>
#include "../OpenClaw/OpenClaw/TouchOverlayEvent.h"

extern "C" JNIEXPORT void JNICALL
Java_com_openclaw_game_NativeTouchBridge_nativeTouchAction(
    JNIEnv* env, jclass, jstring action, jboolean pressed, jfloat x, jfloat y)
{
    if (!action) return;
    const char* chars=env->GetStringUTFChars(action,NULL);
    if (!chars) return;
    int id=OPENCLAW_TOUCH_UNKNOWN;
    if (!std::strcmp(chars,"MOVE")) id=OPENCLAW_TOUCH_MOVE;
    else if (!std::strcmp(chars,"JUMP")) id=OPENCLAW_TOUCH_JUMP;
    else if (!std::strcmp(chars,"ATTACK")) id=OPENCLAW_TOUCH_ATTACK;
    env->ReleaseStringUTFChars(action,chars);
    if (id==OPENCLAW_TOUCH_UNKNOWN) return;
    OpenClawTouchPayload* p=new(std::nothrow) OpenClawTouchPayload();
    if (!p) return;
    p->pressed=(pressed==JNI_TRUE); p->x=x; p->y=y;
    SDL_Event e; SDL_zero(e);
    e.type=SDL_USEREVENT; e.user.code=OPENCLAW_TOUCH_OVERLAY_EVENT_CODE;
    e.user.data1=reinterpret_cast<void*>(static_cast<intptr_t>(id));
    e.user.data2=p;
    if (SDL_PushEvent(&e)<0) delete p;
}
