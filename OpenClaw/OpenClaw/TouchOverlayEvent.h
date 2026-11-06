#ifndef OPENCLAW_TOUCH_OVERLAY_EVENT_H
#define OPENCLAW_TOUCH_OVERLAY_EVENT_H
#include <SDL.h>
static const Sint32 OPENCLAW_TOUCH_OVERLAY_EVENT_CODE = 0x4F43;
struct OpenClawTouchPayload { bool pressed; float x; float y; };
enum OpenClawTouchAction { OPENCLAW_TOUCH_UNKNOWN=0, OPENCLAW_TOUCH_MOVE=1, OPENCLAW_TOUCH_JUMP=2, OPENCLAW_TOUCH_ATTACK=3 };
#endif
