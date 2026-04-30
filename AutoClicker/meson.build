project('AutoClicker', 'cpp',
  version : '0.1.0',
  default_options : ['cpp_std=c++17'])

qt6 = import('qt6')
qt6_dep = dependency('qt6', modules: ['Core', 'Gui', 'Widgets'])
xdo = dependency('libei-1.0')
inc = include_directories('include')

# MOC is required because MainMenu.h has Q_OBJECT
moc_files = qt6.compile_moc(headers: ['include/MainMenu.h', 'include/DebugMenu.h', 'include/MainWindow.h', 'include/SettingsMenu.h'])

executable('AutoClicker',
  sources : [
    'src/main.cpp',
    'src/MainMenu.cpp',
    'src/IPC.cpp',
    'src/DebugMenu.cpp',
    'src/MainWindow.cpp',
    'src/SettingsMenu.cpp',
    'src/Clicker.cpp',
    moc_files,
  ],
  dependencies: [qt6_dep, xdo],
  include_directories: inc)
