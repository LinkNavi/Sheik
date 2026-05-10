#include "Clicker.h"
#include <cstdlib>
#include <cstdio>
#include <X11/Xlib.h>
#include <X11/extensions/XTest.h>

Clicker::Clicker() : display_(nullptr) {
    display_ = XOpenDisplay(nullptr);
    if (!display_) {
        fprintf(stderr, "Clicker: failed to open X display\n");
        return;
    }
    
    // Check if XTest extension is available
    int event_base, error_base, major, minor;
    if (!XTestQueryExtension(display_, &event_base, &error_base, &major, &minor)) {
        fprintf(stderr, "Clicker: XTest extension not available\n");
        XCloseDisplay(display_);
        display_ = nullptr;
        return;
    }
    
    fprintf(stderr, "Clicker: X11 XTest initialized (version %d.%d)\n", major, minor);
}

Clicker::~Clicker() {
    if (display_) {
        XCloseDisplay(display_);
    }
}

int Clicker::randJitter(int min, int max) {
    if (min >= max) return min;
    return min + (rand() % (max - min + 1));
}

void Clicker::leftClick() {
    if (!display_) { 
        fprintf(stderr, "leftClick: no display\n"); 
        return; 
    }
    fprintf(stderr, "[Clicker] Sending LEFT click\n");
    
    // Button 1 is left mouse button in X11
    XTestFakeButtonEvent(display_, 1, True, CurrentTime);  // Press
    XTestFakeButtonEvent(display_, 1, False, CurrentTime); // Release
    XFlush(display_);
}

void Clicker::rightClick() {
    if (!display_) { 
        fprintf(stderr, "rightClick: no display\n"); 
        return; 
    }
    fprintf(stderr, "[Clicker] Sending RIGHT click\n");
    
    // Button 3 is right mouse button in X11
    XTestFakeButtonEvent(display_, 3, True, CurrentTime);  // Press
    XTestFakeButtonEvent(display_, 3, False, CurrentTime); // Release
    XFlush(display_);
}
