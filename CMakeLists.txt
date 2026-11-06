
cmake_minimum_required(VERSION 3.22.1)

project(OpenClaw)

option(Android "Build for Android" OFF)
option(Emscripten "Build as WASM" OFF)
option(Extern_Config "Do not embed config file" ON)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(Android)
    set(SDL_SHARED OFF CACHE BOOL "" FORCE)
    set(SDL_STATIC ON CACHE BOOL "" FORCE)
    set(SDL_TEST OFF CACHE BOOL "" FORCE)
    set(SDL_TESTS OFF CACHE BOOL "" FORCE)

    if(NOT EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/android/sdl2/CMakeLists.txt")
        message(FATAL_ERROR "SDL2 source is missing: android/sdl2")
    endif()

    add_subdirectory(android/sdl2)
endif()

add_subdirectory(Box2D)
add_subdirectory(libwap)
add_subdirectory(./ThirdParty/Tinyxml)

if(WIN32)
    add_subdirectory(MidiProc)
endif()

# Keep legacy ThirdParty headers out of Android builds.
if(Android)
    include_directories(
        ./Box2D/
        ./libwap/
        ./ThirdParty/Tinyxml/
    )
else()
    include_directories(
        ./Box2D/
        ./libwap/
        ./ThirdParty/
        ./ThirdParty/Tinyxml/
    )
endif()

if(Emscripten)
    include_directories(
        "${EMSCRIPTEN_PATH}/upstream/emscripten/system/include/emscripten/"
    )
endif()

link_directories(
    ./libwap
    ./Box2D
)

if(WIN32)
    link_directories(
        ./ThirdParty/SDL2/lib/x86
        ./Build_Release
    )
endif()

if(Android)
    set(CMAKE_LIBRARY_OUTPUT_DIRECTORY
        "${CMAKE_CURRENT_BINARY_DIR}/lib/${ANDROID_ABI}"
    )

    add_library(openclaw SHARED "")
else()
    set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ../Build_Release)

    add_executable(openclaw "")
endif()

add_subdirectory(OpenClaw)

list(APPEND TARGET_LIBS
    libwap
    tinyxml
    Box2D
)

if(Android)
    list(APPEND TARGET_LIBS
        SDL2-static
        GLESv2
        GLESv1_CM
        dl
        log
        android
        m
    )
elseif(Emscripten)
    # Emscripten linker settings are configured separately.
else()
    list(APPEND TARGET_LIBS
        SDL2
        SDL2_mixer
        SDL2_ttf
        SDL2_image
        SDL2_gfx
    )

    if(WIN32)
        list(APPEND TARGET_LIBS Rpcrt4)
    else()
        list(APPEND TARGET_LIBS stdc++ m)
    endif()
endif()

target_link_libraries(openclaw ${TARGET_LIBS})
