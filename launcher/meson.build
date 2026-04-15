qt6 = import('qt6')

qt6_dep = dependency('qt6', modules: ['Core', 'Qml', 'Quick', 'QuickControls2'])

qml_resources = qt6.compile_resources(
    name: 'qml_resources',
    sources: 'qml.qrc',
)

moc_files = qt6.compile_moc(
    headers: 'src/launcher/LaunchController.h',
    dependencies: qt6_dep,
)

launcher_sources = files(
    'src/main.cpp',
    'src/launcher/LaunchController.cpp',
)

executable('sheik-launcher',
    launcher_sources + qml_resources + moc_files,
    dependencies: qt6_dep,
    install: true,
)
