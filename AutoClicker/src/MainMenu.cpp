#include "MainMenu.h"
#include <QDebug>
#include <QStyle>
#include <fcntl.h>
#include <unistd.h>
#include <dirent.h>
#include <cstring>
#include <cstdio>
#include <linux/input.h>

// ── Find the real mouse evdev device ─────────────────────────────
bool MainMenu::openEvdev() {
    DIR *d = opendir("/dev/input");
    if (!d) {
        fprintf(stderr, "Evdev: failed to open /dev/input\n");
        return false;
    }
    struct dirent *ent;
    while ((ent = readdir(d)) != nullptr) {
        if (strncmp(ent->d_name, "event", 5) != 0) continue;
        char path[64];
        snprintf(path, sizeof(path), "/dev/input/%s", ent->d_name);
        int fd = open(path, O_RDONLY | O_NONBLOCK);
        if (fd < 0) {
            fprintf(stderr, "Evdev: failed to open %s\n", path);
            continue;
        }
        // Get device name
        char name[256] = "Unknown";
        ioctl(fd, EVIOCGNAME(sizeof(name)), name);
        fprintf(stderr, "Evdev: checking %s - %s\n", path, name);
        
        // Check it has BTN_LEFT (mouse)
        uint8_t bits[KEY_MAX / 8 + 1] = {};
        ioctl(fd, EVIOCGBIT(EV_KEY, sizeof(bits)), bits);
        if (bits[BTN_LEFT / 8] & (1 << (BTN_LEFT % 8))) {
            evdevFd = fd;
            closedir(d);
            fprintf(stderr, "Evdev: selected %s (%s) with BTN_LEFT capability\n", path, name);
            return true;
        }
        ::close(fd);
    }
    closedir(d);
    fprintf(stderr, "Evdev: no suitable mouse device found\n");
    return false;
}


MainMenu::MainMenu(SettingsMenu *settings, QWidget *parent)
    : QMainWindow(parent), settings(settings)
{
    centralWidget = new QWidget(this);
    setCentralWidget(centralWidget);

    layout = new QVBoxLayout(centralWidget);
    layout->setSpacing(0);
    layout->setContentsMargins(0, 0, 0, 0);

    // ── Top bar ──────────────────────────────────────────────────
    auto *topBar = new QWidget(this);
    topBar->setObjectName("topBar");
    topBarLayout = new QHBoxLayout(topBar);
    topBarLayout->setContentsMargins(16, 10, 16, 10);

    auto *appTitle = new QLabel("SHEIK", this);
    appTitle->setObjectName("appTitle");

    lblConnectionDot  = new QLabel("●", this);
    lblConnectionText = new QLabel("Disconnected", this);
    lblConnectionDot->setObjectName("dotDisconnected");
    lblConnectionText->setObjectName("statusText");

    auto *statusRow = new QHBoxLayout();
    statusRow->setSpacing(5);
    statusRow->addWidget(lblConnectionDot);
    statusRow->addWidget(lblConnectionText);

    btnSettings = new QPushButton("⚙", this);
    btnSettings->setObjectName("btnIcon");
    btnSettings->setFixedSize(32, 32);

    topBarLayout->addWidget(appTitle);
    topBarLayout->addStretch();
    topBarLayout->addLayout(statusRow);
    topBarLayout->addSpacing(12);
    topBarLayout->addWidget(btnSettings);
    layout->addWidget(topBar);

    // ── Content area ─────────────────────────────────────────────
    auto *content = new QWidget(this);
    auto *contentLayout = new QVBoxLayout(content);
    contentLayout->setSpacing(12);
    contentLayout->setContentsMargins(20, 20, 20, 20);

    infoPanel = new QWidget(this);
    infoPanel->setObjectName("infoPanel");
    auto *infoPanelLayout = new QVBoxLayout(infoPanel);
    infoPanelLayout->setSpacing(6);
    infoPanelLayout->setContentsMargins(14, 12, 14, 12);

    auto *infoTitle = new QLabel("TARGET", this);
    infoTitle->setObjectName("panelTitle");

    lblTargetId = new QLabel("No target", this);
    lblTargetId->setObjectName("infoValue");

    lblTargetHP = new QLabel("HP: —", this);
    lblTargetHP->setObjectName("infoValueSub");

    hpBar = new QProgressBar(this);
    hpBar->setRange(0, 20);
    hpBar->setValue(0);
    hpBar->setTextVisible(false);
    hpBar->setFixedHeight(6);
    hpBar->setObjectName("hpBar");

    infoPanelLayout->addWidget(infoTitle);
    infoPanelLayout->addWidget(lblTargetId);
    infoPanelLayout->addWidget(lblTargetHP);
    infoPanelLayout->addWidget(hpBar);
    contentLayout->addWidget(infoPanel);

    btnToggle = new QPushButton("START", this);
    btnToggle->setObjectName("btnToggleOff");
    btnToggle->setMinimumHeight(52);
    contentLayout->addWidget(btnToggle);

    auto *secRow = new QHBoxLayout();
    btnDebugMenu = new QPushButton("Debug", this);
    btnQuit      = new QPushButton("Quit", this);
    btnDebugMenu->setObjectName("btnSecondary");
    btnQuit->setObjectName("btnDanger");
    secRow->addWidget(btnDebugMenu);
    secRow->addWidget(btnQuit);
    contentLayout->addLayout(secRow);

    contentLayout->addStretch();
    layout->addWidget(content);

    // ── Connections ──────────────────────────────────────────────
    connect(btnQuit,      &QPushButton::clicked, this, &QMainWindow::close);
    connect(btnToggle,    &QPushButton::clicked, this, &MainMenu::toggleRunning);
    connect(btnDebugMenu, &QPushButton::clicked, this, [this]() { emit goToDebug(); });
    connect(btnSettings,  &QPushButton::clicked, this, [this]() { emit goToSettings(); });

    updateTimer = new QTimer(this);
    connect(updateTimer, &QTimer::timeout, this, &MainMenu::updateLoop);
    updateTimer->start(100);

    clickTimer = new QTimer(this);
    connect(clickTimer, &QTimer::timeout, this, &MainMenu::clickLoop);

    // ── Evdev mouse listener ─────────────────────────────────────
    if (openEvdev()) {
        evdevNotifier = new QSocketNotifier(evdevFd, QSocketNotifier::Read, this);
        connect(evdevNotifier, &QSocketNotifier::activated, this, &MainMenu::onEvdevReadable);
    } else {
        fprintf(stderr, "Warning: could not open mouse evdev\n");
        evdevNotifier = nullptr;
    }
}

