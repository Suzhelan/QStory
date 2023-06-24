#最低版本号
cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} -ffunction-sections -fdata-sections -std=c++20")

#查找预编译库 并将它的路径存储在变量中
find_library(
        ANDROID_LIBS
        log)

#链接加载目录下构建脚本
add_subdirectory(DexBuilder)
#生成动态共享库
add_library(
        dexfinder SHARED
        biliroaming.cc
)

#设置target链接的库
target_link_libraries(dexfinder dex_builder)
