# minimum CMake version
cmake_minimum_required(VERSION 3.22.1)

# project name
project("trafficmanagermobile")

# directory to include header files
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../../core/Traffic-Manager)

# create a shared library from the native-lib.cpp file
add_library(trafficmanagermobile SHARED
        native-lib.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../core/Traffic-Manager/aes.c)

# include libraries needed for logging
find_library(log-lib log)
target_link_libraries(trafficmanagermobile ${log-lib})