MainMenu::~MainMenu() {
    if (evdevFd >= 0) ::close(evdevFd);
}

void MainMenu::onEvdevReadable() {
    struct input_event ev;
    int count = 0;
    while (read(evdevFd, &ev, sizeof(ev)) == sizeof(ev)) {
        count++;
        if (ev.type != EV_KEY) continue;
        fprintf(stderr, "Evdev event: type=%d code=%d value=%d\n", ev.type, ev.code, ev.value);
        if (ev.code == BTN_LEFT)  {
            leftHeld  = (ev.value == 1);
            fprintf(stderr, "  -> leftHeld = %d\n", leftHeld);
        }
        if (ev.code == BTN_RIGHT) {
            rightHeld = (ev.value == 1);
            fprintf(stderr, "  -> rightHeld = %d\n", rightHeld);
        }
    }
    if (count > 0) {
        fprintf(stderr, "onEvdevReadable: processed %d events\n", count);
    }
}

void MainMenu::toggleRunning() {
    running = !running;
    if (running) {
        btnToggle->setText("STOP");
        btnToggle->setObjectName("btnToggleOn");
        clickTimer->setInterval(settings->leftIntervalMs());
        clickTimer->start();
    } else {
        btnToggle->setText("START");
        btnToggle->setObjectName("btnToggleOff");
        clickTimer->stop();
    }
    btnToggle->style()->unpolish(btnToggle);
    btnToggle->style()->polish(btnToggle);
}

void MainMenu::onSettingsChanged() {
    if (running)
        clickTimer->setInterval(settings->leftIntervalMs());
}

void MainMenu::updateLoop() {
    if (!ipc.valid()) {
        lblConnectionDot->setObjectName("dotDisconnected");
        lblConnectionText->setText("Disconnected");
        lblConnectionDot->style()->unpolish(lblConnectionDot);
        lblConnectionDot->style()->polish(lblConnectionDot);
        return;
    }

    ipc.update();
    const auto* s = ipc.state();

    bool connected = (s->lookingAtBlock || s->targetId != -1);
    QString dotObj = connected ? "dotConnected" : "dotIdle";
    QString statusStr = connected ? "Connected" : "Idle";

    if (lblConnectionDot->objectName() != dotObj) {
        lblConnectionDot->setObjectName(dotObj);
        lblConnectionText->setText(statusStr);
        lblConnectionDot->style()->unpolish(lblConnectionDot);
        lblConnectionDot->style()->polish(lblConnectionDot);
    }

    if (s->targetId != -1) {
        lblTargetId->setText(QString("Entity #%1").arg(s->targetId));
        float hp = s->targetHealth;
        lblTargetHP->setText(QString("HP: %1 / 20").arg(hp, 0, 'f', 1));
        hpBar->setValue(static_cast<int>(hp));
    } else {
        lblTargetId->setText("No target");
        lblTargetHP->setText("HP: —");
        hpBar->setValue(0);
    }
}

void MainMenu::clickLoop() {
    if (!running || !ipc.valid()) return;
    const auto* s = ipc.state();
    if (s->inGui) return;

    HeldItem held = ipc.heldItemType();
    fprintf(stderr, "leftHeld=%d rightHeld=%d\n", leftHeld, rightHeld);

    // Left click — only if left mouse button is physically held
    bool leftClicked = false;
    if (leftHeld && settings->leftEnabled()) {
        uint8_t mask = settings->leftHeldItemMask();
        bool maskPass = (mask == 0) || (mask & (1 << static_cast<uint8_t>(held)));
        bool miningPass = settings->leftAllowMining() || !s->lookingAtBlock;
        if (maskPass && miningPass) {
            clicker.leftClick();
            leftClicked = true;
        }
    }

    // Right click — only if right mouse button is physically held
    bool rightClicked = false;
    if (rightHeld && settings->rightEnabled()) {
        bool blockPass = !settings->rightOnlyWithBlock() || (held == HeldItem::BLOCK);
        if (blockPass) {
            clicker.rightClick();
            rightClicked = true;
        }
    }

    // Set next interval based on which button fired
    if (leftClicked) {
        clickTimer->setInterval(
            settings->leftIntervalMs() +
            clicker.randJitter(settings->leftRandMin(), settings->leftRandMax())
        );
    } else if (rightClicked) {
        clickTimer->setInterval(
            settings->rightIntervalMs() +
            clicker.randJitter(settings->rightRandMin(), settings->rightRandMax())
        );
    }
}
