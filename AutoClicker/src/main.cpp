#include <QApplication>
#include <fcntl.h>
#include <unistd.h>
#include <cstdio>
#include "MainWindow.h"

static bool canOpenUinput() {
    int fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
    if (fd >= 0) { close(fd); return true; }
    return false;
}

int main(int argc, char *argv[]) {
    if (!canOpenUinput()) {
        // Re-launch with pkexec (triggers polkit GUI prompt)
        char path[1024];
        ssize_t len = readlink("/proc/self/exe", path, sizeof(path) - 1);
        if (len > 0) {
            path[len] = '\0';
            execl("/usr/bin/pkexec", "pkexec", path, nullptr);
            // If pkexec not found, try absolute path fallback
            perror("pkexec");
        }
        return 1;
    }

    QApplication app(argc, argv);
    MainWindow w;
    w.show();
    return app.exec();
}
