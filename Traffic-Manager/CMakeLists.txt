cmake_minimum_required(VERSION 3.15)

project(Traffic-Manager VERSION 1.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

if(MSVC)
    set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif()

add_executable(TrafficSimulator main.cpp)

include(FetchContent)
FetchContent_Declare(
  googletest
  GIT_REPOSITORY https://github.com/google/googletest.git
  GIT_TAG        release-1.12.1
)

FetchContent_MakeAvailable(googletest)

add_executable(TrafficTests tests/TrafficManagerTests.cpp)

target_link_libraries(TrafficTests gtest_main)

include(GoogleTest)
gtest_discover_tests(TrafficTests)