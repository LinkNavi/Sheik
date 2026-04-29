#include "MainWindow.h"

MainWindow::MainWindow(QWidget *parent) : QMainWindow(parent) {
    stack = new QStackedWidget(this);
    setCentralWidget(stack);

    mainMenu = new MainMenu(this);
    debugMenu = new DebugMenu(this);

    stack->addWidget(mainMenu);   // index 0
    stack->addWidget(debugMenu);  // index 1

    // Connect signals from menus to switch screens
    connect(mainMenu, &MainMenu::goToDebug, this, [this]() { setScreen(Screen::DebugMenu); });
    connect(debugMenu, &DebugMenu::goBack, this, [this]() { setScreen(Screen::MainMenu); });

    setScreen(Screen::MainMenu);
    setWindowTitle("AutoClicker");
    resize(300, 400);
}

void MainWindow::setScreen(Screen screen) {
    switch (screen) {
        case Screen::MainMenu: stack->setCurrentIndex(0); break;
        case Screen::DebugMenu: stack->setCurrentIndex(1); break;
        default: break;
    }
}