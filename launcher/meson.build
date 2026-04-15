qt6 = import('qt6')

qt6_dep = dependency('qt6', modules: ['Core', 'Gui', 'Network', 'Qml', 'Quick', 'QuickControls2'])

qml_resources = qt6.compile_resources(
    name: 'qml_resources',
    sources: ['qml.qrc', 'certs.qrc'],
)

moc_files = qt6.compile_moc(
    headers: [
        'src/launcher/LaunchController.h',
        'src/auth/AuthController.h',
    ],
    dependencies: qt6_dep,
)

launcher_sources = files(
    'src/main.cpp',
    'src/launcher/LaunchController.cpp',
    'src/auth/AuthController.cpp',
)

executable('sheik-launcher',
    launcher_sources + qml_resources + moc_files,
    dependencies: qt6_dep,
    install: true,
)
