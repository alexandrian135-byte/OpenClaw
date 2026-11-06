نظام طبقة لمس قابلة للتخصيص لـ OpenClaw

المحتويات:
- Assets/ControlsCustom/: ضع أي صور PNG هنا مع touch_config.json.
- touch_config.json: لكل زر x,y,size,action واسم الصورة وطريقة التفاعل.
- TouchOverlayView.java: يرسم الصور فوق اللعبة ويدعم تعدد اللمس والسحب في وضع التعديل.
- NativeTouchBridge.java + native/TouchOverlayJni.cpp: يرسلان أحداث الأزرار إلى SDL.
- patches/: إضافات مطلوبة لربط MOVE/JUMP/ATTACK مع ActorController وClawHumanView.

التخصيص:
1) انسخ مجلد Assets/ControlsCustom إلى app/src/main/assets/ControlsCustom.
2) أنشئ TouchOverlayView فوق سطح SDL داخل نفس Activity (FrameLayout: SDL view أولاً ثم TouchOverlayView).
3) استدع loadFromAssets(getAssets()) ثم setActionSink(new NativeTouchBridge()).
4) فعّل وضع التحرير عبر setEditMode(true)، اسحب الأزرار، ثم احفظ saveConfig(new File(filesDir,"touch_config.json")).
5) لو تستخدم loadFromDirectory، انسخ PNG وملف JSON إلى نفس المجلد الخاص بالتطبيق.
6) تأكد من أن libopenclaw.so محمّل قبل NativeTouchBridge.

مهم:
- x و y مركز الزر كنسبة من عرض/ارتفاع طبقة اللمس بين 0 و1.
- size نسبة من أصغر بُعد للشاشة.
- MOVE يستخدم mode=joystick ويُرسل اتجاهًا نسبيًا بين -1 و1.
- JUMP يضغط/يرفع SDLK_SPACE.
- ATTACK يستدعي ActionType_Attack عند بداية اللمس.
- يجب أن يكون حدث SDL_USER code 0x8000 محجوزًا لهذا الجسر وغير مستخدم في مكان آخر.
- تعديلات ActorController وClawHumanView مرفقة كإضافات، وليست ملفات كاملة بديلة؛ ادمجها في الملفات الأصلية.
- TouchOverlayView يحتاج أن يُضاف فوق SDL Activity. هذه الحزمة لا تحتوي SDLActivity.java ولا مكتبات SDL Android، لأن المستودع الأصلي الذي فُحص لا يضمها.
