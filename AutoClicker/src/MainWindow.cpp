#include "MainWindow.h"
#include "Style.h"

MainWindow::MainWindow(QWidget *parent) : QMainWindow(parent) {
    // Apply global stylesheet
    qApp->setStyleSheet(sheikStyleSheet());

    stack = new QStackedWidget(this);
    setCentralWidget(stack);

    mainMenu     = new MainMenu(this);
    debugMenu    = new DebugMenu(this);
    settingsMenu = new SettingsMenu(this);

    stack->addWidget(mainMenu);      // index 0
    stack->addWidget(debugMenu);     // index 1
    stack->addWidget(settingsMenu);  // index 2

    connect(mainMenu,     &MainMenu::goToDebug,    this, [this]() { setScreen(Screen::DebugMenu);    });
    connect(mainMenu,     &MainMenu::goToSettings, this, [this]() { setScreen(Screen::SettingsMenu); });
    connect(debugMenu,    &DebugMenu::goBack,       this, [this]() { setScreen(Screen::MainMenu);     });
    connect(settingsMenu, &SettingsMenu::goBack,    this, [this]() { setScreen(Screen::MainMenu);     });

    setScreen(Screen::MainMenu);
    setWindowTitle("Sheik AutoClicker");
    setFixedSize(320, 480);
}

void MainWindow::setScreen(Screen screen) {
    switch (screen) {
        case Screen::MainMenu:    stack->setCurrentIndex(0); break;
        case Screen::DebugMenu:   stack->setCurrentIndex(1); break;
        case Screen::SettingsMenu: stack->setCurrentIndex(2); break;
        default: break;
    }
}