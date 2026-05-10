project('AutoClicker', 'cpp',
  version : '0.1.0',
  default_options : ['cpp_std=c++17'])

qt6 = import('qt6')
qt6_dep = dependency('qt6', modules: ['Core', 'Gui', 'Widgets'])
x11_dep = dependency('x11')
xtst_dep = dependency('xtst')
inc = include_directories('include')

moc_files = qt6.compile_moc(headers: [
  'include/MainMenu.h',
  'include/DebugMenu.h',
  'include/MainWindow.h',
  'include/SettingsMenu.h',
])

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
  dependencies: [qt6_dep, x11_dep, xtst_dep],
  include_directories: inc)
