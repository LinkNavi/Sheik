cmake_minimum_required(VERSION 3.16)
project(SheikLauncher)

set(CMAKE_CXX_STANDARD 17)
find_package(Qt6 REQUIRED COMPONENTS Core Quick)

qt_add_executable(SheikLauncher
    src/main.cpp
)

qt_add_qml_module(SheikLauncher
    URI SheikLauncher
    VERSION 1.0
    QML_FILES
        qml/main.qml
)

target_link_libraries(SheikLauncher PRIVATE Qt6::Core Qt6::Quick)
